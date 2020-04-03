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
        val resource = FileResource(file)

        val result = resource.getAll()

        assertEquals(result, listOf("Hello", "World", "Goodbye", "Moon"))
    }

    @Test
    fun `assert close calls a listener`() {
        var called = false

        val resource = FileResource(file).apply {
            addOnCloseListener { called = true }
        }

        resource.close()

        assert(called)
    }

    @Test
    fun `assert close calls multiple listeners`() {
        var called1 = false
        var called2 = false
        var called3 = false

        val resource = FileResource(file).apply {
            addOnCloseListener { called1 = true }
            addOnCloseListener { called2 = true }
            addOnCloseListener { called3 = true }
        }

        resource.close()

        assert(called1 && called2 && called3)
    }
}