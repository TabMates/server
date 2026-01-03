package de.tabmates.server.user.infra.ratelimiting

import de.tabmates.server.user.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.lang.Exception
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig,
) {
    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers: List<IpAddressMatcher> =
        nginxConfig
            .trustedIps
            .filter { it.isNotBlank() }
            .map { proxy ->
                val cidr =
                    when {
                        proxy.contains("/") -> proxy
                        proxy.contains(":") -> "$proxy/128"
                        else -> "$proxy/32"
                    }
                IpAddressMatcher(cidr)
            }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { matcher -> matcher.matches(ip) }
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher -> matcher.matches(ip) }
    }

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        if (!isFromTrustedProxy(remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }

            return remoteAddr
        }

        // Try X-Forwarded-For first (used by Traefik), then X-Real-IP (used by Nginx)
        val clientIp =
            extractFromXForwardedFor(request, remoteAddr)
                ?: extractFromXRealIp(request, remoteAddr)

        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers from $remoteAddr")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }

        return clientIp ?: remoteAddr
    }

    private fun extractFromXForwardedFor(
        request: HttpServletRequest,
        proxyIp: String,
    ): String? {
        return request.getHeader("X-Forwarded-For")?.let { header ->
            // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2
            // We want the first (leftmost) IP which is the original client
            val firstIp = header.split(",").firstOrNull()?.trim()
            firstIp?.let {
                validateAndNormalizeIp(it, "X-Forwarded-For", proxyIp)
            }
        }
    }

    private fun extractFromXRealIp(
        request: HttpServletRequest,
        proxyIp: String,
    ): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        }
    }

    private fun validateAndNormalizeIp(
        ip: String,
        headerName: String,
        proxyIp: String,
    ): String? {
        val trimmedIp = ip.trim()

        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp.lowercase())) {
            logger.debug("Invalid IP in $headerName: $trimmedIp from proxy $proxyIp")
        }

        return try {
            val inetAddr =
                when {
                    trimmedIp.contains(":") -> {
                        Inet6Address.getByName(trimmedIp)
                    }

                    trimmedIp.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")) -> {
                        Inet4Address.getByName(trimmedIp)
                    }

                    else -> {
                        logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                        return null
                    }
                }

            if (isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddr.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    companion object {
        private val PRIVATE_IP_RANGES =
            listOf(
                "10.0.0.0/8",
                "172.16.0.0/12",
                "192.168.0.0/16",
                "127.0.0.0/8",
                "::1/128",
                "fc00::/7",
                "fe80::/10",
            ).map { IpAddressMatcher(it) }

        private val INVALID_IPS =
            listOf(
                "unknown",
                "unavailable",
                "0.0.0.0",
                "::",
            )
    }
}
