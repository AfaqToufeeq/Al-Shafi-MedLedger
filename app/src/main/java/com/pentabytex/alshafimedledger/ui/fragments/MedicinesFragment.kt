package com.pentabytex.alshafimedledger.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.adapter.MedicineAdapter
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.FragmentMedicinesBinding
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.ui.activities.AddMedicineActivity
import com.pentabytex.alshafimedledger.ui.activities.MedicineDetailsActivity
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicinesFragment : Fragment() {

    private var _binding: FragmentMedicinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MedicineAdapter
    private var isSelectionMode = false
    private val selectedMedicines = mutableListOf<Medicine>()


    private val viewModel: MedicineViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setUpUI()
        setupSearch()
        setupRecyclerView()
        observeMedicines()
    }

    private fun setUpUI() {
        binding.apply {
            toolbar.backTitleTV.text = "Medicines"
            toolbar.backIV.setOnClickListener { findNavController().popBackStack() }
            selectionActionsLayout.ivCloseSelection.setOnClickListener { toggleSelectionMode(false, medicine = Medicine()) }
            selectionActionsLayout.ivMoreOptions.setOnClickListener {
                showSelectionOptionsMenu(it)

                Log.d("MedicinesFragmentSelection", "Selected Count: ${selectedMedicines.size}")
                selectedMedicines.forEach {
                    Log.d("MedicinesFragmentSelection", "Selected Medicine: ${it.name}")
                }
            }
        }
    }

    private fun showSelectionOptionsMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_medicine_toolbar, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    deleteMedicinesBulk()
                    true
                }
                R.id.action_select_customers -> {
                    addMedicinesToCustomers()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun addMedicinesToCustomers() {
        val selectedMedicineIds = selectedMedicines.map { it.id }

    }


    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchMedicines(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setupRecyclerView() {
        adapter = MedicineAdapter(
            onItemClick = { medicine ->
                navigationToMedicineDetails(medicine)
            },
            onDeleteClick = { medicine ->
                showDeleteDialog(medicine)
            },
            onItemLongClick = { medicine ->
                if (!isSelectionMode) {
                    toggleSelectionMode(true, medicine)
                    selectedMedicines.add(medicine)
                    updateSelectedCount()
                }
            },
            onSelectionChanged = { medicine, isChecked ->
                if (isChecked) {
                    if (!selectedMedicines.any { it.id == medicine.id }) {
                        selectedMedicines.add(medicine)
                    }
                } else {
                    selectedMedicines.removeAll { it.id == medicine.id }
                }
                updateSelectedCount()
            }

        )

        binding.apply {
            medicinesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            medicinesRecyclerView.adapter = adapter
        }
    }

    private fun navigationToMedicineDetails(medicine: Medicine) {
        val bundle = Bundle().apply {
            putParcelable(TRANSFER_DATA, medicine)
        }
        navigateToActivity(requireContext(), MedicineDetailsActivity::class.java, isAnimation = true, extras = bundle)
    }

    private fun showDeleteDialog(medicine: Medicine) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Medicine")
            .setMessage("Are you sure you want to delete this ${medicine.name} medicine?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMedicine(medicine.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteMedicinesBulk() {
        if (selectedMedicines.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Medicines")
            .setMessage("Are you sure you want to delete ${selectedMedicines.size} selected item(s)?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMedicinesBulk(selectedMedicines)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun observeMedicines() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterMedicines.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.loadingOverlay.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            adapter.submitList(resource.data)
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
                viewModel.deleteMedicineState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.loadingOverlay.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            binding.selectionActionsLayout.ivCloseSelection.performClick()
                           Utils.showSnackbar(binding.root,"Medicine record is deleted!")
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

    private fun toggleSelectionMode(enable: Boolean, medicine: Medicine) {
        isSelectionMode = enable
        binding.selectionActionsLayout.root.visibility = if (enable) View.VISIBLE else View.GONE
        binding.toolbar.root.visibility = if (enable) View.GONE else View.VISIBLE
        adapter.setSelectionMode(enable, medicine)
        if (!enable) {
            selectedMedicines.clear()
            adapter.setSelectionMode(false, medicine) // Disable and clear selection
        }
        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        binding.selectionActionsLayout.tvSelectedCount.text = "${selectedMedicines.size} selected"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
