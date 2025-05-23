package com.pentabytex.alshafimedledger.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.adapter.SaleMedicineAdapter
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.databinding.FragmentSaleDetailsBinding
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.viewmodels.NewSaleSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SaleDetailsFragment : Fragment() {

    private var _binding: FragmentSaleDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewSaleSharedViewModel by activityViewModels()
    private lateinit var adapter: SaleMedicineAdapter
    private val saleItemList = mutableListOf<SaleItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.backTitleTV.text = "Adjust Quantity"
        setupRecyclerView()
        observeMedicines()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener { navigateToCustomersScreen() }
        binding.toolbar.backIV.setOnClickListener { requireActivity().finish() }
    }


    private fun navigateToCustomersScreen() {
        if (saleItemList.any { it.quantitySold <= 0 || it.sellingPricePerUnit <= 0 }) {
            Utils.showSnackbar(binding.root, "Please set quantity and price for all items before proceeding")
            return
        }

        viewModel.saleItemDetailsList.value = saleItemList

        val bundle = Bundle().apply {
            putBoolean(TRANSFER_DATA, true)
        }
        findNavController().navigate(R.id.action_saleDetailsFragment_to_customersFragment2, bundle)
    }


    private fun observeMedicines() {
        viewModel.selectedMedicines.observe(viewLifecycleOwner) { medicines ->
            adapter.submitList(medicines)
            saleItemList.apply {
                clear()
                addAll(medicines.map { SaleItem(medicineId = it.id) })
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = SaleMedicineAdapter(
            onSaleUpdated = { index, sale ->
                if (index in saleItemList.indices) saleItemList[index] = sale
            },
            onDeleteClick = { medicine ->
                // Remove from sale list
                saleItemList.removeAll { it.medicineId == medicine.id }

                val updatedMedicines = adapter.currentList.filterNot { it.id == medicine.id }
                adapter.submitList(updatedMedicines)
            }
        )

        binding.medicineRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SaleDetailsFragment.adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
