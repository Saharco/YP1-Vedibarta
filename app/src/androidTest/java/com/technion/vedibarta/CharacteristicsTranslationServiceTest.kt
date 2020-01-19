package com.technion.vedibarta

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.utilities.services.Languages
import com.technion.vedibarta.utilities.services.translate
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class CharacteristicsTranslationServiceTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun translationWorksOnSingleString(){
        val initial = "דתיה"
        val translated = initial.translate(appContext).characteristics()
            .from(Languages.HEBREW,Gender.FEMALE)
            .to(Languages.HEBREW,Gender.MALE)
            .execute().first()
        Assert.assertEquals(translated, "דתי")
    }

    @Test
    fun translationWorksWithArray(){
        val initial = appContext.resources.getStringArray(R.array.characteristicsMale_hebrew)
        val goal = appContext.resources.getStringArray(R.array.characteristicsFemale_hebrew)
        val translated = initial.translate(appContext)
            .characteristics()
            .from(Languages.HEBREW)
            .to(Languages.HEBREW,Gender.FEMALE)
            .execute()
        Assert.assertArrayEquals(goal, translated)
    }

    @Test
    fun doesNoChangeStringIfToAndFromAreTheSame() {
        val initial = "דתיה"
        val translated = initial.translate(appContext)
            .characteristics()
            .from(Languages.HEBREW, Gender.FEMALE)
            .to(Languages.HEBREW,Gender.FEMALE)
            .execute().first()
        Assert.assertEquals(initial, translated)
    }

    @Test
    fun hobbies_translationWorksOnSingleString(){
        val initial = "ריקוד"
        val translated = initial.translate(appContext).hobbies()
            .from(Languages.BASE)
            .to(Languages.HEBREW)
            .execute().first()
        Assert.assertEquals(translated, "ריקוד")
    }

    @Test
    fun hobbies_doesNoChangeStringIfToAndFromAreTheSame() {
        val initial = "אוכל"
        val translated = initial.translate(appContext)
            .hobbies()
            .from(Languages.HEBREW)
            .to(Languages.HEBREW)
            .execute().first()
        Assert.assertEquals(initial, translated)
    }


}