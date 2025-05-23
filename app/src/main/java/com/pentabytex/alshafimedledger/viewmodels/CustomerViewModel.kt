package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentabytex.alshafimedledger.data.models.Customer
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.data.repository.CustomerRepository
import com.pentabytex.alshafimedledger.helpersutils.Resource
import com.pentabytex.alshafimedledger.utils.CoroutineDispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val dispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    private val _saveCustomerState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val saveCustomerState: StateFlow<Resource<Unit>> = _saveCustomerState

    private val _customers = MutableStateFlow<Resource<List<Customer>>>(Resource.Idle)
    val customers: StateFlow<Resource<List<Customer>>> = _customers

    private val _filterCustomers = MutableStateFlow<Resource<List<Customer>>>(Resource.Idle)
    val filterCustomers: StateFlow<Resource<List<Customer>>> = _filterCustomers

    private val _customer = MutableStateFlow<Resource<Customer>>(Resource.Idle)
    val customer: StateFlow<Resource<Customer>> = _customer

    private val _updateCustomerState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updateCustomerState: StateFlow<Resource<Unit>> = _updateCustomerState

    private val _deleteCustomerState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteCustomerState: StateFlow<Resource<Unit>> = _deleteCustomerState

    init {
        observeCustomersRealtime()
        fetchCustomers()
    }

    fun saveCustomer(customer: Customer) {
        _saveCustomerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.addCustomer(customer)
            _saveCustomerState.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun observeCustomersRealtime() {
        viewModelScope.launch {
            repository.observeCustomers().collect { resource ->
                _customers.value = resource
                _filterCustomers.value = resource
            }
        }
    }

    fun fetchCustomers() {
        _customers.value = Resource.Loading
        _filterCustomers.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getCustomers()
            _customers.value = result.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error fetching customers") }
            )
            _filterCustomers.value = _customers.value
        }
    }

    fun searchCustomers(query: String) {
        viewModelScope.launch(dispatcherProvider.default) {
            val resource = _customers.value
            if (resource is Resource.Success) {
                val list = resource.data
                val filteredList = if (query.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.phone.contains(query, ignoreCase = true) ||
                        it.address.contains(query, ignoreCase = true)
                    }
                }
                _filterCustomers.emit(Resource.Success(filteredList))
            } else {
                _filterCustomers.emit(resource)
            }
        }
    }

    fun getCustomerById(id: String) {
        _updateCustomerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getCustomerById(id)
            _customer.value = result.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error updating customer") }
            )
        }
    }

    fun updateCustomer(customer: Customer) {
        _updateCustomerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.updateCustomer(customer)
            _updateCustomerState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error updating customer") }
            )
        }
    }

    fun deleteCustomer(id: String) {
        _deleteCustomerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.deleteCustomer(id)
            _deleteCustomerState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error deleting customer") }
            )
        }
    }

    fun deleteCustomersBulk(customers: List<Customer>) {
        _deleteCustomerState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.deleteCustomersBulk(customers)
            _deleteCustomerState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Bulk delete failed") }
            )
        }
    }

    fun resetStates() {
        _saveCustomerState.value = Resource.Idle
        _updateCustomerState.value = Resource.Idle
        _deleteCustomerState.value = Resource.Idle
    }
}
