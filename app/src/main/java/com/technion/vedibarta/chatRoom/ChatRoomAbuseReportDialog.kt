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
import kotlinx.android.synthetic.main.report_abuse_dialog.*

class ChatRoomAbuseReportDialog: DialogFragment() {

    private lateinit var listener: AbuseReportDialogListener

    interface AbuseReportDialogListener
    {
        fun onAbuseTypeClick(dialog: DialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.report_abuse_dialog, container, false)
        dialog?.setCanceledOnTouchOutside(true)
        return inflatedView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as AbuseReportDialogListener
        } catch (e: ClassCastException) {
            Log.d("reportAbuseDialog", e.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        abuse1RadioButton.setOnClickListener{
            listener.onAbuseTypeClick(this)
            Toast.makeText(context, "abuse1", Toast.LENGTH_SHORT).show()
        }

        abuse2RadioButton.setOnClickListener{
            listener.onAbuseTypeClick(this)
            Toast.makeText(context, "abuse2", Toast.LENGTH_SHORT).show()
        }

        abuse3RadioButton.setOnClickListener{
            listener.onAbuseTypeClick(this)
            Toast.makeText(context, "abuse3", Toast.LENGTH_SHORT).show()
        }

        reportAbuseDismissButton.setOnClickListener {
            Toast.makeText(context, "dismiss", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}