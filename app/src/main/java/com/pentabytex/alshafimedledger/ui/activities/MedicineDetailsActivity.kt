package com.pentabytex.alshafimedledger.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ActivityMedicineDetailsBinding
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Constants.STOCK_LIMIT
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.utils.Utils.formatDate
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedicineDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineDetailsBinding
    private val medicineViewModel: MedicineViewModel by viewModels()
    private var medicineToEdit: Medicine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMedicineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        getIntentData()
        setListeners()
        observeDeleteState()
    }

    private fun setListeners() = with(binding) {
        buttonEdit.setOnClickListener { navigateToEditMedicine() }
        buttonDelete.setOnClickListener { medicineToEdit?.let { showDeleteDialog(it) } }
        toolbar.backIV.setOnClickListener { finish() }
    }

    private fun navigateToEditMedicine() {
        medicineToEdit?.let {
            val bundle = Bundle().apply { putParcelable(TRANSFER_DATA, it) }
            navigateToActivity(
                this@MedicineDetailsActivity,
                AddMedicineActivity::class.java,
                isAnimation = true,
                extras = bundle
            )
        }
    }

    private fun getIntentData() {
        medicineToEdit = intent.getParcelableExtra(TRANSFER_DATA)
        medicineToEdit?.let { med ->
            setupMedicineUI(med)
        }
    }

    private fun setupMedicineUI(med: Medicine) = with(binding) {
        toolbar.backTitleTV.text = "Medicine Details"

        textMedicineName.text = med.name
        textMedicineCode.text = "${med.type} • ${med.volume}"

        val remainingStock = med.totalStock - med.soldStock
        val totalStockProgress = if (med.totalStock > 0) 100 else 0
        val soldStockProgress = if (med.totalStock > 0) (med.soldStock * 100 / med.totalStock) else 0
        val remainingStockProgress = if (med.totalStock > 0) (remainingStock * 100 / med.totalStock) else 0

        textTotalStockCount.text = "${med.totalStock} Medicines"
        progressTotalStock.progress = totalStockProgress

        textStockSoldCount.text = "${med.soldStock} Sold"
        progressStockSold.progress = soldStockProgress

        textStockCount.text = "$remainingStock Remaining"
        progressStockRemaining.progress = remainingStockProgress

        val profit = med.sellingPrice - med.purchasePrice
        val profitPercentage = if (med.purchasePrice != 0.0) (profit / med.purchasePrice) * 100 else 0.0

        textProfitPrice.text = "PKR %.2f".format(profit)
        textProfitPercent.text = "${"%.1f".format(profitPercentage)}% Sold"


        val alertMessage = if (remainingStock <= STOCK_LIMIT) {
            "⚠️ $remainingStock medicine packs left"
        } else {
            "ℹ️ Stock level is normal."
        }
        val outOfStockNotification = if (remainingStock == 0) "Out of Stock!" else null

        textSetAlert.text = alertMessage
        textOutOfStock.apply {
            visibility = if (outOfStockNotification == null) View.GONE else View.VISIBLE
            text = outOfStockNotification
        }

        textNotes.text = med.notes
        textCostPrice.text = "PKR ${med.purchasePrice}"
        textTotalSalePrice.text = "PKR ${med.sellingPrice}"
        textPrices.text = "Purchase: PKR ${med.purchasePrice} | Sell: PKR ${med.sellingPrice}"
        textSoldQuantity.text = "Total Sold: ${med.soldStock} medicines"

        textTimestamps.text = "Added: ${formatDate(med.createdAt)}\nLast Updated: ${formatDate(med.updatedAt)}"
    }

    private fun showDeleteDialog(medicine: Medicine) {
        AlertDialog.Builder(this)
            .setTitle("Delete Medicine")
            .setMessage("Are you sure you want to delete ${medicine.name}?")
            .setPositiveButton("Delete") { _, _ ->
                medicineViewModel.deleteMedicine(medicine.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeDeleteState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                medicineViewModel.deleteMedicineState.collect { state ->
                    when (state) {
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            showSnackbar("Medicine record deleted.")
                            finish()
                        }
                        is Resource.Error -> showSnackbar(state.message)
                        Resource.Idle -> Unit
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
