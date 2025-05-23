package com.pentabytex.alshafimedledger.ui.activities

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.pentabytex.alshafimedledger.adapter.SaleItemAdapter
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.databinding.ActivityReturnMedicinesBinding
import com.pentabytex.alshafimedledger.databinding.FragmentConfirmationBinding
import com.pentabytex.alshafimedledger.enums.PaymentStatus
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.utils.Utils.formatDate
import com.pentabytex.alshafimedledger.viewmodels.CustomerViewModel
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import com.pentabytex.alshafimedledger.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReturnMedicinesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReturnMedicinesBinding

    private val customerViewModel: CustomerViewModel by viewModels()
    private val medicinesViewModel: MedicineViewModel by viewModels()
    private val saleViewModel: SaleViewModel by viewModels()

    private lateinit var saleItemAdapter: SaleItemAdapter
    private lateinit var saleData: Sale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReturnMedicinesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        getData()
        setUpUI()
        setRecyclerView()
        setObservers()
    }

    private fun setUpUI() {
        binding.apply {
            toolbar.apply {
                backTitleTV.text = "Sale Detail Screen"
                backIV.setOnClickListener { finish() }
            }

            buttonDelete.setOnClickListener { showDeleteDialog(saleData.saleId) }
            actvPaymentStatus.setOnClickListener { actvPaymentStatus.showDropDown() }
            buttonEdit.setOnClickListener { updateStatus() }
        }
    }

    private fun updateStatus() {
        binding.apply {
            val paymentStatus = actvPaymentStatus.text.toString()
            val amountReceived = etPaymentReceived.text.toString().toDouble()
            val notes = etGlobalNote.text.toString()

            val updatedSale = saleData.copy(
                paymentStatus = paymentStatus,
                amountReceived = amountReceived,
                notes = notes,
                updatedTimestamp = System.currentTimeMillis()
            )

            saleViewModel.updateSale(updatedSale)
        }
    }

    private fun showDeleteDialog(saleId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete sale record")
            .setMessage("Are you sure you want to delete this sale record ${saleId}?")
            .setPositiveButton("Delete") { _, _ ->
                saleViewModel.deleteSale(saleId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setRecyclerView() {
        saleItemAdapter = SaleItemAdapter()
        binding.apply {
            rvMedicines.layoutManager = LinearLayoutManager(this@ReturnMedicinesActivity)
            rvMedicines.adapter = saleItemAdapter


            val statusAdapter = ArrayAdapter(
                this@ReturnMedicinesActivity,
                R.layout.simple_dropdown_item_1line,
                PaymentStatus.entries.filter { it != PaymentStatus.ALL }.map { it.displayName }
            )
           actvPaymentStatus.setAdapter(statusAdapter)
        }
    }

    private fun setObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                customerViewModel.customer.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            populateData(resource.data)
                        }
                        is Resource.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, resource.message)
                        }
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.sale.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            saleItemAdapter.submitList(resource.data.saleItems)
                        }
                        is Resource.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, resource.message)
                        }
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.updateSaleState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, "sale record is updated!")
                        }
                        is Resource.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, resource.message)
                        }
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.deleteSaleState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, "sale record is deleted!")
                            finish()
                        }
                        is Resource.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, resource.message)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun populateData(customer: Customer) {
        binding.apply {
            tvCustomerName.text = "Name: ${customer.name}"
            tvCustomerPhone.text = "Phone: ${customer.phone}"
            tvCustomerAddress.text = "Address: ${customer.address}"

            etPaymentReceived.setText(saleData.amountReceived.toString())
            actvPaymentStatus.setText(saleData.paymentStatus)
            etGlobalNote.setText(saleData.notes)
            val totalPrice = saleData.saleItems.sumOf { it.totalSellingPrice }
            tvTotalPrice.text = "Total Price: Rs. $totalPrice"
            textTimestamps.text = "Added: ${formatDate(saleData.timestamp)}\nLast Updated: ${formatDate(saleData.updatedTimestamp)}"
        }
    }

    private fun getData() {
        saleData = intent.getParcelableExtra(TRANSFER_DATA)!!
        saleData?.let {
            customerViewModel.getCustomerById(saleData.customerId)
            saleViewModel.getSaleById(saleData.saleId)
        }
    }
}