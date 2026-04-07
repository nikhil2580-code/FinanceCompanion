package com.nikhilkhairnar.financecompanion.data.model

// Change 'class' to 'enum class'
enum class Category(val displayName: String, val emoji: String) {
    FOOD("Food", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🍿"),
    HEALTH("Health", "🏥"),
    EDUCATION("Education", "📚"),
    BILLS("Bills", "💡"),
    SALARY("Salary", "💼"),
    FREELANCE("Freelance", "💻"),
    INVESTMENT("Investment", "📈"),
    OTHER("Other", "📦"); // Use a semicolon here if you plan to add functions/properties below

    // Optional: You can add helper functions here if needed
}