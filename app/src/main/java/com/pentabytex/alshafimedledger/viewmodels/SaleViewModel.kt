package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.data.repository.SaleRepository
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(
    private val repository: SaleRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    private val _saveSaleState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val saveSaleState: StateFlow<Resource<Unit>> = _saveSaleState

    private val _sale = MutableStateFlow<Resource<Sale>>(Resource.Idle)
    val sale: StateFlow<Resource<Sale>> = _sale

    private val _sales = MutableStateFlow<Resource<List<Sale>>>(Resource.Idle)
    val sales: StateFlow<Resource<List<Sale>>> = _sales

    private val _filterSales = MutableStateFlow<Resource<List<Sale>>>(Resource.Idle)
    val filterSales: StateFlow<Resource<List<Sale>>> = _filterSales

    private val _deleteSaleState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteSaleState: StateFlow<Resource<Unit>> = _deleteSaleState

    init {
        observeSalesRealtime()
    }

    fun saveSale(sale: Sale) {
        _saveSaleState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.addSale(sale)
            _saveSaleState.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    private fun observeSalesRealtime() {
        viewModelScope.launch {
            repository.observeSales().collect { resource ->
                _sales.value = resource
                _filterSales.value = resource
            }
        }
    }

    fun getSaleById(id: String) {
        viewModelScope.launch(dispatcherProvider.io) {
           val sale =  repository.getSaleById(id)
            if (sale.isSuccess) {
                _sale.value = Resource.Success(sale.getOrNull()!!)
            } else {
                _sale.value = Resource.Error(sale.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun searchSales(query: String) {
        viewModelScope.launch(dispatcherProvider.default) {
            val resource = _sales.value
            if (resource is Resource.Success) {
                val list = resource.data
                val filteredList = if (query.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.saleId.contains(query, ignoreCase = true) ||
                        it.customerName.contains(query, ignoreCase = true) ||
                                it.totalPrice.toString().contains(query, ignoreCase = true) ||
                                it.notes.contains(query, ignoreCase = true)
                    }
                }
                _filterSales.emit(Resource.Success(filteredList))
            } else {
                _filterSales.emit(resource)
            }
        }
    }

    fun deleteSale(id: String) {
        _deleteSaleState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.deleteSale(id)
            _deleteSaleState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error deleting customer") }
            )
        }
    }

    fun deleteSalesBulk(sale: List<Sale>) {
        _deleteSaleState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.deleteSalesBulk(sale)
            _deleteSaleState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error deleting customer") }
            )
        }
    }
}
