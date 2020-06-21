package com.technion.vedibarta.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Space
import com.technion.vedibarta.R
import com.technion.vedibarta.databinding.ScheduleButtonBinding
import com.technion.vedibarta.databinding.ScheduleDayTitleBinding
import com.technion.vedibarta.databinding.SchedulePeriodTitleBinding

class ScheduleAdapter(
    private val context: Context,
    private val onClick: (Int, Int, Boolean) -> Unit
) : BaseAdapter() {

    companion object {
        val DAYS_RESOURCES = listOf(
            R.string.alef,
            R.string.bet,
            R.string.ghimel,
            R.string.daled,
            R.string.hei,
            R.string.vav,
            R.string.shin
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

    override fun getCount() = (DAYS_RESOURCES.size + 1) * (PERIODS_RESOURCES.size + 1)

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (position == 0) {
            return getBlankView()
        }

        if (position <= DAYS_RESOURCES.size) {
            return getDayTitleView(position - 1, parent)
        }

        if (position % (DAYS_RESOURCES.size + 1) == 0) {
            return getPeriodTitleView(position / (DAYS_RESOURCES.size + 1) - 1, parent)
        }

        return getButtonView(
            position % (DAYS_RESOURCES.size + 1) - 1,
            position / (DAYS_RESOURCES.size + 1) - 1,
            parent
        )
    }

    private fun getDayTitleView(dayIdx: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ScheduleDayTitleBinding.inflate(inflater, parent, false)

        binding.text = context.getString(DAYS_RESOURCES[dayIdx])

        return binding.root
    }

    private fun getPeriodTitleView(periodIdx: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SchedulePeriodTitleBinding.inflate(inflater, parent, false)

        binding.text = context.getString(PERIODS_RESOURCES[periodIdx])

        return binding.root
    }

    private fun getBlankView(): View {
        return Space(context)
    }

    private fun getButtonView(dayIdx: Int, periodIdx: Int, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ScheduleButtonBinding.inflate(inflater, parent, false)

        binding.button.setOnCheckedChangeListener { button, isChecked ->
            button.alpha = if (isChecked) 1f else 0.4f
            onClick(dayIdx, periodIdx, isChecked)
        }

        return binding.root
    }
}