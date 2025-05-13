package com.pentabytex.alshafimedledger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.databinding.ItemCustomerBinding
import com.pentabytex.alshafimedledger.data.models.Customer

class CustomerAdapter(
    private val onItemClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.CustomerViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer) = with(binding) {
            tvCustomerName.text = customer.name
            tvCustomerPhone.text = "Phone: ${customer.phone}"
            tvCustomerAddress.text = "Address: ${customer.address}"

            root.setOnClickListener { onItemClick(customer) }
            ivDelete.setOnClickListener { onDeleteClick(customer) }
        }
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Customer>() {
            override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
                return oldItem == newItem
            }
        }
    }
}
