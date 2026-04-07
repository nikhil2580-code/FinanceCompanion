package com.nikhilkhairnar.financecompanion.data.local

import androidx.room.TypeConverter
import com.nikhilkhairnar.financecompanion.data.model.TransactionType
import com.nikhilkhairnar.financecompanion.data.model.Category //Unresolved reference 'Category'.

class Converters {

    // TransactionType ↔ String
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)

    // Category ↔ String
    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category =
        Category.valueOf(value)
}