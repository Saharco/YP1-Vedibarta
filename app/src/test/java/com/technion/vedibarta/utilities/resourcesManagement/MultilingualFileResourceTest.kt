package com.technion.vedibarta.utilities.resourcesManagement

import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import java.io.File

class MultilingualFileResourceTest {
    private lateinit var userFile: File
    private lateinit var baseFile: File

    @Before
    fun createFiles() {
        userFile = File.createTempFile("user-1", null)
        baseFile = File.createTempFile("base-1", null)

        userFile.writeText("""
            Hello
            World
            Goodbye
            Moon
        """.trimIndent())

        baseFile.writeText("""
            HELLO
            WORLD
            GOODBYE
            MOON
        """.trimIndent())
    }

    @After
    fun deleteFiles() {
        userFile.delete()
        baseFile.delete()
    }

    @Test
    fun `getAll should return the content of userFile`() {
        val resource = MultilingualFileResource(userFile, baseFile)

        val result = resource.getAll()

        assertEquals(result, listOf("Hello", "World", "Goodbye", "Moon"))
    }

    @Test
    fun `getAllBase should return the content of baseFile`() {
        val resource = MultilingualFileResource(userFile, baseFile)

        val result = resource.getAllBase()

        assertEquals(result, listOf("HELLO", "WORLD", "GOODBYE", "MOON"))
    }

    @Test
    fun `toCurrentLanguage translates correctly`() {
        val resource = MultilingualFileResource(userFile, baseFile)

        val result = resource.toCurrentLanguage("GOODBYE")

        assertEquals(result, "Goodbye")
    }

    @Test
    fun `toBaseLanguage translates correctly`() {
        val resource = MultilingualFileResource(userFile, baseFile)

        val result = resource.toBaseLanguage("Goodbye")

        assertEquals(result, "GOODBYE")
    }
}