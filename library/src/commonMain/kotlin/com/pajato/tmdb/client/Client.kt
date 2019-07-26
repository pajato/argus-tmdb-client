package com.pajato.tmdb.client

import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.TmdbError
import com.pajato.tmdb.core.getListName
import kotlin.reflect.KClass

internal expect fun getPageList(listName: String, startRecord: Int, pageSize: Int, baseUrl: String): List<TmdbData>

const val DEFAULT_URL = "https://tmdb.pajato.com"

/** The page wrapper class. */
class Page<T1 : TmdbData> (
    val type: KClass<T1>,
    val startIndex: Int,
    val pageSize: Int,
    val list: List<TmdbData>,
    val baseUrl: String
) {
    fun hasError(): Boolean = list.size == 1 && list[0] is TmdbError
    fun getError(): String {
        fun getErrorMessage(item: TmdbData): String = when (item) {
            is TmdbError -> item.message
            else -> ""
        }

        return if (list.isEmpty()) "" else getErrorMessage(list[0])
    }
}

/** Access the reference (first) page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1 : TmdbData> getFirstPage(type: KClass<T1>, pageSize: Int, baseUrl: String = DEFAULT_URL): Page<T1> {
    return getPage(type, 0, pageSize, baseUrl)
}

/** Access the next page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getNextPage(type: KClass<T1>, pageSize: Int, page: Page<T1>): Page<T1> {
    return getPage(type, page.startIndex + page.pageSize, pageSize, page.baseUrl)
}

/** Access the previous page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getPrevPage(type: KClass<T1>, pageSize: Int, page: Page<T1>): Page<T1> {
    return getPage(type, page.startIndex - page.pageSize, pageSize, page.baseUrl)
}

internal fun <T1 : TmdbData> getPage(type: KClass<T1>, startIndex: Int, pageSize: Int, baseUrl: String): Page<T1> {
    val listName = type.getListName()
    val list: List<TmdbData> = getPageList(listName, startIndex, pageSize, baseUrl)
    return Page(type, startIndex, pageSize, list, baseUrl)
}
