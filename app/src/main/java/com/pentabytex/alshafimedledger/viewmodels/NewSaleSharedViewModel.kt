package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.data.models.SaleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewSaleSharedViewModel @Inject constructor() : ViewModel() {
    val selectedMedicines = MutableLiveData<List<Medicine>>()
    val selectedCustomer = MutableLiveData<Customer>()
    val saleItemDetailsList = MutableLiveData<List<SaleItem>>()
}