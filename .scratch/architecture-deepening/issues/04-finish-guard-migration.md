# 04: Finish guard migration

**What to build:** The remaining resource flows — Student and Class Session — resolve entities only through the ownership guard, so the rule has exactly one implementation in the codebase. ClassSession's 404-masking stops being an inline anomaly and becomes a named policy decision of the guard (deliberate IDOR masking or not, it's now written once where future readers will find it).

**Blocked by:** 03 (ownership guard + first adapters).

**Status:** resolved

- [x] Student fetch/update/delete paths resolve entities only through the guard
- [x] ClassSession fetch/update/delete paths resolve entities only through the guard
- [x] All four inline fetch-and-check copies are deleted; grep finds no hand-rolled owner comparison outside the guard
- [x] The foreign-entity error mode for each resource type is asserted in tests, making the 403-vs-404 divergence a visible, intentional choice

## Comments

**Review (post-implementation, two axes):**

- Spec: faithful. Note: ClassSessionService has no update path at all (no update method or controller PUT/PATCH), so "update paths through the guard" holds vacuously for ClassSession, mirroring the Task situation recorded in 03. Student keeps FORBIDDEN (403); ClassSession keeps NOT_FOUND masking, now asserted by name ("deliberately masks a foreign class session as 404"). Call sites in Attendance/Submission/Classroom services unaffected (signatures unchanged).
- Standards: no hard violations. Test-fixture repetition mirrors the existing task/classroom tests verbatim (file-convention consistency). The `(finder, ownerIdOf, entityType, errorMode)` clump is pre-existing shape, extended not worsened.
- Judgement call: the missing-student message is reworded by the guard's uniform format — `"Student with id 42 not found"` became `"Student not found with id: 42"`. Status codes are byte-identical everywhere (404 missing / 403 foreign for Student, 404 both cases for ClassSession); preserving the old wording would have required a per-entity message-template hook, fragmenting the seam this ticket exists to unify. Kept uniform, asserted in OwnershipGuardsTest.
