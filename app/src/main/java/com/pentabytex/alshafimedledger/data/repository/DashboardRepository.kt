package com.pentabytex.alshafimedledger.data.repository

import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.QuickAccessButton
import com.pentabytex.alshafimedledger.enums.DashboardTitle
import javax.inject.Inject

class DashboardRepository @Inject constructor() {
    fun getQuickAccessButtons(): List<QuickAccessButton> {
        return listOf(
            QuickAccessButton(DashboardTitle.AddMedicines.title, R.drawable.ic_add_medicines),
            QuickAccessButton(DashboardTitle.ViewMedicines.title, R.drawable.ic_view_medicine),
            QuickAccessButton(DashboardTitle.AddCustomers.title, R.drawable.ic_add_customer),
            QuickAccessButton(DashboardTitle.ViewCustomers.title, R.drawable.ic_customers)
        )
    }
}
