package com.technion.vedibarta.utilities.questionGenerator

import com.google.android.gms.tasks.Task
import java.io.Serializable

interface QuestionGeneratorManager : Serializable {
    /**
     * returns a list of the mutual hobbies.
     * in case there are no mutual hobbies returns an empty list
     */
    fun getMutualHobbies(): List<String>

    /**
     * returns a list of the mutual hobbies' categories, general questions about the category
     * The Map is <Category, Questions>
     */
    fun getMutualCategories() : Task<List<String>>

    /**
     * returns all questions based on mutual hobbies, questions based on specific hobbies
     * The Map is <Category, Questions>
     */
    fun getQuestionsBasedOnMutualHobbies() : Task<Map<String, List<String>>>

    /**
     * returns all questions based on mutual hobbies' categories
     */
    fun getQuestionsBasedOnMutualCategories() : Task<Map<String, List<String>>>

    /**
     * Returns all questions based on mutual hobbies, and mutual hobbies' categories
     * also returns general questions, questions that are not related to hobbies
     * The Map is <Category, Questions>
     */
    fun getQuestions() : Task<Map<String, List<String>>>
}