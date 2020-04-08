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
import com.technion.vedibarta.POJOs.Student
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.VedibartaActivity.Companion.dpToPx
import com.technion.vedibarta.utilities.resourcesManagement.RemoteResourcesManager
import com.technion.vedibarta.utilities.resourcesManagement.toCurrentLanguage

open class CarouselAdapter(
    val context: Context,
    val itemClick: (position: Int, carouselAdapterItem: Student) -> Unit
) :
    RecyclerView.Adapter<ItemViewHolder>() {

    private val TAG = "carouselAdapter"

    protected var carouselAdapterItems: List<Student> = listOf()
    private val selectedPos = RecyclerView.NO_POSITION

    private val BUBBLE_WIDTH = 38

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item_horizontal,
                    parent,
                    false
                )
            )
        } else {
            ItemViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.carousel_item,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(carouselAdapterItems[holder.adapterPosition])
        holder.itemView.setOnClickListener {
            itemClick(holder.adapterPosition, carouselAdapterItems[holder.adapterPosition])
        }
        holder.itemView.isSelected = holder.adapterPosition == selectedPos

        val student = carouselAdapterItems[holder.adapterPosition]
        loadProfilePicture(holder.profilePicture, student)
        holder.name.text = student.name
        holder.description.text = "${student.school}, ${student.region}"
        if (student.gender != Gender.FEMALE)
            holder.button.text = context.resources.getString(R.string.chat_candidate_accept_button_m)
        populateTable(holder.table, student)
    }

    private fun loadProfilePicture(profilePicture: ImageView, student: Student) {
        if (student.photo == null) {
            loadDefaultUserProfilePicture(profilePicture, student)
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(student.photo)
            .apply(RequestOptions.circleCropTransform())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadDefaultUserProfilePicture(profilePicture, student)
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

    private fun loadDefaultUserProfilePicture(profilePicture: ImageView, student: Student) {
        if (student.gender == Gender.MALE)
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_man)
        else
            profilePicture.setImageResource(R.drawable.ic_photo_default_profile_girl)
//        displayUserProfilePicture(profilePicture)
    }

    @SuppressLint("InflateParams")
    private fun populateTable(table: TableLayout, student: Student) {
//        if (student.characteristics.isEmpty()) {
//            handleNoCharacteristics()
//            return
//        }
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

            val studentsCharacteristics = student.characteristics.filter { it.value }.keys.toList()
            (studentsCharacteristics.indices step colsAmount).forEach { i ->

                if (i <= maxBubblesIndex) {

                    val tableRow = TableRow(context)
                    tableRow.layoutParams = tableRowParams
                    tableRow.gravity = Gravity.CENTER_HORIZONTAL

                    for (j in 0 until colsAmount) {
                        if (i + j >= studentsCharacteristics.size)
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
                        RemoteResourcesManager(context).findMultilingualResource("characteristics", student.gender)
                            .addOnSuccessListener { bubble.text = it.toCurrentLanguage(studentsCharacteristics[i+j]); loading.visibility = View.GONE}

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

    fun setItems(newCarouselAdapterItems: List<Student>) {
        carouselAdapterItems = newCarouselAdapterItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val profilePicture: ImageView = view.findViewById(R.id.candidateProfilePicture)
    val name: TextView = view.findViewById(R.id.candidateName)
    val description: TextView = view.findViewById(R.id.candidateDescription)
    val table: TableLayout = view.findViewById(R.id.candidateTable)
    val button: Button = view.findViewById(R.id.confirmCandidateButton)

    fun bind(carouselAdapterItem: Student) {

    }
}