package com.pajato.tmdb.client

import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.toTmdbData
import java.net.URL

internal actual fun getPageList(listName: String, startRecord: Int, pageSize: Int, baseUrl: String): List<TmdbData> {
    val result = mutableListOf<TmdbData>()
    val url = "$baseUrl/page/$listName/$startRecord/$pageSize"
    URL(url).openConnection().getInputStream().reader().readLines().forEach { result.add(it.toTmdbData(listName)) }
    return result
}