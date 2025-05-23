package com.pentabytex.alshafimedledger.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.databinding.ActivityNewSaleBinding
import com.pentabytex.alshafimedledger.utils.Constants.IntentExtras.TRANSFER_DATA
import com.pentabytex.alshafimedledger.utils.InsetsUtil
import com.pentabytex.alshafimedledger.viewmodels.NewSaleSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewSaleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewSaleBinding

    private val viewModel: NewSaleSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNewSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InsetsUtil.applyInsetsWithInitialPadding(binding.root)


        val selectedMedicines = intent.getParcelableArrayListExtra<Medicine>(TRANSFER_DATA)
        viewModel.selectedMedicines.value = selectedMedicines

    }
}