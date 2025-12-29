package com.ravi.mylivetv.utils

object UrlBuilder {

    private const val BASE = "https://iptv-org.github.io/iptv"

    fun categories(category: String) =
        "$BASE/categories/${category.lowercase()}.m3u"

    fun languages(code: String) =
        "$BASE/languages/${code.lowercase()}.m3u"

    fun countries(code: String) =
        "$BASE/countries/${code.lowercase()}.m3u"
}
