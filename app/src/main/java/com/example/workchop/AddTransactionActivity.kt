package com.example.workchop

import android.app.Activity
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class AddTransactionActivity : AppCompatActivity() {
    private var tanggalDipilih: Long = System.currentTimeMillis()
    private var idEdit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val editKeterangan = findViewById<EditText>(R.id.editKeterangan)
        val editNominal = findViewById<EditText>(R.id.editNominal)
        val toggleGroupTipe = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroupTipe)
        val spinnerKategori = findViewById<Spinner>(R.id.spinnerKategori)
        val cardPilihTanggal = findViewById<View>(R.id.cardPilihTanggal)
        val buttonSimpan = findViewById<Button>(R.id.buttonSimpan)
        val buttonHapus = findViewById<Button>(R.id.buttonHapus)
        val textTanggalDipilih = findViewById<TextView>(R.id.textTanggalDipilih)

        // Real-time Rupiah number formatter
        editNominal.addTextChangedListener(NumberTextWatcher(editNominal))

        textTanggalDipilih.text = formatTanggal(tanggalDipilih)

        cardPilihTanggal.setOnClickListener {
            pilihTanggalDanJam { millis ->
                tanggalDipilih = millis
                textTanggalDipilih.text = formatTanggal(millis)
            }
        }

        buttonSimpan.setOnClickListener {
            val keterangan = editKeterangan.text.toString().trim()
            val nominalText = editNominal.text.toString().trim()

            if (keterangan.isEmpty() || nominalText.isEmpty()) {
                Toast.makeText(this, "Keterangan & nominal wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nominal = parseNominal(nominalText)
            if (nominal <= 0L) {
                Toast.makeText(this, "Nominal tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tipe = if (toggleGroupTipe.checkedButtonId == R.id.btnTipePemasukan) {
                "Pemasukan"
            } else {
                "Pengeluaran"
            }
            val kategori = spinnerKategori.selectedItem?.toString() ?: "Lainnya"

            val hasil = Intent().apply {
                putExtra(EXTRA_ID, idEdit)
                putExtra(EXTRA_JUDUL, keterangan)
                putExtra(EXTRA_NOMINAL, nominal)
                putExtra(EXTRA_TIPE, tipe)
                putExtra(EXTRA_KATEGORI, kategori)
                putExtra(EXTRA_TANGGAL, tanggalDipilih)
                putExtra(EXTRA_IS_DELETE, false)
            }
            setResult(Activity.RESULT_OK, hasil)
            finish()
        }

        idEdit = intent.getIntExtra(EXTRA_ID, 0)
        if (idEdit == 0) {
            title = getString(R.string.judul_fore_tambah)
            buttonHapus.visibility = View.GONE
        } else {
            title = "Edit Transaksi"
            buttonHapus.visibility = View.VISIBLE
            editKeterangan.setText(intent.getStringExtra(EXTRA_JUDUL))

            val nominalValue = intent.getLongExtra(EXTRA_NOMINAL, 0)
            if (nominalValue > 0) {
                editNominal.setText(formatRibuan(nominalValue))
            }

            val tipe = intent.getStringExtra(EXTRA_TIPE)
            if (tipe == "Pemasukan") {
                toggleGroupTipe.check(R.id.btnTipePemasukan)
            } else {
                toggleGroupTipe.check(R.id.btnTipePengeluaran)
            }

            val kategori = intent.getStringExtra(EXTRA_KATEGORI)
            val daftarKategori = resources.getStringArray(R.array.daftar_kategori)
            val kategoriIndex = daftarKategori.indexOf(kategori)
            if (kategoriIndex >= 0) {
                spinnerKategori.setSelection(kategoriIndex)
            }

            tanggalDipilih = intent.getLongExtra(EXTRA_TANGGAL, System.currentTimeMillis())
            textTanggalDipilih.text = formatTanggal(tanggalDipilih)

            buttonHapus.setOnClickListener {
                konfirmasiHapus()
            }
        }
    }

    private fun konfirmasiHapus() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Hapus") { _, _ ->
                val hasil = Intent().apply {
                    putExtra(EXTRA_ID, idEdit)
                    putExtra(EXTRA_IS_DELETE, true)
                }
                setResult(Activity.RESULT_OK, hasil)
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun pilihTanggalDanJam(onSelesai: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(tanggalDipilih)
            .setCalendarConstraints(batasTidakMasaDepan())
            .build()

        datePicker.addOnPositiveButtonClickListener { pilihanTanggalUtc ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = pilihanTanggalUtc

            val awal = Calendar.getInstance().apply { timeInMillis = tanggalDipilih }
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(awal.get(Calendar.HOUR_OF_DAY))
                .setMinute(awal.get(Calendar.MINUTE))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val cal = Calendar.getInstance()
                cal.set(
                    utc.get(Calendar.YEAR),
                    utc.get(Calendar.MONTH),
                    utc.get(Calendar.DAY_OF_MONTH),
                    timePicker.hour,
                    timePicker.minute,
                    0
                )
                cal.set(Calendar.MILLISECOND, 0)

                val sekarang = System.currentTimeMillis()
                if (cal.timeInMillis > sekarang) {
                    Toast.makeText(this, "Waktu tidak boleh masa depan", Toast.LENGTH_SHORT).show()
                    onSelesai(sekarang)
                } else {
                    onSelesai(cal.timeInMillis)
                }
            }
            timePicker.show(supportFragmentManager, "pemilih_jam")
        }
        datePicker.show(supportFragmentManager, "pemilih_tanggal")
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_JUDUL = "extra_judul"
        const val EXTRA_NOMINAL = "extra_nominal"
        const val EXTRA_TIPE = "extra_tipe"
        const val EXTRA_KATEGORI = "extra_kategori"
        const val EXTRA_TANGGAL = "extra_tanggal"
        const val EXTRA_IS_DELETE = "extra_is_delete"
    }
}