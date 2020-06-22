package com.technion.vedibarta.chatRoom

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.technion.vedibarta.R
import kotlinx.android.synthetic.main.report_abuse_dialog.*

/***
 * class in charge of the reporting functionality in a specific chat
 */
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

        reportButton.text = "הבא"

        reportButton.setOnClickListener{
            if (reportViewFlipper.displayedChild == 0) {
                reportViewFlipper.showNext()
                reportButton.text = "דווח"
                reportBackButton.visibility = View.VISIBLE
            } else {
                Toast.makeText(context, "Report", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        reportBackButton.setOnClickListener {
            reportViewFlipper.showPrevious()
            reportBackButton.visibility = View.GONE
            reportButton.text = "הבא"
        }

        reportAbuseDismissButton.setOnClickListener {
            dismiss()
        }
    }
}