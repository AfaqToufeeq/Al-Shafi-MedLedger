package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentabytex.alshafimedledger.data.models.Medicine
import com.pentabytex.alshafimedledger.data.repository.MedicineRepository
import com.pentabytex.alshafimedledger.helpersutils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _saveMedicineState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val saveMedicineState: StateFlow<Resource<Unit>> = _saveMedicineState

    private val _medicines = MutableStateFlow<Resource<List<Medicine>>>(Resource.Idle)
    val medicines: StateFlow<Resource<List<Medicine>>> = _medicines

    private val _updateMedicineState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val updateMedicineState: StateFlow<Resource<Unit>> = _updateMedicineState

    private val _deleteMedicineState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteMedicineState: StateFlow<Resource<Unit>> = _deleteMedicineState

    init {
        observeMedicinesRealtime()
        fetchMedicines()
    }

    fun saveMedicine(medicine: Medicine) {
        _saveMedicineState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.addMedicine(medicine)
            _saveMedicineState.value = if (result.isSuccess) {
                Resource.Success(Unit)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }


    fun observeMedicinesRealtime() {
        viewModelScope.launch {
            repository.observeMedicines().collect { resource ->
                _medicines.value = resource
            }
        }
    }

    fun fetchMedicines() {
        _medicines.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.getMedicines()
            _medicines.value = result.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Error fetching medicines") }
            )
        }
    }

    fun updateMedicine(medicine: Medicine) {
        _updateMedicineState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.updateMedicine(medicine)
            _updateMedicineState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error updating medicine") }
            )
        }
    }

    fun deleteMedicine(id: String) {
        _deleteMedicineState.value = Resource.Loading
        viewModelScope.launch {
            val result = repository.deleteMedicine(id)
            _deleteMedicineState.value = result.fold(
                onSuccess = { Resource.Success(Unit) },
                onFailure = { Resource.Error(it.message ?: "Error deleting medicine") }
            )
        }
    }

    fun resetStates() {
        _saveMedicineState.value = Resource.Idle
        _updateMedicineState.value = Resource.Idle
        _deleteMedicineState.value = Resource.Idle
    }


}
