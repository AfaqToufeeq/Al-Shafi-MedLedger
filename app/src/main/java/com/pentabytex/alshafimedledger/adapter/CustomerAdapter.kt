package com.pentabytex.alshafimedledger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.pentabytex.alshafimedledger.databinding.ItemCustomerBinding
import com.pentabytex.alshafimedledger.data.models.Customer

class CustomerAdapter(
    private val flagVisibility: Boolean?,
    private val onItemClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit,
    private val onItemLongClick: (Customer) -> Unit,
    private val onSelectionChanged: (Customer, Boolean) -> Unit
) : ListAdapter<Customer, CustomerAdapter.CustomerViewHolder>(DiffCallback) {

    private val selectedItems = mutableSetOf<String>() // Track selected items
    private var selectedCustomerId: String? = null

    private var isSelectionMode = false

    fun setSelectionMode(enable: Boolean, customer: Customer) {
        isSelectionMode = enable

        if (enable) {
            selectedItems.add(customer.id)
        } else {
            selectedItems.clear()
            selectedCustomerId = null
        }
        notifyDataSetChanged()
    }


    fun selectAll(enable: Boolean) {
        if (enable) {
            selectedItems.clear()
            selectedItems.addAll(currentList.map { it.id })
        } else {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val item = getItem(position)
        val isItemSelected = selectedItems.contains(item.id)
        holder.bind(item, isItemSelected, isSelectionMode)
    }

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(customer: Customer, isSelected: Boolean, isSelectionMode: Boolean) = with(binding) {

            rbSelect.visibility = if (flagVisibility == true) View.VISIBLE else View.GONE
            rbSelect.setOnCheckedChangeListener(null) // Prevent unwanted triggers

            val isRadioSelected = customer.id == selectedCustomerId
            rbSelect.isChecked = isRadioSelected

            rbSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCustomerId = customer.id
                    onSelectionChanged(customer, true)
                    notifyDataSetChanged() // Refresh all items to reflect new selection
                }
            }


            tvCustomerName.text = customer.name
            tvCustomerPhone.text = "Phone: ${customer.phone}"
            tvCustomerAddress.text = "Address: ${customer.address}"

            // Handle checkbox visibility and click
            // Show checkboxes only in selection mode
            cbSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE

            // Final visibility logic for ivDelete
            ivDelete.visibility = when {
                isSelectionMode -> View.GONE // Always hide in selection mode
                flagVisibility == true -> View.GONE // Respect flag if not in selection mode
                else -> View.VISIBLE
            }

            cbSelect.setOnCheckedChangeListener(null)
            cbSelect.isChecked = isSelected

            cbSelect.setOnCheckedChangeListener { _, isChecked ->
                toggleSelection(customer, isChecked)
            }

            root.setOnClickListener {
                if (flagVisibility == true) {
                    selectedCustomerId = customer.id
                    onSelectionChanged(customer, true)
                    notifyDataSetChanged()
                }
                else if (isSelectionMode) {
                    toggleSelection(customer, !isSelected)
                } else {
                    onItemClick(customer)
                }
            }

            root.setOnLongClickListener {
                if (!isSelectionMode) {
                    onItemLongClick(customer)
                }
                true
            }

            ivDelete.setOnClickListener { onDeleteClick(customer) }
        }

        // Function to toggle selection state and notify
        private fun toggleSelection(customer: Customer, isChecked: Boolean) {
            if (isChecked) {
                selectedItems.add(customer.id)
            } else {
                selectedItems.remove(customer.id)
            }
            onSelectionChanged(customer, isChecked)
            notifyItemChanged(adapterPosition)
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
