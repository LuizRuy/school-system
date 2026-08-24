# 07: Token lifecycle has one owner

**What to build:** The refresh-token lifecycle lives behind one interface — issue a token pair, rotate on refresh, validate a raw token — with a single owner for reads and writes. Login performs one user lookup instead of three, and "a new login invalidates the previous refresh token" becomes a named, tested invariant rather than a hidden side effect of a create-shaped call. Expired vs invalid tokens are distinguishable and tested using the injected Clock. Dead security shims in the same area are deleted in passing.

**Blocked by:** 02 (auditing owns timestamps; Clock seam).

**Status:** resolved

- [x] Login issues access + refresh tokens through one interface; exactly one user query per login (asserted)
- [x] Refresh flow reads tokens only through the owning module — no direct repository reads from outside
- [x] Rotation invariant tested: using an old refresh token after re-login fails
- [x] Expired and malformed refresh tokens produce distinct, honest error responses; expiry tests run against a fixed Clock
- [x] Dead code removed: the unused static security utility and unreachable passthrough lookups are gone

## Comments

**Implementation:** New `TokenLifecycle` interface (`issuePair(User)` / `rotate(String)` / `validateAccess(String)`) with `RefreshTokenLifecycle` as the single owner of `RefreshTokenRepository` — grep-verified that no production code outside `infra.security.RefreshTokenLifecycle` touches it; `JwtUtil` is now an internal collaborator of the owner (the token filter reaches claims through `validateAccess`). The named invariant lives in `freshPairFor`/`revokeActiveTokens`: both issuing paths revoke all of the user's active refresh tokens before minting the new one, so re-login *and* rotation invalidate every previously issued refresh token. Login's three lookups collapsed to one by having `CustomUserDetailsService` return `UserAuthenticated` wrapping the domain `User` (the principal carries the entity out of the authentication manager) and having `issuePair` accept the already-resolved user instead of an email to re-query.

**Tests:** `LoginSingleUserQueryTest` runs real wiring over H2 (DaoAuthenticationProvider + CustomUserDetailsService + RefreshTokenLifecycle) behind a `MockitoSpyBean` on `UserRepository`, asserting exactly one `findByEmail` per login — including the disabled-user rejection path; mutation-checked (reverting the details service to rebuild-style principals turns the test red). `RefreshTokenLifecycleRotationTest` proves over real rows: issued tokens persist with Clock-based expiry, re-login kills the previous refresh token, rotated tokens cannot be reused, and advancing the fixed Clock past expiry fails as expired and deletes the row. `AuthControllerErrorTest` pins the HTTP contract: unknown vs expired refresh tokens both answer 401 (matching how the access-token entry point treats bad credentials) with distinct messages ("Refresh token is invalid" / "Refresh token has expired. Please make a new signin request").

**Review (post-implementation, two axes):**

- Spec: faithful on all five bullets; single-owner claim verified structurally. Judgement calls: the web-slice error test stubs `AuthService`, so it proves the status/message contract rather than the full stack — the real unknown/expired flows are covered at the lifecycle seam over a real database; "no direct repository reads from outside" is enforced structurally (review-time grep), not by an architecture test — adding ArchUnit was judged out of proportion for this repo; login-time authorities are now empty on the `UserAuthenticated` principal, which is inert because login endpoints are permitAll and `TokenAuthenticationFilter` rebuilds authorities from JWT claims on every subsequent request.
- Known limitation (honest note): `rotate`'s find→verify→revoke sequence is not atomic, so two concurrent requests presenting the same refresh token could both pass validation before either revokes it. Same class of caveat as tickets 05/06's unique-constraint backstops: without migration tooling or locking policy in this repo, serializable behaviour was judged out of scope for this ticket.
- Standards: two findings applied — the repeated revoke-then-issue pair in `issuePair`/`rotate` extracted into `freshPairFor`, and `verifyNotExpired` renamed `deleteAndRejectIfExpired` so the name reveals its delete side effect. Remaining notes accepted as consistent-with-repo: one-handler-per-exception in `GlobalExceptionHandler`, cross-file test fixture repetition (matches existing file-convention fixtures), raw-string token values (matches `JwtUtil` style). Dead code removed in passing: `SecurityUtil`, `AuthService.findByEmail`, `UserService.findByEmail`, and the superseded `RefreshTokenService` + test.
