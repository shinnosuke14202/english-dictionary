package com.example.dictionary.network

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

        val ids = listOf("cald4", "cbed", "cacd")
        val nextBody = listOf(".pr.entry-body__el", ".pr.di.superentry")

        val wordElements = ids.asSequence().mapNotNull { id ->
                document.select(".page .pr.dictionary[data-id=$id]").takeIf { it.isNotEmpty() }
            }.map { dictElement ->
                nextBody.asSequence().map { body -> dictElement.select(body) }
                    .firstOrNull { it.isNotEmpty() } ?: dictElement
            }.firstOrNull() ?: Elements()

        if (wordElements.isNotEmpty()) {
            for (e in wordElements) {
                val title = e.select(".di-title .headword").text().trim()

                val type =
                    e.select(".posgram .pos.dpos, .di-info .pos.dpos").eachText().joinToString(", ")

                val ipa = e.select(".uk.dpron-i .pron.dpron").text().trim()
                val soundUrl = e.select(".uk.dpron-i source[type=audio/mpeg]").attr("src").let {
                    if (it.startsWith("/")) BASE_URL + it else it
                }

                val wordBody: Element? = e.selectFirst(".pos-body .pr.dsense .def-block")
                    ?: e.selectFirst(".idiom-body.didiom-body")

                val meaning: String? = wordBody?.selectFirst(".def.ddef_d, .def.ddef_d.db")?.text()

                val examples = wordBody?.select(".examp.dexamp .eg")?.map { it.text() }
                    ?.toCollection(ArrayList()) ?: arrayListOf()

                val newWord = Word(
                    id = 0,
                    title = title,
                    type = type,
                    ipa = ipa,
                    soundUrl = soundUrl,
                    meaning = meaning ?: "No meaning found!",
                    examples = examples
                )

                results.add(newWord)
            }
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
