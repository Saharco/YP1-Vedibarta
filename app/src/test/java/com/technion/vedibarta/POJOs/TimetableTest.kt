package com.technion.vedibarta.POJOs

import org.junit.Assert.*
import org.junit.Test
import com.technion.vedibarta.POJOs.Day.*
import com.technion.vedibarta.POJOs.Hour.*

class TimetableTest {

    @Test
    fun `timetable contains hour as specified in timetableOf`() {
        val timetable = timetableOf { on(SUNDAY) { at(FIRST) } }

        assert(SUNDAY at FIRST in timetable)
    }

    @Test
    fun `timetable contains multiple hours on same day as specified in timetableOf`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FIFTH)
            }
        }

        assert(SUNDAY at FIRST in timetable)
        assert(SUNDAY at FIFTH in timetable)
    }

    @Test
    fun `timetable contains multiple hours on different days specified in timetableOf`() {
        val timetable = timetableOf {
            on(MONDAY) {
                at(NINTH)
            }
            on(WEDNESDAY) {
                at(SIXTH)
                at(TENTH)
            }
        }

        assert(MONDAY at NINTH in timetable)
        assert(WEDNESDAY at SIXTH in timetable)
        assert(WEDNESDAY at TENTH in timetable)
    }

    @Test
    fun `timetable doesn't contain time not specified in timetableOf`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FOURTH)
                at(SEVENTH)
            }
            on(THURSDAY) {
                at(SECOND)
                at(THIRD)
            }
        }

        assert(SUNDAY at SECOND !in timetable)
    }

    @Test
    fun `timetable's iterator includes all and only times specified in timetableOf`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FOURTH)
                at(SEVENTH)
            }
            on(THURSDAY) {
                at(SECOND)
                at(THIRD)
            }
        }

        val expected = setOf(
            SUNDAY at FIRST,
            SUNDAY at FOURTH,
            SUNDAY at SEVENTH,
            THURSDAY at SECOND,
            THURSDAY at THIRD
        )

        assertEquals(expected, timetable.iterator().toSet())
    }

    @Test
    fun `timetable's iterator contains each time only once`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FIRST)
            }
            on(SUNDAY) {
                at(FIRST)
            }
        }

        assertEquals(1, timetable.iterator().count { it == SUNDAY at FIRST })
    }

    @Test
    fun `timetable's toMap returns correct map`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FOURTH)
            }
            on(TUESDAY) {
                at(FIFTH)
            }
            on(SATURDAY) {
                at(FIRST)
            }
        }

        val expected = mapOf(
            SUNDAY at FIRST to true,
            SUNDAY at FOURTH to true,
            TUESDAY at FIFTH to true,
            SATURDAY at FIRST to true
        )

        assertEquals(expected, timetable.toMap())
    }

    @Test
    fun `timetable's toStringMap returns correct map`() {
        val timetable = timetableOf {
            on(SUNDAY) {
                at(FIRST)
                at(FOURTH)
            }
            on(TUESDAY) {
                at(FIFTH)
            }
            on(SATURDAY) {
                at(FIRST)
            }
        }

        val expected = mapOf(
            "SUNDAY\$FIRST" to true,
            "SUNDAY\$FOURTH" to true,
            "TUESDAY\$FIFTH" to true,
            "SATURDAY\$FIRST" to true
        )

        assertEquals(expected, timetable.toStringMap())
    }

    @Test
    fun `emptyTimetable returns an empty timetable`() {
        val timetable = emptyTimetable()

        assert(timetable.iterator().toSet().isEmpty())
    }

    @Test
    fun `empty Map's toTimetable returns an empty timetable`() {
        val timetable = emptyMap<String, Boolean>().toTimetable()

        assert(timetable.iterator().toSet().isEmpty())
    }

    @Test
    fun `Map's to timetable returns timetables containing the map's true keys`() {
        val timetable = mapOf(
            "$SUNDAY$$FIRST" to true,
            "$SUNDAY$$SECOND" to false,
            "$THURSDAY$$FIFTH" to true,
            "$SATURDAY$$FIRST" to true
        ).toTimetable()

        val expected = setOf(
            SUNDAY at FIRST,
            THURSDAY at FIFTH,
            SATURDAY at FIRST
        )

        assertEquals(expected, timetable.iterator().toSet())
    }

    @Test
    fun `adding an already contained time to a timetable does nothing`() {
        val timetable = mutableTimetableOf(SUNDAY at FIRST)

        timetable.add(SUNDAY at FIRST)

        assertEquals(setOf(SUNDAY at FIRST), timetable.iterator().toSet())
    }

    @Test
    fun `adding a time to the timetable adds it`() {
        val timetable = mutableTimetableOf(SUNDAY at FIRST)

        timetable.add(MONDAY at FIRST)

        assertEquals(setOf(SUNDAY at FIRST, MONDAY at FIRST), timetable.iterator().toSet())
    }

    @Test
    fun `removing a not contained time from a timetable does nothing`() {
        val timetable = mutableTimetableOf(SUNDAY at FIRST)

        timetable.remove(MONDAY at FIRST)

        assertEquals(setOf(SUNDAY at FIRST), timetable.iterator().toSet())
    }

    @Test
    fun `removing a time from a timetable removes it`() {
        val timetable = mutableTimetableOf(SUNDAY at FIRST, MONDAY at FIRST)

        timetable.remove(MONDAY at FIRST)

        assertEquals(setOf(SUNDAY at FIRST), timetable.iterator().toSet())
    }
}