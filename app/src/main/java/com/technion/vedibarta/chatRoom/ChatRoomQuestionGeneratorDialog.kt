package com.technion.vedibarta.chatRoom

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.profile_picture_dialog.dismissButton
import kotlinx.android.synthetic.main.question_generator_dialog.*

class ChatRoomQuestionGeneratorDialog : DialogFragment() {

    private lateinit var listener: QuestionGeneratorDialogListener

    interface QuestionGeneratorDialogListener
    {
        fun onQuestionclick(dialog: DialogFragment, v: View)
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

        questionExample1.setOnClickListener {
            listener.onQuestionclick(this, it)
            dismiss()
        }

        questionExample2.setOnClickListener {
            listener.onQuestionclick(this, it)
            dismiss()
        }

        questionExample3.setOnClickListener {
            listener.onQuestionclick(this, it)
            dismiss()
        }

        questionGeneratorDismissButton.setOnClickListener {
            Toast.makeText(context, "dismiss", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}