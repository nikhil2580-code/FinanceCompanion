package com.nikhilkhairnar.financecompanion.utils

import android.icu.text.NumberFormat
import java.util.Locale

object CurrencyUtils {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun format(amount: Double): String =
        formatter.format(amount)

    fun formatWithSign(amount: Double, isIncome: Boolean): String {
        val prefix = if (isIncome) "+" else "-"
        return "$prefix${format(Math.abs(amount))}"
    }
}
