package com.example.karzararhesapla.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.karzararhesapla.databinding.ItemHedefBinding
import com.example.karzararhesapla.entity.Hedefler
import java.text.SimpleDateFormat
import java.util.Locale

class HedefAdapter(
    private val onItemClick: (Hedefler) -> Unit = {}
) : RecyclerView.Adapter<HedefAdapter.HedefViewHolder>()
{

    inner class HedefViewHolder(val binding: ItemHedefBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Hedefler>()
    {
        override fun areItemsTheSame(oldItem: Hedefler, newItem: Hedefler): Boolean
        {
            return oldItem.hedefID == newItem.hedefID
        }

        override fun areContentsTheSame(oldItem: Hedefler, newItem: Hedefler): Boolean
        {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Hedefler>)
    {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HedefViewHolder
    {
        val binding = ItemHedefBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HedefViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HedefViewHolder, position: Int)
    {
        val hedef = differ.currentList[position]
        val binding = holder.binding

        binding.tvTargetTitle.text = hedef.hedefAdi

        val format = SimpleDateFormat("dd MMM yyyy", Locale("tr"))
        binding.tvTargetDate.text = "Son Tarih: ${format.format(hedef.hedefBitisTarihi)}"

        if (hedef.hedefTutar > 0)
        {
            binding.tvTargetAmount.text = "${hedef.hedefTutar} ₺"
            binding.tvCompletedAmount.text = "${hedef.tamamlananTutar} ₺"
            
            val yuzde = if (hedef.hedefTutar > 0) ((hedef.tamamlananTutar / hedef.hedefTutar) * 100).toInt() else 0
            binding.progressBar.progress = yuzde
            binding.tvPercentage.text = "%$yuzde"
        }
        else
        {
            binding.tvTargetAmount.text = "${hedef.hedefMiktar} Adet"
            binding.tvCompletedAmount.text = "${hedef.tamamlananMiktar} Adet"
            
            val yuzde = if (hedef.hedefMiktar > 0) ((hedef.tamamlananMiktar.toDouble() / hedef.hedefMiktar) * 100).toInt() else 0
            binding.progressBar.progress = yuzde
            binding.tvPercentage.text = "%$yuzde"
        }

        // Karta tıklama - güncelleme dialogunu aç
        binding.root.setOnClickListener {
            onItemClick(hedef)
        }
    }

    override fun getItemCount(): Int
    {
        return differ.currentList.size
    }
}
