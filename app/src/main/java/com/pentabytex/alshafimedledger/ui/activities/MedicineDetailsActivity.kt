package com.pentabytex.alshafimedledger.ui.activities

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ActivityMedicineDetailsBinding
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.Constants.STOCK_LIMIT
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.utils.RsFormatHelper
import com.pentabytex.alshafimedledger.utils.Utils.formatDate
import com.pentabytex.alshafimedledger.utils.Utils.navigateToActivity
import com.pentabytex.alshafimedledger.viewmodels.MedicineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MedicineDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineDetailsBinding
    private val viewModel: MedicineViewModel by viewModels()
    private var medicine: Medicine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMedicineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)

        getIntentData()
        setupUI()
        setupListeners()
        observeDeleteState()
    }

    private fun setupUI() {
        binding.toolbar.backTitleTV.text = "Medicine Details"
    }

    private fun setupListeners() = with(binding) {
        toolbar.backIV.setOnClickListener { finish() }
        buttonEdit.setOnClickListener { navigateToEditScreen() }
        buttonDelete.setOnClickListener { medicine?.let { showDeleteDialog(it) } }
        buttonAdjustQuantity.setOnClickListener { navigateToSalesDetails() }
        btnAddStock.setOnClickListener { addNewStock() }

    }

    private fun addNewStock() {
        binding.apply {
            val addStockStr = edtAddStock.text.toString()
            val addedStock = addStockStr.toIntOrNull()
            if (addedStock != null && addedStock > 0 && medicine != null) {
                medicine?.let { medicine ->
                    val existingTotalStock = medicine.totalStock
                    val existingPurchasePrice = medicine.purchasePrice
                    val existingPricePerUnit = medicine.purchasePricePerUnit

                    // Calculate new purchase amount
                    val newPurchase = addedStock * existingPricePerUnit

                    val updatedTotalStock = existingTotalStock + addedStock
                    val updatedTotalPurchasePrice = existingPurchasePrice + newPurchase

                    val updatedMedicine = medicine.copy(
                        totalStock = updatedTotalStock,
                        purchasePrice = updatedTotalPurchasePrice,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateMedicine(updatedMedicine)
                    populateUI(updatedMedicine)
                }
                edtAddStock.text?.clear()
                showSnackbar("$addedStock units added to stock.")
            } else {
                showSnackbar("Please enter a valid stock number.")
            }
        }
    }

    private fun navigateToSalesDetails() {
        val list = if (medicine != null) arrayListOf(medicine) else arrayListOf<Medicine>()
        navigateToActivity(
            this,
            NewSaleActivity::class.java,
            extras = Bundle().apply {
                putParcelableArrayList(TRANSFER_DATA, list)
            }
        )
    }


    private fun getIntentData() {
        medicine = intent.getParcelableExtra(TRANSFER_DATA)
        medicine?.let { populateUI(it) }
    }

    private fun populateUI(med: Medicine) = with(binding) {
        textMedicineName.text = med.name
        textMedicineCode.text = "${med.type} • ${med.volume}"

        setupStockProgress(med)
        setupPricing(med)
        setupAlerts(med)

        textNotes.text = med.notes
        textSoldQuantity.text = "Total Sold: ${med.soldStock} units"
        textTimestamps.text = "Added: ${formatDate(med.createdAt)}\nLast Updated: ${formatDate(med.updatedAt)}"
    }

    private fun setupStockProgress(med: Medicine) = with(binding) {
        val remaining = med.totalStock - med.soldStock
        val total = med.totalStock.coerceAtLeast(1)

        textTotalStockCount.text = "${med.totalStock} Medicines"
        progressTotalStock.progress = 100

        textStockSoldCount.text = "${med.soldStock} Sold"
        progressStockSold.progress = (med.soldStock * 100 / total)

        textStockCount.text = "$remaining Remaining"
        progressStockRemaining.progress = (remaining * 100 / total)
    }

    private fun setupPricing(med: Medicine) = with(binding) {
        val totalStock = med.totalStock.coerceAtLeast(1)

        val pricePerUnit = med.purchasePricePerUnit
        val sellPerUnit = med.sellingPrice / totalStock

        val profit = med.sellingPrice - med.purchasePrice
        val profitPerUnit = sellPerUnit - pricePerUnit
        val profitPercent = if (med.purchasePrice != 0.0) (profit / med.purchasePrice) * 100 else 0.0

        textPricePerUnit.text = RsFormatHelper.formatPrice(pricePerUnit, 2)
        textSalePerUnit.text = RsFormatHelper.formatPrice(sellPerUnit, 2)
        textProfitPerUnit.text = RsFormatHelper.formatPrice(profitPerUnit, 2)
        textProfitPrice.text = RsFormatHelper.formatPrice(profit, 2)

        val (profitLabel, profitDrawable) = when {
            sellPerUnit > pricePerUnit -> "Profit" to R.drawable.ic_profit
            sellPerUnit < pricePerUnit -> "Loss" to R.drawable.ic_loss
            else -> "Break-Even" to R.drawable.ic_balance
        }

        textProfitPercent.text = RsFormatHelper.formatPercent(profitPercent, profitLabel)
        textProfitPercent.setCompoundDrawablesWithIntrinsicBounds(0, 0, profitDrawable, 0)
        textProfitPercent.compoundDrawablePadding = 8

        textCostPrice.text = RsFormatHelper.formatPrice(med.purchasePrice, 0)
        textTotalSalePrice.text = RsFormatHelper.formatPrice(med.sellingPrice, 0)
        textPrices.text = "Purchase: ${RsFormatHelper.formatPrice(med.purchasePrice, 2)} | Sell: ${RsFormatHelper.formatPrice(med.sellingPrice, 2)}"

        val profitColor = if (sellPerUnit > pricePerUnit) {
            getColor(android.R.color.holo_green_dark)
        } else {
            getColor(android.R.color.holo_red_dark)
        }

        textProfitPerUnit.setTextColor(profitColor)
        textProfitPrice.setTextColor(profitColor)
        textProfitPercent.setTextColor(profitColor)
    }


    private fun setupAlerts(med: Medicine) = with(binding) {
        val remaining = med.totalStock - med.soldStock

        textSetAlert.text = if (remaining <= STOCK_LIMIT) {
            "⚠️ Only $remaining units left in stock!"
        } else {
            "ℹ️ Stock level is normal."
        }

        textOutOfStock.apply {
            visibility = if (remaining == 0) View.VISIBLE else View.GONE
            text = if (remaining == 0) "Out of Stock!" else ""
        }
    }

    private fun navigateToEditScreen() {
        medicine?.let {
            val bundle = Bundle().apply { putParcelable(TRANSFER_DATA, it) }
            navigateToActivity(
                this@MedicineDetailsActivity,
                AddMedicineActivity::class.java,
                isAnimation = true,
                extras = bundle,
                finishCurrentActivity = true
            )
        }
    }

    private fun showDeleteDialog(med: Medicine) {
        AlertDialog.Builder(this)
            .setTitle("Delete Medicine")
            .setMessage("Are you sure you want to delete ${med.name}?")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteMedicine(med.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeDeleteState() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteMedicineState.collect { state ->
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
