package com.pajato.tmdb.client

import com.pajato.tmdb.core.Page
import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.getListName
import kotlin.reflect.KClass

expect fun getPage(listName: String, startRecord: Int, pageSize: Int, page: Int): List<TmdbData>

/** Access the reference (first) page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1 : TmdbData> getFirstPage(type: KClass<T1>, pageSize: Int): Page<T1, TmdbData> {
    val listName = type.getListName()
    val list: List<TmdbData> = getPage(listName, 0, pageSize, 0)
    return getPage(type, pageSize, list)
}

internal fun <T1 : TmdbData> getPage(type: KClass<T1>, pageSize: Int, list: List<TmdbData>): Page<T1, TmdbData> {
    return Page(type, pageSize, list)
}

/** Access the next page returning a TmdbData object of type T1 or TmdbError.*/
//fun <T1: TmdbData, T2: TmdbData> getNextPage(type: KClass<T1>, pageSize: Int, Page<T1>): Page

/** Access the previous page returning a TmdbData object of type T1 or TmdbError.*/
//fun <T1: TmdbData, T2: TmdbData> getPrevPage(type: KClass<T1>, pageSize: Int, Page<T1>): Page<T2>
