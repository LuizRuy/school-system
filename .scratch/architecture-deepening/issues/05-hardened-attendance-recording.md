# 05: Hardened attendance recording

**What to build:** Recording attendance — one student or a whole class session — is a single transactional path that rejects duplicates instead of silently inserting them: marking the same student twice for the same session updates rather than creating a second row. Entity resolution goes through the ownership guard, and the update path stops fetching entities purely for their authorization side effects.

**Blocked by:** 04 (finish guard migration).

**Status:** resolved

- [x] Marking attendance for an already-marked student+session pair upserts instead of inserting a duplicate row
- [x] Single-student and whole-session recording are equally transactional: partial failures leave no partial rows
- [x] A teacher cannot record attendance against another teacher's session or students
- [x] Update path no longer performs fetch-only-for-side-effect calls; assertions cover this via the guard's interface
- [x] API-level tests demonstrate: fresh mark, re-mark, cross-tenant denial, session-not-found

## Comments

**Review (post-implementation, two axes):**

- Spec: faithful. Upsert via find-then-update-or-insert inside `@Transactional` paths (`record` helper); a mid-session denial against a real H2 transaction leaves zero rows (`AttendanceRecordingPersistenceTest.midSessionDenialLeavesNoPartialRows`). Cross-tenant denials surface as 403 (foreign student) / masked 404 (foreign session), inherited unchanged from guard error modes. Update path authorizes through `OwnershipGuard.authorize`, backed by owner-id projections (`findOwnerIdById`) on Student/ClassSession repositories, so authorization no longer loads entities at all; unit tests assert through mocked guard interfaces that `resolve` is never called there, guard tests assert the finder is never consulted during `authorize`. API-level slice drives the real service over real guards with a `UserAuthenticated` principal: fresh mark, re-mark, whole-session upsert, foreign-student 403, foreign-session 404, unknown-session 404.
- Spec (judgement calls): `uk_attendance_student_session` unique constraint added beyond the letter of the upsert bullet — it hardens "rejects duplicates": a concurrent lost race now surfaces as 409 (existing GlobalExceptionHandler mapping) instead of a silently inserted duplicate. Caveat recorded: a dev database already holding duplicate pairs will fail schema update loudly on startup. Single-student rollback is pinned only by annotation reflection; with a single-row write there is no partial state a behavioural test could stage. Concurrent re-mark ends in a rejected duplicate rather than last-write-wins; a DB-native merge was judged out of proportion here.
- Standards: no violations. House style kept (Lombok constructor injection, @DisplayName'd AssertJ/Mockito tests, per-file fixtures). Judgement calls noted from review: the nullable storage-projection component acts as an implicit two-mode switch on `RepositoryOwnershipGuard` (kept: both modes are exercised by Task/Classroom vs Student/ClassSession factories); test-fixture repetition mirrors existing file-convention fixtures; inline fully-qualified names cleaned up to imports after review.
