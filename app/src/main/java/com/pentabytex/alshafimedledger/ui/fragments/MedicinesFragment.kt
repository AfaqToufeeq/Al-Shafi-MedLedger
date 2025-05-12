package com.pentabytex.alshafimedledger.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicinesFragment : Fragment() {

    private var _binding: FragmentMedicinesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MedicineAdapter

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
        setupRecyclerView()
        observeMedicines()
    }

    private fun setUpUI() {
        binding.apply {
            toolbar.backTitleTV.text = "Medicines"
            toolbar.backIV.setOnClickListener { findNavController().popBackStack() }
        }
    }

    private fun setupRecyclerView() {
        adapter = MedicineAdapter(
            onItemClick = { medicine ->
                navigationToMedicineDetails(medicine)
            },
            onDeleteClick = { medicine ->
                showDeleteDialog(medicine)
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
        Utils.navigateToActivity(requireContext(), MedicineDetailsActivity::class.java, isAnimation = true, extras = bundle)
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

    private fun observeMedicines() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.medicines.collect { resource ->
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
