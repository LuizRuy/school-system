# 08: Open endpoints catalog

**What to build:** The set of public (unauthenticated) endpoints is defined in exactly one place, consumed by both the security configuration and the rate-limit filter, so adding a public endpoint means editing one list. Rate limits become configuration rather than compiled constants, and window expiry is unit-tested against the injected Clock.

**Blocked by:** 02 (auditing owns timestamps; Clock seam).

**Status:** done (commit e27abdb)

- [x] One catalog lists every open endpoint and its rate limit; both security config and rate filter derive their behaviour from it
- [x] Adding an entry to the catalog makes the endpoint public and rate-limited with no other edit
- [x] Limits configurable via application properties
- [x] Window expiry proven by tests using a fixed Clock (request → limit hit → time advances → allowed again), no real waiting
- [x] Client IP resolution documented/decided: no blind trust of a single spoofable forwarded header

## Comments

- The one list is `OpenEndpoints.DEFINITIONS` (`infra/security/OpenEndpoints.java`): name, HTTP method (null = any), path pattern, default limit, default window. `OpenEndpointsCatalog` merges it with `OpenEndpointsProperties` (`school.open-endpoints.*`) into effective rules with precompiled `PathPattern`s; unknown override names fail fast at startup.
- Consumers: `SecurityConfig.permitOpenEndpoints` emits the anonymous matchers from the catalog; `OpenEndpointRateLimitFilter` looks its rule up via `findRule(method, uri)` and takes `widestWindow()` for cleanup. Adding an entry to `DEFINITIONS` is the only edit needed.
- Catalog now also covers the swagger/api-docs paths (300/min default), so every public endpoint has a rate limit. Auth endpoints are now POST-only public — GET on them previously slipped past permitAll as a 405 anyway.
- Overrides: `school.open-endpoints.limits.<name>.limit` / `.window` (e.g. `PT2M`). Documented in `application.properties`.
- Client IP decision: forwarded headers are never blindly trusted (`ClientIpResolver`, documented in class javadoc + properties). Default `trusted-proxies=0` uses only the TCP peer address and ignores `X-Forwarded-For`/`X-Real-IP`; setting N trusts exactly N appending reverse proxies and takes the hop N from the right of the chain, falling back to the peer address when the header is missing or too short.
- Window expiry proven by `OpenEndpointRateLimitFilterTest.advancingClockReopensWindow` against a fixed `MutableClock`; spoof-splitting covered by `spoofedForwardedHeaderDoesNotSplitBuckets`. End-to-end promise proven by `OpenEndpointCatalogWiringTest`: a synthetic entry added only to the catalog comes out public (200) then rate-limited (429) through the real `SecurityConfig`, while uncatalogued paths stay 401.
- Property binding proven end-to-end by `OpenEndpointsPropertiesBindingTest` (`@SpringBootTest`): `school.open-endpoints.limits.login.limit/window` reach the catalog's rules and `client-ip.trusted-proxies` drives the resolver.
- Review follow-ups: shared `SecurityHandlersSliceConfig`/`OpenEndpointsSliceConfig` replace four copies of slice-bean boilerplate; spoof test renamed to state exactly what count-based trust proves (`forgedPrefixBeyondTrustedHopCountIsIgnored`).
