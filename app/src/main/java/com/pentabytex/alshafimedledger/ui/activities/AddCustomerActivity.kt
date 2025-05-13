package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.databinding.ActivityAddCustomerBinding
import com.pentabytex.alshafimedledger.enums.DashboardTitle
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.viewmodels.CustomerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCustomerBinding
    private val customerViewModel: CustomerViewModel by viewModels()

    private var isEditMode = false
    private var customerToEdit: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        getIntentData()
        setupUI()
        observeSaveState()
    }

    private fun getIntentData() {
        customerToEdit = intent.getParcelableExtra(TRANSFER_DATA)
        isEditMode = customerToEdit != null

        customerToEdit?.let { customer ->
            binding.apply {
                toolbar.backTitleTV.text = getString(R.string.update_customer)
                edtCustomerName.setText(customer.name)
                edtCustomerPhone.setText(customer.phone)
                edtCustomerAddress.setText(customer.address)
                btnSaveCustomer.text = getString(R.string.update_customer)
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            toolbar.apply {
                if (!isEditMode) backTitleTV.text = DashboardTitle.AddCustomers.title
                backIV.setOnClickListener { finish() }
            }

            btnSaveCustomer.setOnClickListener {
                if (isFormValid()) {
                    if (isEditMode) updateCustomerData(customerToEdit!!.id)
                    else saveCustomerData()
                } else {
                    showError("Please fill all required fields.")
                }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val name = binding.edtCustomerName.text.toString().trim()
        val phone = binding.edtCustomerPhone.text.toString().trim()
        return name.isNotEmpty() && phone.isNotEmpty()
    }

    private fun saveCustomerData() {
        val name = binding.edtCustomerName.text.toString().trim()
        val phone = binding.edtCustomerPhone.text.toString().trim()
        val address = binding.edtCustomerAddress.text.toString().trim()

        val customer = Customer(
            id = System.currentTimeMillis().toString(),
            name = name,
            phone = phone,
            address = address
        )

        customerViewModel.saveCustomer(customer)
    }

    private fun updateCustomerData(customerId: String) {
        val updatedCustomer = Customer(
            id = customerId,
            name = binding.edtCustomerName.text.toString().trim(),
            phone = binding.edtCustomerPhone.text.toString().trim(),
            address = binding.edtCustomerAddress.text.toString().trim(),
            createdAt = customerToEdit?.createdAt ?: System.currentTimeMillis()
        )

        customerViewModel.updateCustomer(updatedCustomer)
    }

    private fun observeSaveState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                customerViewModel.saveCustomerState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.btnSaveCustomer.isEnabled = false
                        }
                        is Resource.Success -> {
                            showSuccess("Customer saved successfully!")
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnSaveCustomer.isEnabled = true
                            showError(state.message)
                        }
                        Resource.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
