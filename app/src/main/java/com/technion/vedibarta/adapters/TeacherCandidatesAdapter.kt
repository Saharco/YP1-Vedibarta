package com.technion.vedibarta.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.technion.vedibarta.POJOs.Gender
import com.technion.vedibarta.POJOs.Teacher
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.dpToPx
import com.technion.vedibarta.utilities.resourcesManagement.RemoteTextResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage

open class TeacherCandidatesAdapter(
    val context: Context,
    val itemClick: (position: Int, carouselAdapterItem: Teacher) -> Unit
) : RecyclerView.Adapter<TeacherCandidatesAdapter.TeacherViewHolder>() {

    companion object {
        private const val TAG = "carouselAdapter"
        private const val BUBBLE_WIDTH = 38
    }

    protected var carouselAdapterItems: List<Teacher> = listOf()
    private val selectedPos = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            TeacherViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item_horizontal,
                    parent,
                    false
                )
            )
        } else {
            TeacherViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        holder.bind(carouselAdapterItems[holder.adapterPosition])
    }

    private fun loadProfilePicture(profilePicture: ImageView, teacher: Teacher) {
        if (teacher.photo == null) {
            loadDefaultUserProfilePicture(profilePicture, teacher)
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(teacher.photo)
            .apply(RequestOptions.circleCropTransform())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadDefaultUserProfilePicture(profilePicture, teacher)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
//                    displayUserProfilePicture(profilePicture)
                    return false
                }

            })
            .into(profilePicture)
    }

    private fun loadDefaultUserProfilePicture(profilePicture: ImageView, teacher: Teacher) {
        if (teacher.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
//        displayUserProfilePicture(profilePicture)
    }

    @SuppressLint("InflateParams")
    private fun populateTable(table: TableLayout, teacher: Teacher) {
        table.doOnPreDraw {

            table.removeAllViews()

            val tableRowParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )

            val bubbleParams =
                TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

            val rowsAmount =
                (table.height / dpToPx(context.resources, BUBBLE_WIDTH.toFloat())).toInt()
            val colsAmount =
                (table.width / dpToPx(context.resources, BUBBLE_WIDTH.toFloat())).toInt()
            val maxBubblesIndex = rowsAmount * colsAmount - 1

            Log.d(TAG, "Amount of rows: $rowsAmount")
            Log.d(TAG, "Amount of bubbles in a row: $colsAmount")

            val teacherCharacteristics = teacher.schoolCharacteristics.filter { it.value }.keys.toList()
            (teacherCharacteristics.indices step colsAmount).forEach { i ->

                if (i <= maxBubblesIndex) {

                    val tableRow = TableRow(context)
                    tableRow.layoutParams = tableRowParams
                    tableRow.gravity = Gravity.CENTER_HORIZONTAL

                    for (j in 0 until colsAmount) {
                        if (i + j >= teacherCharacteristics.size)
                            break

                        if (i + j == maxBubblesIndex) {
                            val bubbleFrame = LayoutInflater.from(context).inflate(
                                R.layout.carousel_plus_bubble,
                                null
                            ) as FrameLayout
                            bubbleFrame.layoutParams = bubbleParams
                            tableRow.addView(bubbleFrame)
                            break
                        }

                        val bubbleFrame = LayoutInflater.from(context).inflate(
                            R.layout.carousel_bubble_blue,
                            null
                        ) as FrameLayout

                        val bubble = bubbleFrame.findViewById(R.id.invisibleBubble) as TextView
                        val loading = bubbleFrame.findViewById(R.id.loading) as ProgressBar
                        RemoteTextResourcesManager(context).findMultilingualResource("school_characteristics", teacher.gender)
                            .addOnSuccessListener { bubble.text = it.toCurrentLanguage(teacherCharacteristics[i+j]); loading.visibility = View.GONE}

                        bubbleFrame.layoutParams = bubbleParams
                        tableRow.addView(bubbleFrame)
                    }

                    table.addView(tableRow)
                }
            }
        }
    }

    private fun handleNoCharacteristics() {
        //TODO: add some behavior for the scenario where the user has no characteristics
    }


    override fun getItemCount() = carouselAdapterItems.size

    fun setItems(newCarouselAdapterItems: List<Teacher>) {
        carouselAdapterItems = newCarouselAdapterItems
        notifyDataSetChanged()
    }

    inner class TeacherViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val profilePicture: ImageView = view.findViewById(R.id.candidateProfilePicture)
        val name: TextView = view.findViewById(R.id.candidateName)
        val description: TextView = view.findViewById(R.id.candidateDescription)
        val table: TableLayout = view.findViewById(R.id.candidateTable)
        val button: Button = view.findViewById(R.id.confirmCandidateButton)

        fun bind(carouselAdapterItem: Teacher) {
            itemView.setOnClickListener {
                itemClick(adapterPosition, carouselAdapterItems[adapterPosition])
            }
            itemView.isSelected = adapterPosition == selectedPos

            val teacher = carouselAdapterItems[adapterPosition]
            loadProfilePicture(profilePicture, teacher)
            name.text = teacher.name
            description.text = "${teacher.schools[0]}, ${teacher.regions[0]}"
            if (teacher.gender != Gender.FEMALE)
                button.text = context.resources.getString(R.string.chat_candidate_accept_button_m)
            populateTable(table, teacher)
        }
    }
}
