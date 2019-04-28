package com.pajato.tmdb.client

import com.pajato.tmdb.core.Movie
import com.pajato.tmdb.core.TmdbData
import com.pajato.tmdb.core.getListName
import io.javalin.Context
import io.javalin.Javalin
import org.junit.Test
import java.io.File
import java.net.URL
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val ARG_LIST_NAME = "list"
private const val ARG_START = "start"
private const val ARG_SIZE = "size"
private const val ARG_PAGE = "page"

class LibraryTestJVM {
    var app: Javalin? = null

    @BeforeTest
    fun setup() {
        if (app == null) {
            app = Javalin.create().apply {
                start(7000).apply {
                    get("/") { ctx -> ctx.result("Hello World") }
                    get("/page/:$ARG_LIST_NAME/:$ARG_START/:$ARG_SIZE/:$ARG_PAGE") { ctx ->
                        ctx.result(getPageFromContext(ctx))
                    }
                }
            }
        }
    }

    @AfterTest
    fun teardown() {
        app?.stop()
    }

    private val resourceDir by lazy { getFilesDir() }
    private val cache: Map<String, List<String>> = loadCacheFromFiles()

    private fun loadCacheFromFiles(): Map<String, List<String>> {
        fun getPair(kclass: KClass<out TmdbData>): Pair<String, List<String>> {
            val listName = kclass.getListName()
            val file = File(resourceDir, "$listName.json")

            return if (!file.exists() || !file.isFile) listName to listOf() else listName to file.readLines()
        }

        return TmdbData::class.sealedSubclasses
            .filter { kClass ->  kClass.getListName() != "" }
            .map { kClass -> getPair(kClass) }.toMap()
    }

    private fun getFilesDir(): String =
        "${File(this::class.java.classLoader.getResource("").path).parent}/test"

    private fun getPageFromContext(ctx: Context): String {
        fun getPageFromParams(listName: String, start: Int, pageSize: Int, page: Int): String {
            fun getPageRecords(startIndex: Int, endIndex: Int): String {
                val list: List<String> = cache[listName] ?: listOf()
                val result = StringBuilder()

                for (index in startIndex .. endIndex) {
                    if (index >= 0 && index < list.size) result.append("${list[index]}\n")
                }

                return result.toString()
            }

            return when {
                page > 0 -> getPageRecords(start + 1, start + pageSize)
                page < 0 -> getPageRecords(start - pageSize, start - 1)
                else -> getPageRecords(0, pageSize - 1)
            }
        }
        val listName = ctx.pathParam(ARG_LIST_NAME)
        val start = ctx.pathParam(ARG_START).toInt()
        val pageSize = ctx.pathParam(ARG_SIZE).toInt()
        val page = ctx.pathParam(ARG_PAGE).toInt()

        return getPageFromParams(listName, start, pageSize, page)
    }

    @Test fun `when the Javalin server is activated verify hello world works`() {
        val expectedSize = 1
        val expectedContent = "Hello World"
        val content = URL("http://localhost:7000/").openConnection().getInputStream().bufferedReader().readLines()
        //assertTrue(app.toString().isNotEmpty())
        assertEquals(expectedSize, content.size, "Wrong number or lines!")
        assertEquals(expectedContent, content[0], "Wrong content!")
    }

    @Test fun `when the first page is requested with valid data do the right thing`() {
        val type = Movie::class
        val pageSize = 25
        val uut = getFirstPage(type, pageSize)
        assertEquals(type, uut.type, "The list name property is wrong!")
        assertEquals(pageSize, uut.pageSize, "The page size property is wrong!")
        assertEquals(pageSize, uut.list.size, "The list size is wrong.")
        assertTrue(uut.list[0] is Movie, "A type other than TmdbError was found!")
    }

}
