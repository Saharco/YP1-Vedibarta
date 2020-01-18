package com.technion.vedibarta.chatRoom

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.QuestionGeneratorCategoryAdapter
import kotlinx.android.synthetic.main.question_generator_dialog.*


class ChatRoomQuestionGeneratorDialog : DialogFragment() {

    private lateinit var listener: QuestionGeneratorDialogListener
    lateinit var hobbies: Array<String>
    lateinit var partnerHobbies: Array<String>

    interface QuestionGeneratorDialogListener {
        fun onQuestionclick(dialog: DialogFragment, v: View)
    }

    companion object {
        fun newInstance(
            hobbies: Array<String>,
            partnerHobbies: Array<String>
        ): ChatRoomQuestionGeneratorDialog {
            val fragment = ChatRoomQuestionGeneratorDialog()
            val args = Bundle()
            args.putStringArray("hobbies", hobbies)
            args.putStringArray("partnerHobbies", partnerHobbies)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hobbies = arguments!!.getStringArray("hobbies")!!
        partnerHobbies = arguments!!.getStringArray("partnerHobbies")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.question_generator_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as QuestionGeneratorDialogListener
        } catch (e: ClassCastException) {
            Log.d("questionGeneratorDialog", e.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesLayoutInit()
        questionsLayout.visibility = View.GONE

        viewFlipper.setInAnimation(context, android.R.anim.fade_in)
        viewFlipper.setOutAnimation(context, android.R.anim.fade_out)

    }

    private fun categoriesLayoutInit() {
        questionCategoriesList.adapter =
            QuestionGeneratorCategoryAdapter(getCategoriesInCommon()) { str -> onCategorySelect(str) }
        questionCategoriesList.layoutManager = LinearLayoutManager(this.context)
        questionGeneratorDismissButton.setOnClickListener { dismiss() }
    }

    private fun questionsLayoutInit(category: String) {
        questionsLayoutTitle.text = category
        questionList.adapter = QuestionGeneratorCategoryAdapter(getQuestions(category)) { str -> onQuestionSelect(str) }
        questionList.layoutManager = LinearLayoutManager(this.context)
        backButton.setOnClickListener { onBackButton() }
    }

    private fun onQuestionSelect(question: String)
    {
        (activity as ChatRoomActivity).sendMessage(question, true)
        dismiss()
    }

    private fun getCategoriesInCommon(): Array<String> {
        val commonHobbies = hobbies.intersect(partnerHobbies.asIterable())
        val hobbyIdToCategory = resources.getStringArray(R.array.hobbiesId_to_category)
        val hobbies = resources.getStringArray(R.array.hobbies)
        val commonCategories = commonHobbies.map { hobbyIdToCategory[hobbies.indexOf(it)] }
        return commonCategories.plusElement("כללי").toTypedArray()
    }


    private fun onCategorySelect(category: String) {
        questionsLayoutInit(category)
        viewFlipper.showNext()
    }

    private fun onBackButton() {
        viewFlipper.showPrevious()
    }

    private fun getQuestions(category: String): Array<String> {
        if (category == "כללי") {
            questionList.visibility = View.VISIBLE
            emptyQuestionList.visibility = View.GONE
            return resources.getStringArray(R.array.general_questions)
        }
        val commonHobbies = hobbies.intersect(partnerHobbies.asIterable())
        val hobbiesCategories = resources.getStringArray(R.array.hobbies_categories)
        val hobbiesLinkArray = resources.obtainTypedArray(R.array.hobbies_id_link)
        val hobbiesCategoryToQuestion =
            resources.obtainTypedArray(R.array.hobbies_categories_questions_linker)

        val categoryIndex = hobbiesCategories.indexOf(category)
        val hobbyQuestionsId = hobbiesCategoryToQuestion.getResourceId(categoryIndex, -1)
        val hobbyId = hobbiesLinkArray.getResourceId(categoryIndex, -1)
        val hobbies = resources.getStringArray(hobbyId)
        val questionsIdArray = resources.obtainTypedArray(hobbyQuestionsId)

        val commonHobbiesIdFromChosenCategory = commonHobbies.map { hobby ->
            hobbies.indexOf(hobby)
        }.filter { it != -1 }

        val questions = commonHobbiesIdFromChosenCategory
            .map { id -> resources.getStringArray(questionsIdArray.getResourceId(id,-1)) }
            .flatMap { array -> array.plus(array).asIterable() }.distinct()

        hobbiesLinkArray.recycle()
        hobbiesCategoryToQuestion.recycle()
        questionsIdArray.recycle()
        if (questions.isEmpty()){
            questionList.visibility = View.GONE
            emptyQuestionList.visibility = View.VISIBLE
        }
        else{
            questionList.visibility = View.VISIBLE
            emptyQuestionList.visibility = View.GONE
        }

        return questions.toTypedArray()
    }


}