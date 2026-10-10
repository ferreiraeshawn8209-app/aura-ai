package com.aura.ai.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/** Applied to each network hop, including redirects. Public servers must use HTTPS. */
class LocalHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        if (url.scheme == "http" && !LocalHttpPolicy.isLocalHost(url.host)) {
            throw IOException("HTTP is supported only for a private LAN IP or loopback. Use HTTPS for a public server.")
        }
        return chain.proceed(chain.request())
    }
}
