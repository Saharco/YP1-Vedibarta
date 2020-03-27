package com.technion.vedibarta.utilities.resourcesManagement

import org.junit.Test

import org.junit.Assert.*

class PreferencesResolverTest {
    private val preferences = mapOf<String, String>(
        "Lang" to "HE",
        "Sex" to "MALE",
        "Shape" to "CIRCLE"
    )

    @Test
    fun `resolver picks only option - no preferences`() {
        val possibilities = setOf("resource")

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource", result)
    }

    @Test
    fun `resolver picks only option - unset preference`() {
        val possibilities = setOf("resource-Color=RED")

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Color=RED", result)
    }

    @Test(expected = PreferencesResolver.NoMatchException::class)
    fun `resolver throws when no match 1`() {
        val possibilities = setOf("resource-Lang=EN")

        PreferencesResolver(preferences).resolve(possibilities)
    }

    @Test(expected = PreferencesResolver.NoMatchException::class)
    fun `resolver throws when no match 2`() {
        val possibilities = setOf("resource-Lang=HE-Sex=FEMALE")

        PreferencesResolver(preferences).resolve(possibilities)
    }

    @Test
    fun `resolver picks only matching option 1`() {
        val possibilities = setOf(
            "resource",
            "resource-Lang=EN"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource", result)
    }

    @Test
    fun `resolver picks only matching option 2`() {
        val possibilities = setOf(
            "resource-Lang=EN",
            "resource-Sex=MALE"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Sex=MALE", result)
    }

    @Test
    fun `resolver picks only matching option 3`() {
        val possibilities = setOf(
            "resource-Sex=MALE-Lang=EN",
            "resource-Sex=MALE-Lang=HE"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Sex=MALE-Lang=HE", result)
    }

    @Test
    fun `resolver picks best match 1`() {
        val possibilities = setOf(
            "resource-Sex=MALE-Lang=HE",
            "resource",
            "resource-Lang=HE"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Sex=MALE-Lang=HE", result)
    }

    @Test
    fun `resolver picks least specific best match`() {
        val possibilities = setOf(
            "resource-Sex=MALE-Lang=HE-Color=RED",
            "resource",
            "resource-Sex=MALE-Lang=EN",
            "resource-Sex=MALE-Lang=HE",
            "resource-Lang=HE"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Sex=MALE-Lang=HE", result)
    }

    @Test
    fun `resolves works with ordered collections`() {
        val possibilities = listOf(
            "resource-Sex=MALE-Lang=HE-Color=RED",
            "resource",
            "resource-Sex=MALE-Lang=EN",
            "resource-Sex=MALE-Lang=HE",
            "resource-Lang=HE"
        )

        val result = PreferencesResolver(preferences).resolve(possibilities)

        assertEquals("resource-Sex=MALE-Lang=HE", result)
    }
}