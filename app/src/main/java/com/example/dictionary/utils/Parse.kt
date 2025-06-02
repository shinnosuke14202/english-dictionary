package com.example.dictionary.utils

import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.InputStream

fun Response.parseHtml() : Document = use { response ->
    val inputStream: InputStream = response.body.byteStream()
    val charsetName: String = response.body.contentType()?.charset().toString()
    val baseUrl: String = response.request.url.toString()
    return Jsoup.parse(inputStream, charsetName, baseUrl)
}
