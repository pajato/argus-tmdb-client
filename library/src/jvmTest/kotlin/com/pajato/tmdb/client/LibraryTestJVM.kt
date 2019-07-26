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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private const val ARG_LIST_NAME = "list"
private const val ARG_START = "start"
private const val ARG_SIZE = "size"

class LibraryTestJVM {
    private var app: Javalin? = null
    private val port = 7000
    private val baseUrl = "http://localhost:$port"

    @BeforeTest
    fun setup() {
        fun getPageFromContext(ctx: Context): String {
            fun getPageFromParams(listName: String, start: Int, pageSize: Int): String {
                fun getPageRecords(startIndex: Int, endIndex: Int): String {
                    val list: List<String> = cache[listName] ?: listOf()
                    val result = StringBuilder()

                    for (index in startIndex .. endIndex) {
                        if (index >= 0 && index < list.size) result.append("${list[index]}\n")
                    }

                    return result.toString()
                }

                return getPageRecords(start, start + pageSize - 1)
            }
            val listName = ctx.pathParam(ARG_LIST_NAME)
            val start = ctx.pathParam(ARG_START).toInt()
            val pageSize = ctx.pathParam(ARG_SIZE).toInt()

            return getPageFromParams(listName, start, pageSize)
        }

        if (app == null) {
            app = Javalin.create().apply {
                start(port).apply {
                    get("/") { ctx -> ctx.result("Hello World") }
                    get("/page/:$ARG_LIST_NAME/:$ARG_START/:$ARG_SIZE") { ctx ->
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
    private val cache: Map<String, List<String>> by lazy {
        fun getPair(kclass: KClass<out TmdbData>): Pair<String, List<String>> {
            val listName = kclass.getListName()
            val file = File(resourceDir, "$listName.json")

            return if (!file.exists() || !file.isFile) listName to listOf() else listName to file.readLines()
        }

        TmdbData::class.sealedSubclasses
            .filter { kClass ->  kClass.getListName() != "" }
            .map { kClass -> getPair(kClass) }.toMap()
    }

    private fun getFilesDir(): String =
        "${File(this::class.java.classLoader.getResource("").path).parent}/test"


    @Test fun `when the Javalin server is activated verify hello world works`() {
        val expectedSize = 1
        val expectedContent = "Hello World"
        val content = URL(baseUrl).openConnection().getInputStream().reader().readLines()
        assertNotEquals(null, app, "The app is not initialized!")
        assertEquals(expectedSize, content.size, "Wrong number or lines!")
        assertEquals(expectedContent, content[0], "Wrong content!")
    }

    @Test fun `when the first page is requested with valid data do the right thing`() {
        val type = Movie::class
        val pageSize = 25
        val uut = getFirstPage(type, pageSize)
        val expectedId = 601
        assertEquals(type, uut.type, "The list name property is wrong!")
        assertEquals(pageSize, uut.pageSize, "The page size property is wrong!")
        assertEquals(pageSize, uut.list.size, "The list size is wrong.")
        assertTrue(uut.list[0] is Movie, "A type other than TmdbError was found!")
        assertEquals(expectedId, (uut.list[0] as Movie).id, "Wrong id detected!")
    }

    @Test fun `when the next page is requested verify correct results`() {
        val type = Movie::class
        val pageSize = 25
        val page0 = getFirstPage(type, pageSize, baseUrl)
        val uut = getNextPage(type, pageSize, page0)
        val expectedId = 628
        assertEquals(type, uut.type, "The list name property is wrong!")
        assertEquals(pageSize, uut.pageSize, "The page size property is wrong!")
        assertEquals(pageSize, uut.list.size, "The list size is wrong.")
        val typeName = uut.list[0].javaClass.simpleName
        assertTrue(uut.list[0] is Movie, "A type other than Movie was found: $typeName")
        assertEquals(expectedId, (uut.list[0] as Movie).id, "Wrong id detected!:\n${uut.list}")
    }

    @Test
    fun `when the previous page is requested verify correct results`() {
        val type = Movie::class
        val pageSize = 25
        val page0 = getFirstPage(type, pageSize)
        val page1 = getNextPage(type, pageSize, page0)
        val uut = getPrevPage(type, pageSize, page1)
        val expectedId = 601
        assertEquals(type, uut.type, "The list name property is wrong!")
        assertEquals(pageSize, uut.pageSize, "The page size property is wrong!")
        assertEquals(pageSize, uut.list.size, "The list size is wrong.")
        assertTrue(uut.list[0] is Movie, "A type other than TmdbError was found!")
        assertEquals(expectedId, (uut.list[0] as Movie).id, "Wrong id detected!:\n${uut.list}")
    }
}
