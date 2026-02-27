package com.example.engineeringcalculator.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.engineeringcalculator.data.CalculationEntity
import com.example.engineeringcalculator.databinding.ItemHistoryBinding
import java.text.DateFormat
import java.util.Date

class HistoryAdapter : ListAdapter<CalculationEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHistoryBinding.inflate(inflater, parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalculationEntity) {
            binding.textExpression.text = item.expression
            binding.textResult.text = item.result
            binding.textTime.text = DateFormat.getDateTimeInstance(
                DateFormat.SHORT,
                DateFormat.SHORT
            ).format(Date(item.timestamp))
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<CalculationEntity>() {
            override fun areItemsTheSame(oldItem: CalculationEntity, newItem: CalculationEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CalculationEntity, newItem: CalculationEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
