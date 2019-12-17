package com.technion.vedibarta.studentsMatching.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.studentsMatching.Matcher
import org.junit.After
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MatcherImplTest {
    private val studentsCollection = FirebaseFirestore.getInstance()
        .collection("Tests").document("MatcherImplTest")
        .collection("students")

    @Before
    fun createUsers() {
        studentsCollection.apply {
            Tasks.await(Tasks.whenAll(
                document("Or").set(Student(
                    name = "Or",
                    region = "Haifa",
                    school = "Makif Haifa",
                    characteristics = mutableMapOf(
                        "vegan" to true,
                        "religious" to false
                    )
                )),
                document("Victor").set(Student(
                    name = "Victor",
                    region = "Haifa",
                    school = "Haifa's High School",
                    characteristics = mutableMapOf(
                        "vegan" to true,
                        "religious" to true,
                        "jewish" to false
                    )
                )),
                document("Sahar").set(Student(
                    name = "Sahar",
                    region = "Tel Aviv",
                    school = "Makif Tel Aviv",
                    characteristics = mutableMapOf(
                        "vegan" to true,
                        "religious" to true,
                        "jewish" to true
                    )
                ))
            ))
        }
    }

    @After
    fun deleteUsers() {
        studentsCollection.apply {
            Tasks.await(Tasks.whenAll(
                document("Or").delete(),
                document("Victor").delete(),
                document("Sahar").delete()
            ))
        }
    }

    @Test
    fun matcherReturnsNothingIfNoStudentFromTheGivenRegionExists() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("vegan", "religious"),
            region = "Nowhere"
        )

        val result = Tasks.await(matcher.match())

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentFromTheGivenSchoolExists() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("vegan", "religious"),
            region = "Fake School"
        )

        val result = Tasks.await(matcher.match())

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentWithAWantedCharacteristicExists1() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("fake characteristic")
        )

        val result = Tasks.await(matcher.match())

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsNothingIfNoStudentWithAWantedCharacteristicExists2() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("vegan", "fake characteristic")
        )

        val result = Tasks.await(matcher.match())

        assert(result.isEmpty())
    }

    @Test
    fun matcherReturnsOnlyStudentsFromWantedRegion() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("vegan"),
            region = "Haifa"
        )

        val result = Tasks.await(matcher.match())

        assert(result.all { it.region == "Haifa" })
    }

    @Test
    fun matcherReturnsOnlyStudentsFromWantedSchool() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("vegan"),
            region = "Makif Haifa"
        )

        val result = Tasks.await(matcher.match())

        assert(result.all { it.school == "Makif Haifa" })
    }

    @Test
    fun matcherReturnsOnlyStudentsWhoHaveWantedSingleCharacteristic() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("religious")
        )

        val result = Tasks.await(matcher.match())

        assert(result.all { it.characteristics["religious"] == true })
    }

    @Test
    fun matcherReturnsAllStudentsWithWantedSingleCharacteristic() {
        val matcher: Matcher = MatcherImpl(
            studentsCollection,
            setOf("religious")
        )

        val result = Tasks.await(matcher.match())

        assertEquals(setOf("Victor", "Sahar"), result.map { it.name }.toSet())
    }
}