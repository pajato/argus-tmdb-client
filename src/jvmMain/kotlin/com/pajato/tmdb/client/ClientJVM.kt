package com.pajato.tmdb.client

import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.toTmdbData
import java.net.URL

actual fun getPage(listName: String, startRecord: Int, pageSize: Int, page: Int): List<TmdbData> {
    val result = mutableListOf<TmdbData>()
    val url = "http://localhost:7000/page/$listName/$startRecord/$pageSize/$page"
    URL(url).openConnection().getInputStream().bufferedReader().readLines().forEach {
        result.add(it.toTmdbData(listName))
    }
    return result
}