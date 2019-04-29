package com.pajato.tmdb.client

import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.TmdbError
import com.pajato.tmdb.core.getListName
import kotlin.reflect.KClass

internal expect fun getPageList(listName: String, startRecord: Int, pageSize: Int): List<TmdbData>

/** The page wrapper class. */
class Page<T1 : TmdbData, out T2 : TmdbData> (
    val type: KClass<T1>,
    val startIndex: Int,
    val pageSize: Int,
    val list: List<T2>
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
fun <T1 : TmdbData> getFirstPage(type: KClass<T1>, pageSize: Int): Page<T1, TmdbData> {
    return getPage(type, 0, pageSize)
}

/** Access the next page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getNextPage(type: KClass<T1>, pageSize: Int, page: Page<T1, TmdbData>): Page<T1, TmdbData> {
    return getPage(type, page.startIndex + page.pageSize, pageSize)
}

/** Access the previous page returning a TmdbData object of type T1 or TmdbError.*/
fun <T1: TmdbData> getPrevPage(type: KClass<T1>, pageSize: Int, page: Page<T1, TmdbData>): Page<T1, TmdbData> {
    return getPage(type, page.startIndex - page.pageSize, pageSize)
}

internal fun <T1 : TmdbData> getPage(type: KClass<T1>, startIndex: Int, pageSize: Int): Page<T1, TmdbData> {
    val listName = type.getListName()
    val list: List<TmdbData> = getPageList(listName, startIndex, pageSize)
    return Page(type, startIndex, pageSize, list)
}
