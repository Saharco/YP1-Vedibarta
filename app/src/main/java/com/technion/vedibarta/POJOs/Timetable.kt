package com.technion.vedibarta.POJOs

enum class Day(val int: Int) {
    SUNDAY      (0),
    MONDAY      (1),
    TUESDAY     (2),
    WEDNESDAY   (3),
    THURSDAY    (4),
    FRIDAY      (5),
    SATURDAY    (6);

    companion object {
        private val reverseInt = values().associateBy { it.int }
        fun fromInt(int: Int) = reverseInt[int]
    }
}

enum class Hour(val int: Int) {
    FIRST   (0),
    SECOND  (1),
    THIRD   (2),
    FOURTH  (3),
    FIFTH   (4),
    SIXTH   (5),
    SEVENTH (6),
    EIGHTH  (7),
    NINTH   (8),
    TENTH   (9);

    companion object {
        private val reverseInt = values().associateBy { it.int }
        fun fromInt(int: Int) = reverseInt[int]
    }
}

data class DayHour(val day: Day, val hour: Hour) {
    override fun toString() = "$day\$$hour"

    companion object {
        fun all(): Iterable<DayHour> =
            Day.values().flatMap { day ->
                Hour.values().map { hour ->
                    day at hour
                }
            }
    }
}

infix fun Day.at(hour: Hour) = DayHour(this, hour)

fun String.toDayHour(): DayHour {
    val day = Day.valueOf(substringBefore('$'))
    val hour = Hour.valueOf(substringAfter('$'))

    return day at hour
}

interface Timetable {
    operator fun contains(dayHour: DayHour): Boolean

    operator fun iterator(): Iterable<DayHour> = DayHour.all().filter { it in this }

    fun toMap(): Map<DayHour, Boolean> = iterator().map { it to (it in this) }.toMap()

    fun toStringMap(): Map<String, Boolean> = toMap().mapKeys { it.key.toString() }

    fun isEmpty(): Boolean
}

class MutableTimetable
internal constructor(private val set: MutableSet<DayHour>) : Timetable {
    override fun contains(dayHour: DayHour): Boolean = set.contains(dayHour)
    override fun isEmpty() = set.isEmpty()

    fun add(dayHour: DayHour) = set.add(dayHour)

    fun remove(dayHour: DayHour) = set.remove(dayHour)
}

@DslMarker
annotation class TimetableBuilderMarker

@TimetableBuilderMarker
class TimetableBuilder {
    private val dayHours = mutableSetOf<DayHour>()

    fun on(day: Day, func: DayHourSetBuilder.() -> Unit) {
        val hoursSetBuilder = DayHourSetBuilder(day)
        hoursSetBuilder.func()
        dayHours.addAll(hoursSetBuilder.build())
    }

    fun build() = mutableTimetableOf(dayHours)
}

@TimetableBuilderMarker
class DayHourSetBuilder(private val day: Day) {
    private val hours = mutableSetOf<Hour>()

    fun at(hour: Hour) {
        hours.add(hour)
    }

    fun build() = hours.map { hour -> day at hour }
}

fun mutableTimetableOf(times: Collection<DayHour> = emptySet()): MutableTimetable =
    MutableTimetable(times.toMutableSet())

fun timetableOf(times: Collection<DayHour>): Timetable = mutableTimetableOf(times)

fun mutableTimetableOf(vararg times: DayHour) = mutableTimetableOf(times.toList())

fun timetableOf(vararg times: DayHour) = timetableOf(times.toList())

fun mutableTimetableOf(func: TimetableBuilder.() -> Unit): MutableTimetable =
    TimetableBuilder().apply(func).build()

fun timetableOf(func: TimetableBuilder.() -> Unit): Timetable = mutableTimetableOf(func)

fun Map<String, Boolean>.toMutableTimetable() = mutableTimetableOf(
    this.keys.filter { this[it] == true }.map { it.toDayHour() }
)

fun Map<String, Boolean>.toTimetable(): Timetable = this.toMutableTimetable()

fun emptyTimetable() = timetableOf()
