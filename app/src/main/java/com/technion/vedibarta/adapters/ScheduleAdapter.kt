package com.technion.vedibarta.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.POJOs.*
import com.technion.vedibarta.R
import com.technion.vedibarta.databinding.ScheduleButtonBinding
import com.technion.vedibarta.databinding.ScheduleHeaderBinding
import com.technion.vedibarta.utilities.extensions.exhaustive

class ScheduleAdapter(
    private val context: Context,
    private val onTimeChanged: ((DayHour, Boolean) -> Unit)? = null,
    private val initialSchedule: Timetable = emptyTimetable(),
    private val isEditable: Boolean = true
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    companion object {
        val DAYS_RESOURCES = listOf(
            R.string.alef,
            R.string.bet,
            R.string.ghimel,
            R.string.daled,
            R.string.hei,
            R.string.vav
        )

        val PERIODS_RESOURCES = listOf(
            R.string.firstPeriod,
            R.string.secondPeriod,
            R.string.thirdPeriod,
            R.string.fourthPeriod,
            R.string.fifthPeriod,
            R.string.sixthPeriod,
            R.string.seventhPeriod,
            R.string.eighthPeriod
        )
    }

    override fun getItemCount(): Int = (DAYS_RESOURCES.size + 1) * (PERIODS_RESOURCES.size + 1)

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == 0) {
            return ViewHolder.Blank(Space(parent.context))
        }

        if (viewType <= DAYS_RESOURCES.size) {
            val binding = ScheduleHeaderBinding.inflate(inflater, parent, false)
            return ViewHolder.DayHeader(binding)
        }

        if (viewType % (DAYS_RESOURCES.size + 1) == 0) {
            val binding = ScheduleHeaderBinding.inflate(inflater, parent, false)
            return ViewHolder.PeriodHeader(binding)
        }

        val binding = ScheduleButtonBinding.inflate(inflater, parent, false)
        val time = DayHour(
            Day.fromInt(viewType % (DAYS_RESOURCES.size + 1) - 1)!!,
            Hour.fromInt(viewType / (DAYS_RESOURCES.size + 1) - 1)!!
        )
        binding.button.isChecked = time in initialSchedule

        if (!isEditable) {
            binding.button.isEnabled = false
        }
        return ViewHolder.Button(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Blank -> holder.bind()
            is ViewHolder.DayHeader -> holder.bind(
                context.getString(DAYS_RESOURCES[position - 1])
            )
            is ViewHolder.PeriodHeader -> holder.bind(
                context.getString(PERIODS_RESOURCES[position / (DAYS_RESOURCES.size + 1) - 1])
            )
            is ViewHolder.Button -> holder.bind {
                onTimeChanged?.invoke(
                    DayHour(
                        Day.fromInt(position % (DAYS_RESOURCES.size + 1) - 1)!!,
                        Hour.fromInt(position / (DAYS_RESOURCES.size + 1) - 1)!!
                    ),
                    it
                )
            }
        }.exhaustive
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class Blank(space: Space) : ViewHolder(space) {
            fun bind() {}
        }

        class DayHeader(private val binding: ScheduleHeaderBinding) : ViewHolder(binding.root) {
            fun bind(text: String) {
                binding.text = text
                binding.executePendingBindings()
            }
        }

        class PeriodHeader(private val binding: ScheduleHeaderBinding) : ViewHolder(binding.root) {
            fun bind(text: String) {
                binding.text = text
                binding.executePendingBindings()
            }
        }

        class Button(private val binding: ScheduleButtonBinding) : ViewHolder(binding.root) {
            fun bind(onClick: (Boolean) -> Unit) {
                binding.button.alpha = if (binding.button.isChecked) 1f else 0.4f
                binding.button.setOnCheckedChangeListener { button, isChecked ->
                    button.alpha = if (isChecked) 1f else 0.4f
                    onClick(isChecked)
                }
                binding.executePendingBindings()
            }
        }
    }
}