package com.pentabytex.alshafimedledger.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.pentabytex.alshafimedledger.adapter.SaleAdapter
import com.pentabytex.alshafimedledger.databinding.FragmentSalesBinding
import com.pentabytex.alshafimedledger.enums.PaymentStatus
import com.pentabytex.alshafimedledger.enums.SaleSortType
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.ui.activities.ReturnMedicinesActivity
import com.pentabytex.alshafimedledger.utils.Constants
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SalesFragment : Fragment() {

    private var _binding: FragmentSalesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SaleViewModel by viewModels()
    private lateinit var saleAdapter: SaleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUI()
        setupRecyclerView()
        setupSearch()
        observeSales()
    }

    private fun setUpUI() {
        binding.toolbar.apply {
            backTitleTV.text = "Sales History"
            backIV.setOnClickListener { findNavController().popBackStack() }
            binding.btnFilter.setOnClickListener { showFilterDialog() }

        }
    }

    private fun showFilterDialog() {
        val appliedFilters = buildString {
            if (viewModel.currentSearchQuery.isNotBlank()) append("Search: '${viewModel.currentSearchQuery}' | ")
            append("Sort: ${when (viewModel.currentSortType) {
                SaleSortType.DATE_DESC -> "Date ↓"
                SaleSortType.DATE_ASC -> "Date ↑"
                SaleSortType.PRICE_DESC -> "Price ↓"
                SaleSortType.PRICE_ASC -> "Price ↑"
                else -> "None"
            }} | ")

            append("Payment: ${when (viewModel.currentPaymentStatus) {
                PaymentStatus.RECEIVED -> "Received"
                PaymentStatus.PENDING -> "Pending"
                PaymentStatus.NOT_RECEIVED -> "Not Received"
                else -> "All"
            }}")
        }

        val options = arrayOf(
            "Sort by Date (Newest First)",
            "Sort by Date (Oldest First)",
            "Filter: Payment Received",
            "Filter: Payment Pending",
            "Sort by Price (High to Low)",
            "Sort by Price (Low to High)",
            "Filter by Date Range",
            "Clear All Filters"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Filters Applied:\n$appliedFilters")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.sortSalesBy(SaleSortType.DATE_DESC)
                    1 -> viewModel.sortSalesBy(SaleSortType.DATE_ASC)
                    2 -> viewModel.filterByPaymentStatus(PaymentStatus.RECEIVED)
                    3 -> viewModel.filterByPaymentStatus(PaymentStatus.PENDING)
                    4 -> viewModel.sortSalesBy(SaleSortType.PRICE_DESC)
                    5 -> viewModel.sortSalesBy(SaleSortType.PRICE_ASC)
                    6 -> showDateRangePicker()
                    7 -> viewModel.clearAllFilters()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()

        picker.show(parentFragmentManager, picker.toString())

        picker.addOnPositiveButtonClickListener { selection ->
            val startMillis = selection.first ?: return@addOnPositiveButtonClickListener
            val endMillis = selection.second ?: return@addOnPositiveButtonClickListener

            // Call a function in your ViewModel to filter by range
            viewModel.filterByDateRange(startMillis, endMillis)
        }
    }


    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchSales(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setupRecyclerView() {
        saleAdapter = SaleAdapter(
            onItemClick = { sale ->
                val bundle = Bundle().apply {
                    putParcelable(TRANSFER_DATA, sale)
                }
                navigateToActivity(
                    requireActivity(),
                    ReturnMedicinesActivity::class.java,
                    isAnimation = true,
                    extras = bundle
                )
            },
            onDeleteClick = { sale ->
                showDeleteDialog(sale.saleId)
            }
        )
        binding.salesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = saleAdapter
        }
    }

    private fun showDeleteDialog(saleId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete sale record")
            .setMessage("Are you sure you want to delete this sale record ${saleId}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSale(saleId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeSales() {
        lifecycleScope.launch {
            viewModel.filterSales.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.salesRecyclerView.visibility = View.GONE
                        binding.loadingOverlay.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.loadingOverlay.visibility = View.GONE
                        if (resource.data.isNullOrEmpty()) {
                            binding.salesRecyclerView.visibility = View.GONE
                            binding.emptyPlaceholderView.visibility = View.VISIBLE
                            // Optionally show "No Data" message
                        } else {
                            binding.salesRecyclerView.visibility = View.VISIBLE
                            binding.emptyPlaceholderView.visibility = View.GONE
                            saleAdapter.submitList(resource.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.salesRecyclerView.visibility = View.GONE
                        binding.loadingOverlay.visibility = View.GONE
                        Utils.showSnackbar(binding.root, resource.message)
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
