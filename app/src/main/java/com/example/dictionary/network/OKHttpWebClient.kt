package com.example.dictionary.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class OKHttpWebClient(
    private val httpClient: OkHttpClient
) {
    fun httpGet(url: String): Response {
        val request = Request.Builder().url(url).get().build()
        return httpClient.newCall(request).execute()
    }
}
