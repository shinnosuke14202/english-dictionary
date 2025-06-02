package com.example.dictionary.network

import android.util.Log
import com.example.dictionary.model.Word
import com.example.dictionary.utils.BASE_URL
import com.example.dictionary.utils.parseHtml
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

class DictionarySite {

    private val cookieJar: CookieJar = CookieJar.NO_COOKIES

    private val httpClient: OkHttpClient = OkHttpClient.Builder().cookieJar(cookieJar).build()

    private val webClient = OKHttpWebClient(httpClient)

    fun getWordMeaning(word: String): List<Word> {
        val results = ArrayList<Word>()

        val url = "$BASE_URL/dictionary/english/$word"

        val response: Response = webClient.httpGet(url)
        val document: Document = response.parseHtml()

        val wordElements: Elements =
            document.select(".page .pr.dictionary[data-id=cald4] .pr.entry-body__el")

        for (e in wordElements) {
            val title = e.select(".di-title .headword").text()
            val type = e.select(".posgram .pos.dpos").eachText().joinToString { "," }
            val soundUrl: String =
                BASE_URL + e.select(".uk.dpron-i source[type=audio/mpeg]").attr("src")
            val ipa = e.select(".uk.dpron-i .pron.dpron").text()

            val wordBody: Element? = e.select(".pos-body .pr.dsense .def-block").first()
            val meaning: String? = wordBody?.select(".def.ddef_d")?.text()

            val examples = ArrayList<String>()
            wordBody?.select(".examp.dexamp .eg")?.forEach { ex ->
                examples.add(ex.text())
            }

            val newWord = Word(
                title,
                type,
                ipa,
                soundUrl,
                meaning ?: "No meaning found!",
                examples
            )

            results.add(newWord)
        }
        if (results.isEmpty()) throw Exception("Word not found!")
        return results
    }

    fun getSearchSuggestions(word: String): List<String> {
        val results = ArrayList<String>()
        val url = "$BASE_URL/autocomplete/amp?dataset=english&q=$word"
        val response: Response = webClient.httpGet(url)
        val body: String = response.body.string()
        val jsonArray = JSONArray(body)
        for (i in 0..<jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            results.add(obj["word"].toString())
        }
        return results
    }
}
