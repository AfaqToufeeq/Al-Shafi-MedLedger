package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ActivityAddMedicineBinding
import com.pentabytex.alshafimedledger.enums.DashboardTitle
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddMedicineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMedicineBinding
    private val medicineViewModel: MedicineViewModel by viewModels()

    private var isEditMode: Boolean = false
    private var medicineToEdit: Medicine? = null

    private val medicineTypes = arrayOf("Tablet", "Syrup", "Injection", "Capsule", "Vaccine", "Cream")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        getIntentData()
        setUpUI()
        setUpSpinner()
        observeSaveState()
    }

    private fun getIntentData() {
        medicineToEdit = intent.getParcelableExtra(TRANSFER_DATA)
        isEditMode = medicineToEdit != null

        medicineToEdit?.let { med ->
            binding.apply {
                toolbar.backTitleTV.text = getString(R.string.update_medicine)

                edtMedicineName.setText(med.name)
                edtVolume.setText(med.volume)
                edtNotes.setText(med.notes)
                edtTotalStock.setText(med.totalStock.toString())
                edtSoldStock.setText(med.soldStock.toString())
                edtPurchasePrice.setText(med.purchasePrice.toString())
                edtSellingPrice.setText(med.sellingPrice.toString())

                spinnerMedicineType.setText(med.type, false)
                btnSaveMedicine.text = getString(R.string.update_medicine)
            }
        }

    }

    private fun setUpUI() {
        binding.apply {
            toolbar.apply {
                if (!isEditMode) backTitleTV.text = DashboardTitle.AddMedicines.title

                backIV.setOnClickListener { finish() }
            }

            btnSaveMedicine.setOnClickListener {
                if (isFormValid()) {
                    if (isEditMode) updateMedicineData(medicineToEdit!!.id)
                    else saveMedicineData()

                } else {
                    showError("Please fill all required fields.")
                }
            }

            edtMedicineName.addTextChangedListener { validateForm() }
            edtVolume.addTextChangedListener { validateForm() }
            edtTotalStock.addTextChangedListener { validateForm() }
            edtPurchasePrice.addTextChangedListener { validateForm() }

            spinnerMedicineType.setOnClickListener { spinnerMedicineType.showDropDown() }
        }
    }


    private fun setUpSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, medicineTypes)
        binding.spinnerMedicineType.setAdapter(adapter)
    }


    private fun isFormValid(): Boolean {
        val medicineName = binding.edtMedicineName.text.toString().trim()
        val volume = binding.edtVolume.text.toString().trim()
        val totalStock = binding.edtTotalStock.text.toString().trim()
        val purchasePrice = binding.edtPurchasePrice.text.toString().trim()
        val medicineType = binding.spinnerMedicineType.text.toString().trim()

        return !(medicineName.isEmpty() || volume.isEmpty() || totalStock.isEmpty()
                || purchasePrice.isEmpty() || medicineType.isEmpty())
    }

    // Handle the saving of the medicine data
    private fun saveMedicineData() {
        val medicineName = binding.edtMedicineName.text.toString().trim()
        val medicineType = binding.spinnerMedicineType.text.toString().trim()
        val volume = binding.edtVolume.text.toString().trim()
        val notes = binding.edtNotes.text.toString().trim()
        val totalStock = binding.edtTotalStock.text.toString().trim().toInt()
        val soldStock = binding.edtSoldStock.text.toString().trim().toInt()
        val purchasePrice = binding.edtPurchasePrice.text.toString().trim().toDouble()
        val sellingPrice = binding.edtSellingPrice.text.toString().trim().toDouble()

        val medicine = Medicine(
            id = System.currentTimeMillis().toString(),
            name = medicineName,
            type = medicineType,
            volume = volume,
            notes = notes,
            totalStock = totalStock,
            soldStock = soldStock,
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice
        )

        medicineViewModel.saveMedicine(medicine)
    }

    // Handle the update of the medicine data
    private fun updateMedicineData(medicineId: String) {
        val updatedMedicine = Medicine(
            id = medicineId,
            name = binding.edtMedicineName.text.toString().trim(),
            type = binding.spinnerMedicineType.text.toString().trim(),
            volume = binding.edtVolume.text.toString().trim(),
            notes = binding.edtNotes.text.toString().trim(),
            soldStock = binding.edtSoldStock.text.toString().trim().toInt(),
            totalStock = binding.edtTotalStock.text.toString().trim().toInt(),
            purchasePrice = binding.edtPurchasePrice.text.toString().trim().toDouble(),
            sellingPrice = binding.edtSellingPrice.text.toString().trim().toDouble(),
            createdAt = medicineToEdit?.createdAt ?: System.currentTimeMillis(),
        )

        medicineViewModel.updateMedicine(updatedMedicine)
    }


    private fun validateForm() {
        binding.btnSaveMedicine.isEnabled = isFormValid()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    private fun observeSaveState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                medicineViewModel.saveMedicineState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.btnSaveMedicine.isEnabled = false
                        }
                        is Resource.Success -> {
                            showSuccess("Medicine saved successfully!")
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnSaveMedicine.isEnabled = true
                            showError(state.message)
                        }
                        Resource.Idle -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                medicineViewModel.updateMedicineState.collect { state ->
                    when (state) {
                        is Resource.Loading -> {
                            binding.btnSaveMedicine.isEnabled = false
                        }
                        is Resource.Success -> {
                            showSuccess("Medicine updated successfully!")
                            finish()
                        }
                        is Resource.Error -> {
                            binding.btnSaveMedicine.isEnabled = true
                            showError(state.message)
                        }
                        Resource.Idle -> Unit
                    }
                }
            }
        }
    }
}
