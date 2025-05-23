package com.pentabytex.alshafimedledger.ui.fragments

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pentabytex.alshafimedledger.adapter.ConfirmationAdapter
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.databinding.FragmentConfirmationBinding
import com.pentabytex.alshafimedledger.enums.PaymentStatus
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.viewmodels.NewSaleSharedViewModel
import com.pentabytex.alshafimedledger.viewmodels.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConfirmationFragment : Fragment() {

    private var _binding: FragmentConfirmationBinding? = null
    private val binding get() = _binding!!

    private val saleSharedViewModel: NewSaleSharedViewModel by activityViewModels()
    private val saleViewModel: SaleViewModel by viewModels()

    private lateinit var adapter: ConfirmationAdapter
    private var totalPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUI()
        setupRecyclerView()
        observeData()
    }

    private fun setUpUI() {
        binding.apply {
            toolbar.apply {
                backIV.setOnClickListener { findNavController().popBackStack() }
                backTitleTV.text = "Confirmation Details"
            }

            btnConfirm.setOnClickListener { confirmData() }
            actvPaymentStatus.setOnClickListener { actvPaymentStatus.showDropDown() }

        }
    }

    private fun confirmData() {
        val notes = binding.etGlobalNote.text.toString().ifBlank { "No notes" }
        val receivedAmount = binding.etPaymentReceived.text.toString().toDoubleOrNull() ?: 0.0
        val selectedStatusStr = binding.actvPaymentStatus.text.toString()
        val selectedStatus = PaymentStatus.fromString(selectedStatusStr)?.displayName ?: PaymentStatus.PENDING.displayName

        val updatedSales = saleSharedViewModel.selectedCustomer.value?.let {
            Sale(
                customerId = it.id,
                customerName =  it.name,
                saleItems =  saleSharedViewModel.saleItemDetailsList.value ?: emptyList(),
                notes = notes,
                totalPrice = totalPrice,
                amountReceived = receivedAmount,
                paymentStatus = selectedStatus
            )
        }

        updatedSales?.let { saleViewModel.saveSale(it) }

    }

    private fun setupRecyclerView() {
        adapter = ConfirmationAdapter()
        binding.rvMedicines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMedicines.adapter = adapter

        val statusAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_dropdown_item_1line,
            PaymentStatus.entries.filter { it != PaymentStatus.ALL }.map { it.displayName }
        )
        binding.actvPaymentStatus.setAdapter(statusAdapter)
    }

    private fun observeData() {
        saleSharedViewModel.selectedCustomer.observe(viewLifecycleOwner) { customer ->
            binding.tvCustomerName.text = "Name: ${customer.name}"
            binding.tvCustomerPhone.text = "Phone: ${customer.phone}"
            binding.tvCustomerAddress.text = "Address: ${customer.address}"
        }

        saleSharedViewModel.saleItemDetailsList.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
        }

        saleSharedViewModel.saleItemDetailsList.observe(viewLifecycleOwner) { sales ->
            totalPrice = sales.sumOf { it.totalSellingPrice }
            binding.tvTotalPrice.text = "Total Price: Rs. $totalPrice"
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                saleViewModel.saveSaleState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.loadingOverlay.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, "Data is saved successfully!")
                            requireActivity().finish()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
