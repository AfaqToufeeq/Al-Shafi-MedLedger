package com.pentabytex.alshafimedledger.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.adapter.CustomerAdapter
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.databinding.FragmentCustomersBinding
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.ui.activities.AddCustomerActivity
import com.pentabytex.alshafimedledger.ui.activities.NewSaleActivity
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Utils
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.utils.Utils.showToast
import com.pentabytex.alshafimedledger.viewmodels.CustomerViewModel
import com.pentabytex.alshafimedledger.viewmodels.NewSaleSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomersFragment : Fragment() {

    private var _binding: FragmentCustomersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CustomerAdapter
    private var isSelectionMode = false
    private var flagVisibility: Boolean? = null
    private val selectedCustomers = mutableListOf<Customer>()

    private val viewModel: CustomerViewModel by viewModels()
    private val newSaleViewModel: NewSaleSharedViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        getIntentData()
        setUpUI()
        setupSearch()
        observeCustomers()
        setupRecyclerView()
    }

    private fun getIntentData() {
        flagVisibility = arguments?.getBoolean(TRANSFER_DATA)
        if (flagVisibility == true)
            binding.btnNext.visibility = View.VISIBLE
        else
            binding.btnNext.visibility = View.GONE
    }

    private fun setUpUI() {
        binding.apply {
            toolbar.backTitleTV.text = "Customers"
            toolbar.backIV.setOnClickListener { findNavController().popBackStack() }

            selectionActionsLayout.ivCloseSelection.setOnClickListener {
                toggleSelectionMode(false, customer = Customer())
            }
            selectionActionsLayout.ivMoreOptions.setOnClickListener {
                showSelectionOptionsMenu(it)
            }

            btnNext.setOnClickListener {
                navigateToConfirmationScreen()
            }
        }
    }

    private fun showSelectionOptionsMenu(anchor: View) {
        val popupMenu = PopupMenu(requireContext(), anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_customer_toolbar, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_select_all -> {
                    selectAllmedicines(true, Customer())
                    true
                }
                R.id.action_delete -> {
                    deleteCustomersBulk()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteCustomersBulk() {
        if (selectedCustomers.isEmpty()) return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Customers")
            .setMessage("Are you sure you want to delete ${selectedCustomers.size} selected item(s)?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCustomersBulk(selectedCustomers)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToConfirmationScreen() {
        if (selectedCustomers.isNotEmpty()) {
            newSaleViewModel.selectedCustomer.value = selectedCustomers.first()
            findNavController().navigate(R.id.action_customersFragment2_to_confirmationFragment)
        } else {
            showToast(requireContext(), "Please select at least one customer before proceeding")
        }
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchCustomers(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            flagVisibility,

            onItemClick = { customer ->
                navigateToCustomerDetails(customer)
            },
            onDeleteClick = { customer ->
                showDeleteDialog(customer)
            } ,
            onItemLongClick = { customer ->
                if (!isSelectionMode) {
                    toggleSelectionMode(true, customer)
                    selectedCustomers.add(customer)
                    updateSelectedCount()
                }
            },
            onSelectionChanged = { customer, isChecked ->
                if (isChecked) {
                    if (!selectedCustomers.any { it.id == customer.id }) {
                        selectedCustomers.add(customer)
                    }
                } else {
                    selectedCustomers.removeAll { it.id == customer.id }
                }
                updateSelectedCount()
            }
        )

        binding.apply {
            customersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            customersRecyclerView.adapter = adapter
        }
    }

    private fun navigateToCustomerDetails(customer: Customer) {
        val bundle = Bundle().apply {
            putParcelable(TRANSFER_DATA, customer)
        }
        navigateToActivity(
            requireContext(),
            AddCustomerActivity::class.java,
            isAnimation = true,
            extras = bundle
        )
    }

    private fun showDeleteDialog(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Customer")
            .setMessage("Are you sure you want to delete customer ${customer.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCustomer(customer.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeCustomers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterCustomers.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
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
                viewModel.deleteCustomerState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.loadingOverlay.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Utils.showSnackbar(binding.root, "Customer record is deleted!")
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


    private fun toggleSelectionMode(enable: Boolean, customer: Customer) {
        isSelectionMode = enable
        binding.selectionActionsLayout.root.visibility = if (enable) View.VISIBLE else View.GONE
        binding.toolbar.root.visibility = if (enable) View.GONE else View.VISIBLE
        adapter.setSelectionMode(enable, customer)
        if (!enable) {
            selectedCustomers.clear()
            adapter.setSelectionMode(false, customer) // Disable and clear selection
        }
        updateSelectedCount()
    }

    private fun selectAllmedicines(enable: Boolean, customer: Customer) {
        if (!isSelectionMode) {
            toggleSelectionMode(true, customer)
        }

        adapter.selectAll(enable)

        selectedCustomers.clear()
        if (enable) {
            selectedCustomers.addAll(adapter.currentList)
        }

        updateSelectedCount()
    }

    private fun updateSelectedCount() {
        binding.selectionActionsLayout.tvSelectedCount.text = "${selectedCustomers.size} selected"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
