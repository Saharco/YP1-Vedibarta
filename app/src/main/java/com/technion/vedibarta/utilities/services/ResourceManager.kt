package com.technion.vedibarta.utilities.services

import android.content.Context
import com.technion.vedibarta.POJOs.Gender.FEMALE
import com.technion.vedibarta.POJOs.Gender.MALE
import com.technion.vedibarta.R

class ResourceManager(context: Context, type: StringType) {
    val resourcesMap: MutableMap<String, Array<String>> = mutableMapOf()

    init {
        when(type){
            StringType.CHARACTERISTICS -> {
                resourcesMap["${Languages.BASE}_${MALE}"] = context.resources.getStringArray(R.array.characteristics_base)
                resourcesMap["${Languages.HEBREW}_$MALE"] = context.resources.getStringArray(R.array.characteristicsMale)
                resourcesMap["${Languages.HEBREW}_$FEMALE"] = context.resources.getStringArray(R.array.characteristicsFemale)
                //TODO add arabic support
            }
            StringType.HOBBIES -> {
                resourcesMap["${Languages.BASE}_${HobbyType.HOBBY}"] = context.resources.getStringArray(R.array.hobbies_base)
                resourcesMap["${Languages.HEBREW}_${HobbyType.HOBBY}"] = context.resources.getStringArray(R.array.hobbies)
                resourcesMap["${Languages.HEBREW}_${HobbyType.CATEGORY}"] = context.resources.getStringArray(R.array.hobbies_categories_base)
                resourcesMap["${Languages.HEBREW}_${HobbyType.CATEGORY}"] = context.resources.getStringArray(R.array.hobbies_categories)
                //TODO add arabic support
            }
        }
    }

}
