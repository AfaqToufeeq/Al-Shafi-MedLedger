package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ItemMedicineBinding

class MedicineAdapter(private val onItemClick: (Medicine) -> Unit) : ListAdapter<Medicine, MedicineAdapter.MedicineViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = ItemMedicineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MedicineViewHolder(private val binding: ItemMedicineBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(medicine: Medicine) = with(binding) {
            tvMedicineName.text = medicine.name
            tvTypeVolume.text = "${medicine.type} • ${medicine.volume}"
            tvQuantity.text = medicine.quantity.toString()
            tvPurchasePrice.text = "Rs. %.2f".format(medicine.purchasePrice)
            tvSellingPrice.text = "Rs. %.2f".format(medicine.sellingPrice)

            root.setOnClickListener { onItemClick(medicine) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Medicine>() {
            override fun areItemsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Medicine, newItem: Medicine): Boolean {
                return oldItem == newItem
            }
        }
    }
}
