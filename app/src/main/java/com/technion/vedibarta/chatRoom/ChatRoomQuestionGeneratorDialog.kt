package com.technion.vedibarta.chatRoom

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.technion.vedibarta.R
import com.technion.vedibarta.adapters.QuestionGeneratorCategoryAdapter
import com.technion.vedibarta.utilities.VedibartaActivity
import com.technion.vedibarta.utilities.extensions.executeAfterTimeoutInMillis
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorFactory
import com.technion.vedibarta.utilities.questionGenerator.QuestionGeneratorManager
import kotlinx.android.synthetic.main.question_generator_dialog.*

/***
 * class in charge of the question generation functionality in a specific chat
 */
class ChatRoomQuestionGeneratorDialog : DialogFragment()
{

    lateinit var questionGenerator: QuestionGeneratorManager
    lateinit var questions: Task<Map<String, List<String>>>


    companion object
    {
        lateinit var act: Activity
        fun newInstance(
            questionGenerator: QuestionGeneratorManager,
            activity: Activity
        ): ChatRoomQuestionGeneratorDialog
        {
            act = activity
            val fragment = ChatRoomQuestionGeneratorDialog()
            val args = Bundle()
            args.putSerializable("generator", questionGenerator)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        questionGenerator = requireArguments().getSerializable("generator")!! as QuestionGeneratorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val inflatedView = inflater.inflate(R.layout.question_generator_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        questions = questionGenerator.getQuestions()
        Tasks.whenAll(questions)
            .continueWith{
                loading.visibility = View.GONE
                categoriesLayoutInner.visibility = View.VISIBLE
            categoriesLayoutInit()
        }
            .executeAfterTimeoutInMillis { VedibartaActivity.internetConnectionErrorHandler(act) }
        questionsLayout.visibility = View.GONE

        viewFlipper.setInAnimation(context, android.R.anim.fade_in)
        viewFlipper.setOutAnimation(context, android.R.anim.fade_out)

    }

    private fun categoriesLayoutInit()
    {
        questionCategoriesList.adapter =
            QuestionGeneratorCategoryAdapter(questions.result!!.keys.toTypedArray()) { str -> onCategorySelect(str) }
        questionCategoriesList.layoutManager = LinearLayoutManager(this.context)
        questionGeneratorDismissButton.setOnClickListener { dismiss() }
    }

    private fun questionsLayoutInit(category: String)
    {
        questionsLayoutTitle.text = category
        questionList.adapter =
            QuestionGeneratorCategoryAdapter(questions.result!![category]!!.toTypedArray()) { str -> onQuestionSelect(str) }
        questionList.layoutManager = LinearLayoutManager(this.context)
        backButton.setOnClickListener { onBackButton() }
    }

    private fun onQuestionSelect(question: String)
    {
        (activity as ChatRoomActivity).sendSystemMessage(question)
        dismiss()
    }


    private fun onCategorySelect(category: String)
    {
        questionsLayoutInit(category)
        viewFlipper.showNext()
    }

    private fun onBackButton()
    {
        viewFlipper.showPrevious()
    }

}