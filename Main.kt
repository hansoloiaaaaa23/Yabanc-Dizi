package com.lagradost.cloudstream3.plugins

import com.lagradost.cloudstream3.*

class YabanciDiziProvider : MainAPI() {
    override var mainUrl = "https://yabancidizi.tv"
    override var name = "Yabancı Dizi"
    override val supportedTypes = setOf(TvType.TvSeries)
    override var lang = "tr"

    // Arama işlevi
    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("$mainUrl/ara?q=$query").document
        return doc.select(".dizi-card").mapNotNull {
            val title = it.selectFirst(".dizi-title")?.text() ?: return@mapNotNull null
            val url = fixUrl(it.selectFirst("a")?.attr("href") ?: return@mapNotNull null)
            val poster = fixUrl(it.selectFirst("img")?.attr("data-src") ?: "")
            TvSeriesSearchResponse(title, url, this.name, poster)
        }
    }

    // Bölüm detayları
    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text() ?: return null
        val episodes = doc.select(".episode-list a").map {
            val epTitle = it.text()
            val epUrl = fixUrl(it.attr("href"))
            Episode(epUrl, epTitle)
        }
        return TvSeriesLoadResponse(title, url, this.name, episodes)
    }
}
