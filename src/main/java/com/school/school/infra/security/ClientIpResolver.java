package com.school.school.infra.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Decides which address identifies a rate-limited client.
 *
 * <p><strong>Decision (ticket 08):</strong> forwarded headers are never trusted
 * blindly. A client can send any {@code X-Forwarded-For}/{@code X-Real-IP} value
 * it likes, so a single spoofable header must not decide the bucket.
 *
 * <ul>
 *   <li>{@code trusted-proxies = 0} (default): only the TCP peer address
 *       ({@code request.getRemoteAddr()}) is used; all forwarded headers are
 *       ignored. Direct exposure or no proxy-based limiting.</li>
 *   <li>{@code trusted-proxies = N &gt; 0}: the app sits behind exactly N reverse
 *       proxies that <em>append</em> to {@code X-Forwarded-For}. The entry
 *       appended by your innermost trusted proxy's view — N hops from the right
 *       of the chain — is taken; anything further left was client-supplied and
 *       may be forged. A missing header, or fewer entries than trusted proxies,
 *       falls back to the TCP peer address.</li>
 * </ul>
 *
 * <p>{@code X-Real-IP} is deliberately never consulted: it is non-standard and
 * carries no stronger guarantee than {@code X-Forwarded-For}.
 */
@Component
public class ClientIpResolver {

    private final int trustedProxies;

    public ClientIpResolver(OpenEndpointsProperties properties) {
        this.trustedProxies = properties.getClientIp().getTrustedProxies();
    }

    public String resolve(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (trustedProxies <= 0) {
            return remoteAddr;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remoteAddr;
        }

        String[] hops = forwardedFor.split(",");
        int index = hops.length - trustedProxies;
        if (index < 0) {
            return remoteAddr;
        }

        String candidate = hops[index].trim();
        return candidate.isEmpty() ? remoteAddr : candidate;
    }
}
