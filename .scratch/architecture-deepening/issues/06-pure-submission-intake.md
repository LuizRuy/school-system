# 06: Pure submission intake

**What to build:** Recording task submissions for a class resolves all students in one query instead of one per row, and the submission mapper becomes pure — it receives already-resolved students and can no longer trigger database access or authorization checks as a hidden side effect of "mapping". The single-submission path gains the same transactional protection as the bulk path, and duplicate submissions are rejected on insert, not just detected on update.

**Blocked by:** 04 (finish guard migration).

**Status:** resolved

- [x] Recording submissions for N students issues a constant number of queries (batched child resolution), not N
- [x] The submission mapper performs no I/O and no authorization; it maps resolved entities to entities/DTOs only
- [x] Single-submission recording is transactional like bulk recording
- [x] A second submission for the same student+task upserts instead of duplicating
- [x] Tests cover: batch intake with mixed submitted states, duplicate rejection, mapper purity (no collaborator mocking required to test it)

## Comments

**Review (post-implementation, two axes):**

- Spec: faithful. Bulk intake resolves students through `StudentService.resolveAll` — one `findAllById` query for the whole roster, missing ids surfaced as not-found and foreign ids as 403 before any write (`SubmissionIntakePersistenceTest.batchWithUnknownStudentLeavesNoPartialRows` proves zero partial rows over a real H2 transaction). Existing rows for the task are then loaded in one `findByTask` and diffed in memory, so a whole-class recording costs a constant number of reads regardless of N; already-submitted students flip in place and only fresh rows go through `saveAll`. The mapper holds no fields and no collaborators at all — pinned structurally by `mapperNeedsNoCollaborators` asserting zero declared fields — so it cannot perform I/O or authorization; tests construct it directly with nothing mocked. Single path is `@Transactional` like bulk (reflection-pinned plus behavioural upsert proof in `SubmissionIntakePersistenceTest.singleReRecordingPersistsAsUpdateOfSameRow`). Duplicates: happy path upserts per bullet 4, while `uk_submission_student_task` backstops concurrent inserts into a 409 via the existing `GlobalExceptionHandler` mapping.
- Spec (judgement calls): "constant number of queries" read as O(1) *reads* (resolution + existence diff), not counting writes — a DB-native merge was judged out of proportion, matching ticket 05's precedent. The unique constraint goes beyond bullet 4's letter, mirroring `uk_attendance_student_session`; same caveat applies: `ddl-auto=update` may fail loudly on startup if a dev database already holds duplicate student+task rows, and no migration tooling exists in the repo to dedupe first. Update path (`PATCH`) was switched from fetch-for-side-effect calls to guard projections (`authorize`), extending ticket 05's anti-pattern cleanup to submissions; contract unchanged (404 on missing row). Batch resolution lives on `StudentService` rather than the ownership seam: it re-uses the guard's exact error vocabulary but hard-codes the FORBIDDEN mode configured for students in `OwnershipGuards.forStudents` — a batch-aware `resolveAll` on `OwnershipGuard` would own this policy properly if another aggregate ever needs batched children.
- Standards: no violations against documented standards (none exist) or the attendance precedent. Judgement calls noted from review: test-fixture repetition across the three new files mirrors existing file-convention fixtures; mapper's defensive null-student branch throws the house `EntityNotFoundException` after review flagged an earlier `IllegalArgumentException`.
