package com.example.workchop

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
    groupingSeparator = '.'
}
private val rupiahFormatter = DecimalFormat("#,###", symbols)

fun formatRupiah(amount: Long): String {
    if (amount == 0L) return "Rp 0"
    return "Rp " + rupiahFormatter.format(amount)
}

fun formatRibuan(amount: Long): String {
    if (amount == 0L) return ""
    return rupiahFormatter.format(amount)
}

fun parseNominal(text: String): Long {
    val cleanString = text.replace("[^\\d]".toRegex(), "")
    return cleanString.toLongOrNull() ?: 0L
}

class NumberTextWatcher(private val editText: EditText) : TextWatcher {
    private var current = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        val str = s.toString()
        if (str != current) {
            editText.removeTextChangedListener(this)

            val cleanString = str.replace("[^\\d]".toRegex(), "")
            if (cleanString.isNotEmpty()) {
                val parsed = cleanString.toLongOrNull() ?: 0L
                val formatted = rupiahFormatter.format(parsed)
                current = formatted
                editText.setText(formatted)
                editText.setSelection(formatted.length)
            } else {
                current = ""
                editText.setText("")
            }

            editText.addTextChangedListener(this)
        }
    }
}