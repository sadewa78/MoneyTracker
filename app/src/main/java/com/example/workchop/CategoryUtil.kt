package com.example.workchop

object CategoryUtil {
    fun getCategoryIcon(category: String): Int {
        return when (category.lowercase().trim()) {
            "makanan & minuman", "makanan", "minuman", "makan", "jajan" -> R.drawable.ic_cat_food
            "transportasi", "transport", "bensin", "ojek", "angkot" -> R.drawable.ic_cat_transport
            "belanja", "shopping", "belanjaan" -> R.drawable.ic_cat_shopping
            "gaji", "salary", "bonus", "pendapatan", "pemasukan" -> R.drawable.ic_cat_salary
            "tagihan & utilitas", "tagihan", "listrik", "air", "internet", "pulsa" -> R.drawable.ic_cat_bill
            "hiburan", "entertainment", "game", "nonton" -> R.drawable.ic_cat_entertainment
            else -> R.drawable.ic_cat_other
        }
    }
}
