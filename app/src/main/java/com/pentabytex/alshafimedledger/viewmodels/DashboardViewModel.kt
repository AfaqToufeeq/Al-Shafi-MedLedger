package com.pentabytex.alshafimedledger.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pentabytex.alshafimedledger.data.models.QuickAccessButton
import com.pentabytex.alshafimedledger.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _quickAccessButtons = MutableLiveData<List<QuickAccessButton>>()
    val quickAccessButtons: LiveData<List<QuickAccessButton>> get() = _quickAccessButtons

    init {
        fetchQuickAccessButtons()
    }

    private fun fetchQuickAccessButtons() {
        _quickAccessButtons.value = repository.getQuickAccessButtons()
    }

}
