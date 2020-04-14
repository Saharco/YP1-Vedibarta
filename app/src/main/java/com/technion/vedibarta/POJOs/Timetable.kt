package com.technion.vedibarta.POJOs

enum class Day(val int: Int) {
    SUNDAY      (1),
    MONDAY      (2),
    TUESDAY     (3),
    WEDNESDAY   (4),
    THURSDAY    (5),
    FRIDAY      (6),
    SATURDAY    (7);

    companion object {
        private val reverseInt = values().associateBy { it.int }
        fun fromInt(int: Int) = reverseInt[int]
    }
}

enum class Hour(val int: Int) {
    FIRST   (1),
    SECOND  (2),
    THIRD   (3),
    FOURTH  (4),
    FIFTH   (5),
    SIXTH   (6),
    SEVENTH (7),
    EIGHTH  (8),
    NINTH   (9),
    TENTH   (10);

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
}

class MutableTimetable
internal constructor(private val set: MutableSet<DayHour>) : Timetable {
    override fun contains(dayHour: DayHour): Boolean = set.contains(dayHour)
    fun add(dayHour: DayHour) = set.add(dayHour)
    fun remove(dayHour: DayHour) = set.remove(dayHour)
}

class TimetableBuilder
internal constructor() {
    private val dayHours = mutableSetOf<DayHour>()

    class DayHourSetBuilder
    internal constructor(private val day: Day) {

        private val hours = mutableSetOf<Hour>()
        infix fun at(hour: Hour) {
            hours.add(hour)
        }

        internal fun build() = hours.map { hour -> day at hour }
    }

    fun on(day: Day, func: DayHourSetBuilder.() -> Unit) {
        val hoursSetBuilder = DayHourSetBuilder(day)
        hoursSetBuilder.func()
        dayHours.addAll(hoursSetBuilder.build())
    }

    internal fun build() = mutableTimetableOf(dayHours)
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
