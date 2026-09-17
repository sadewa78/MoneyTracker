package com.example.workchop

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * formatTanggal = mengubah timestamp (Long milidetik) menjadi teks
 * tanggal & jam yang sudah dibaca.
 * Contoh: 1757142600000 -> "06 Sept. 2026, 14:30"
 */
fun formatTanggal(millis: Long): String {
    val pola = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
    return pola.format(Date(millis))
}

/**
 * batasTidakMasaDepan = aturan untuk MaterialDatePicker agar tanggal
 * di MASA DEPAN tidak bisa dipilih (hanya hari ini & sebelumnya).
 * Dipakai di form input (hari kedua) dan filter (hari keempat).
 */
fun batasTidakMasaDepan(): CalendarConstraints = CalendarConstraints.Builder()
    .setValidator(DateValidatorPointBackward.now())
    .build()

fun awalBulanIni(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun akhirBulanIni(): Long {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

fun awalBulanLalu(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun akhirBulanLalu(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.MONTH, -1)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

fun awalTigaBulanLalu(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.MONTH, -2)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}