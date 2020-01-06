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

    private lateinit var hobbies: Array<String>
    private lateinit var partnerHobbies: Array<String>

    interface QuestionGeneratorDialogListener
    {
        fun onQuestionclick(dialog: DialogFragment, v: View)
    }

    companion object{
        fun newInstance(hobbies :Array<String>, partnerHobbies: Array<String>): ChatRoomQuestionGeneratorDialog {
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
        questionCategoriesList.adapter = QuestionGeneratorCategoryAdapter(getCategoriesInCommon())
        questionCategoriesList.layoutManager = LinearLayoutManager(this.context)
        questionGeneratorDismissButton.setOnClickListener {
            dismiss()
        }
    }

    private fun getCategoriesInCommon(): Array<String> {
        val commonHobbies = hobbies.filter { partnerHobbies.contains(it) }
        val hobbyIdToCategory = resources.getStringArray(R.array.hobbiesId_to_category)
        val hobbies = resources.getStringArray(R.array.hobbies)
        val commonCategories = commonHobbies.map { hobbyIdToCategory[hobbies.indexOf(it)] }
        return commonCategories.plusElement("כללי").toTypedArray()
    }
}