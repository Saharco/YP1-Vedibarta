package com.technion.vedibarta.matching

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.POJOs.Grade
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.database.DatabaseVersioning
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentsMatcherTest {
    private val studentsCollection =
        DatabaseVersioning.getTestVersion("MatcherImplTest").instance.collection("students")

    @Before
    fun createUsers() {
        studentsCollection.apply {
            Tasks.await(
                Tasks.whenAll(
                document("Or").set(
                    Student(
                        name = "Or",
                        region = "Haifa",
                        school = "Makif Haifa",
                        grade = Grade.TENTH,
                        characteristics = mutableMapOf(
                            "vegan" to true,
                            "religious" to false
                        )
                    )
                ),
                document("Victor").set(
                    Student(
                        name = "Victor",
                        region = "Haifa",
                        school = "Haifa's High School",
                        grade = Grade.ELEVENTH,
                        characteristics = mutableMapOf(
                            "vegan" to true,
                            "religious" to true,
                            "failthfull" to false
                        )
                    )
                ),
                document("Sahar").set(
                    Student(
                        name = "Sahar",
                        region = "Tel Aviv",
                        school = "Makif Tel Aviv",
                        grade = Grade.TENTH,
                        characteristics = mutableMapOf(
                            "vegan" to true,
                            "religious" to true,
                            "failthfull" to true
                        )
                    )
                )
            ))
        }
    }

    @After
    fun deleteUsers() {
        studentsCollection.apply {
            Tasks.await(
                Tasks.whenAll(
                document("Or").delete(),
                document("Victor").delete(),
                document("Sahar").delete()
            ))
        }
    }

    @Test
    fun matcherReturnsNothingIfNoStudentFromTheGivenRegionExists() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan", "religious"),
            region = "Nowhere"
        ))

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentFromTheGivenSchoolExists() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan", "religious"),
            school = "Fake School"
        ))

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentWithAWantedCharacteristicExists1() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("nonreligious")
        ))

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentWithAWantedCharacteristicExists2() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan", "nonreligious")
        ))

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsOnlyStudentsFromWantedRegion() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan"),
            region = "Haifa"
        ))

        assert(result.all { it.region == "Haifa" })
    }

    @Test
    fun matcherReturnsOnlyStudentsFromWantedSchool() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan"),
            region = "Makif Haifa"
        ))

        assert(result.all { it.school == "Makif Haifa" })
    }

    @Test
    fun matcherReturnsOnlyStudentsOfWantedGraded() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan"),
            grade = Grade.TENTH
        ))

        assert(result.all { it.grade == Grade.TENTH })
    }

    @Test
    fun matcherReturnsOnlyStudentsWhoHaveWantedSingleCharacteristic() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("religious")
        ))

        assert(result.all { it.characteristics["religious"] == true })
    }

    @Test
    fun matcherReturnsAllStudentsWithWantedSingleCharacteristic() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("religious")
        ))

        assertEquals(setOf("Victor", "Sahar"), result.map { it.name }.toSet())
    }

    @Test
    fun matcherReturnsStudentsThatAreNotAPerfectMatch() {
        val matcher = StudentsMatcher(studentsCollection)

        val result = Tasks.await(matcher.match(
            characteristics = setOf("vegan", "religious", "failthfull")
        ))

        assertEquals(listOf("Sahar", "Victor"), result.map { it.name })
    }
}