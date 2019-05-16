/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.pajato.tmdb.client

import com.pajato.tmdb.core.Movie
import com.pajato.tmdb.core.TmdbError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibraryTest {

    @Test
    fun `when a page list has an error verify hasError and getError generate correct results`() {
        val errorMessage = "some error message"
        val uut = Page(Movie::class, 0, 25, listOf(TmdbError(errorMessage)))
        assertTrue(uut.hasError(), "No error is indicated!")
        assertEquals(errorMessage, uut.getError(), "Wrong error message!")
    }

    @Test
    fun `when a page list has no error and no result verify hasError and getError generate correct results`() {
        val errorMessage = ""
        val uut = Page(Movie::class, 0, 25, listOf())
        assertFalse(uut.hasError(), "No error is indicated!")
        assertEquals(errorMessage, uut.getError(), "Wrong error message!")
    }

    @Test
    fun `when a page list has no error and a single result verify hasError and getError generate correct results`() {
        val errorMessage = ""
        val uut = Page(Movie::class, 0, 25, listOf(Movie()))
        assertFalse(uut.hasError(), "No error is indicated!")
        assertEquals(errorMessage, uut.getError(), "Wrong error message!")
    }

}