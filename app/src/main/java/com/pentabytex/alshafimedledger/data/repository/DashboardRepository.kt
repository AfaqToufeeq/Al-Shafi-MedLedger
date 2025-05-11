package com.pentabytex.alshafimedledger.data.repository

import com.pentabytex.alshafimedledger.R
import com.pentabytex.alshafimedledger.data.models.QuickAccessButton
import com.pentabytex.alshafimedledger.enums.DashboardTitle
import javax.inject.Inject

class DashboardRepository @Inject constructor() {
    fun getQuickAccessButtons(): List<QuickAccessButton> {
        return listOf(
            QuickAccessButton(DashboardTitle.AddMedicines.title, R.drawable.ic_add_medicine),
            QuickAccessButton(DashboardTitle.ViewMedicines.title, R.drawable.ic_view_medicines),
            QuickAccessButton(DashboardTitle.CHAT_BOX.title, R.drawable.logo),
            QuickAccessButton(DashboardTitle.AI_CHATBOT.title, R.drawable.logo),
            QuickAccessButton(DashboardTitle.SUPPORT_GROUP.title, R.drawable.logo),
            QuickAccessButton(DashboardTitle.RESOURCES.title, R.drawable.logo)
        )
    }
}
