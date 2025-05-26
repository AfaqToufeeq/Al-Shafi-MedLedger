package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentabytex.alshafimedledger.data.models.Sale
import com.pentabytex.alshafimedledger.data.models.SaleItem
import com.pentabytex.alshafimedledger.data.repository.SaleRepository
import com.pentabytex.alshafimedledger.enums.PaymentStatus
import com.pentabytex.alshafimedledger.enums.SaleSortType
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

    private val _updateSaleState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updateSaleState: StateFlow<Resource<Unit>> = _updateSaleState

    // Sorting & Filtering
    var currentSortType: SaleSortType = SaleSortType.DATE_DESC
    var currentPaymentStatus: PaymentStatus = PaymentStatus.ALL
    var currentSearchQuery: String = ""
    var currentDateRange: Pair<Long, Long>? = null

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

    fun updateSale(sale: Sale) {
        _updateSaleState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.updateSale(sale)
            _updateSaleState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error updating sale") }
            )
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

    fun searchSales2(query: String) {
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

    fun searchSales(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun sortSalesBy(type: SaleSortType) {
        currentSortType = type
        applyFilters()
    }

    fun filterByPaymentStatus(status: PaymentStatus) {
        currentPaymentStatus = status
        applyFilters()
    }

    fun filterByDateRange(start: Long, end: Long) {
        currentDateRange = start to end
        applyFilters()
    }

    fun clearAllFilters() {
        currentSortType = SaleSortType.DATE_DESC
        currentPaymentStatus = PaymentStatus.ALL
        currentSearchQuery = ""
        currentDateRange = null
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch(dispatcherProvider.default) {
            val resource = _sales.value
            if (resource is Resource.Success) {
                var filteredList = resource.data

                // Filter by search query
                if (currentSearchQuery.isNotBlank()) {
                    val query = currentSearchQuery.lowercase()
                    filteredList = filteredList.filter {
                        it.saleId.contains(query, ignoreCase = true) ||
                                it.customerName.contains(query, ignoreCase = true) ||
                                it.notes.contains(query, ignoreCase = true) ||
                                it.totalPrice.toString().contains(query)
                    }
                }

                // Filter by payment status
                filteredList = when (currentPaymentStatus) {
                    PaymentStatus.RECEIVED ->
                        filteredList.filter { it.paymentStatus == PaymentStatus.RECEIVED.displayName }
                    PaymentStatus.NOT_RECEIVED ->
                        filteredList.filter { it.paymentStatus == PaymentStatus.NOT_RECEIVED.displayName }
                    PaymentStatus.PENDING ->
                        filteredList.filter { it.paymentStatus == PaymentStatus.PENDING.displayName }
                    else -> filteredList
                }

                // Sort
                filteredList = when (currentSortType) {
                    SaleSortType.DATE_ASC -> filteredList.sortedBy { it.timestamp }
                    SaleSortType.DATE_DESC -> filteredList.sortedByDescending { it.timestamp }
                    SaleSortType.PRICE_ASC -> filteredList.sortedBy { it.totalPrice }
                    SaleSortType.PRICE_DESC -> filteredList.sortedByDescending { it.totalPrice }
                }

                // Filter by date range if set
                currentDateRange?.let { (start, end) ->
                    filteredList = filteredList.filter { it.timestamp in start..end }
                }

                _filterSales.emit(Resource.Success(filteredList))
            } else {
                _filterSales.emit(resource)
            }
        }
    }
}
