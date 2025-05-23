package com.pentabytex.alshafimedledger.utils

object RsFormatHelper {

    /** 
     * Format a Double as Rs currency with commas and specified decimals.
     * Example: formatPrice(12345.678, 2) -> "Rs 12,345.68"
     */
    fun formatPrice(amount: Double, decimals: Int = 2): String {
        val formatString = "%,.${decimals}f"
        return "Rs ${formatString.format(amount)}"
    }

    /**
     * Format percent with 1 decimal place and a label.
     * Example: formatPercent(12.345, "Profit") -> "12.3% Profit"
     */
    fun formatPercent(percent: Double, label: String): String {
        return "%.1f%% $label".format(kotlin.math.abs(percent))
    }
}
