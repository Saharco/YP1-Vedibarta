package com.technion.vedibarta.utilities.resourcesManagement

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class FileResourceTest {
    private lateinit var file: File

    @Before
    fun createFiles() {
        file = File.createTempFile("user-1", null)

        file.writeText("""
            Hello
            World
            Goodbye
            Moon
        """.trimIndent())
    }

    @After
    fun deleteFiles() {
        file.delete()
    }

    @Test
    fun `getAll should return the content of userFile`() {
        val resource = FileTextResource(file)

        val result = resource.getAll()

        assertEquals(result, listOf("Hello", "World", "Goodbye", "Moon"))
    }
}