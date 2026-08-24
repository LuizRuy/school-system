# 03: Ownership guard + first adapters

**What to build:** One ownership guard: a single interface that, given a target id and the current principal, resolves the entity or fails — hiding fetch-or-404, the owner comparison, and whether a foreign entity surfaces as 403 or 404 behind one seam. The Task and Classroom flows adopt it first. The rule "a user can only touch their own resources" becomes one testable fact instead of four divergent copies (ClassSession currently masks existence with 404 while Task/Student/Classroom leak it with 403).

**Blocked by:** None (can start immediately).

**Status:** resolved

- [x] A shared ownership-guard interface exists with an explicit error-mode policy
- [x] Task fetch/update/delete paths resolve entities only through the guard; the inline copy is deleted
- [x] Same for Classroom fetch/update/delete/add-students paths
- [x] Tests through the guard's interface: owner passes, foreign entity denied, missing entity not-found — no full-application context needed
- [x] Behaviour preserved exactly for Task/Classroom (same statuses clients see today)

## Comments

**Review (post-implementation, two axes):**

- Spec: faithful. Note: TaskService has no update path at all, so "update paths through the guard" holds vacuously for Task (fetch/delete demonstrated); Classroom covers fetch/update/delete/add-students fully. Exception types, exact messages, and 403/404 mappings verified byte-identical to the deleted inline copies. SubmissionService call sites unaffected (signature unchanged).
- Standards (all judgement calls): `NOT_FOUND` mode ships without a production caller — kept because the spec requires an explicit error-mode policy and ClassSession adoption (07) will consume it. `forTasks`/`forClassrooms` share a shape differing in three literals — kept as named compositions for readability. StudentService's inline copy remains untouched, as scoped for a later ticket.
