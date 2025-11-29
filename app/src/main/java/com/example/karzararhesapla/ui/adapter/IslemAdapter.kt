package com.example.karzararhesapla.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.karzararhesapla.entity.Islemler
import com.example.karzararhesapla.R
import com.example.karzararhesapla.databinding.ItemIslemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IslemAdapter : ListAdapter<Islemler, IslemAdapter.IslemViewHolder>(IslemDiffCallback())
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IslemViewHolder
    {
        val binding = ItemIslemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IslemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IslemViewHolder, position: Int)
    {
        holder.bind(getItem(position))
    }

    class IslemViewHolder(private val binding: ItemIslemBinding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(islem: Islemler)
        {
            binding.tvTitle.text = if (islem.islemAciklamasi.isNotEmpty()) islem.islemAciklamasi else islem.islemTuru

            val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale("tr"))
            binding.tvDate.text = sdf.format(Date(islem.islemTarihi))

            val isGelir = islem.islemTuru == "SATIS" || islem.islemTuru == "GELIR"
            val context = binding.root.context

            if (isGelir)
            {
                binding.tvAmount.text = "+${islem.islemTutari} ₺"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.profit_green))
                binding.viewIcon.setBackgroundResource(R.drawable.bg_circle_green)
            }
            else
            {
                binding.tvAmount.text = "-${islem.islemTutari} ₺"
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.loss_red))
                binding.viewIcon.setBackgroundResource(R.drawable.bg_circle_red)
            }
        }
    }

    class IslemDiffCallback : DiffUtil.ItemCallback<Islemler>()
    {
        override fun areItemsTheSame(oldItem: Islemler, newItem: Islemler): Boolean
        {
            return oldItem.islemID == newItem.islemID
        }

        override fun areContentsTheSame(oldItem: Islemler, newItem: Islemler): Boolean {
            return oldItem == newItem
        }
    }
}
