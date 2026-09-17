package com.example.workchop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_ID
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_IS_DELETE
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_JUDUL
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_KATEGORI
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_NOMINAL
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_TANGGAL
import com.example.workchop.AddTransactionActivity.Companion.EXTRA_TIPE
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var transactionDao: TransactionDao
    private lateinit var textSaldo: TextView
    private lateinit var textPemasukan: TextView
    private lateinit var textPengeluaran: TextView
    private lateinit var textCountTransaksi: TextView
    private lateinit var layoutEmptyState: View
    private lateinit var recyclerViewTransaksi: RecyclerView
    private lateinit var chipGroupFilter: ChipGroup
    private var sumberSaatIni: LiveData<List<Transaction>>? = null
    private var daftarTransaksiSaatIni: List<Transaction> = emptyList()

    // Adapter yang menjembatani list ke RecyclerView.
    private lateinit var adapter: TransactionAdapter

    // Launcher untuk membuka AddTransactionActivity & menerima hasilnya.
    private val addLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val id = data.getIntExtra(EXTRA_ID, 0)
            val isDelete = data.getBooleanExtra(EXTRA_IS_DELETE, false)

            if (isDelete) {
                lifecycleScope.launch {
                    val target = Transaction(id = id, title = "", amount = 0, type = "", category = "", date = 0)
                    transactionDao.delete(target)
                    Toast.makeText(this@MainActivity, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                return@registerForActivityResult
            }

            val judul = data.getStringExtra(EXTRA_JUDUL) ?: ""
            val nominal = data.getLongExtra(EXTRA_NOMINAL, 0)
            val tipe = data.getStringExtra(EXTRA_TIPE) ?: "Pengeluaran"
            val kategori = data.getStringExtra(EXTRA_KATEGORI) ?: "Lainnya"
            val tanggal = data.getLongExtra(EXTRA_TANGGAL, System.currentTimeMillis())

            val transaksi = Transaction(
                id = id,
                title = judul,
                amount = nominal,
                type = tipe,
                category = kategori,
                date = tanggal
            )

            lifecycleScope.launch {
                if (id == 0) {
                    transactionDao.insert(transaksi)
                    Toast.makeText(this@MainActivity, "Transaksi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                } else {
                    transactionDao.update(transaksi)
                    Toast.makeText(this@MainActivity, "Transaksi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transactionDao = AppDatabase.getInstance(this).transactionDao()

        recyclerViewTransaksi = findViewById(R.id.recyclerViewTransaksi)
        textSaldo = findViewById(R.id.textSaldo)
        textPemasukan = findViewById(R.id.textPemasukan)
        textPengeluaran = findViewById(R.id.textPengeluaran)
        textCountTransaksi = findViewById(R.id.textCountTransaksi)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        chipGroupFilter = findViewById(R.id.chipGroupFilter)

        adapter = TransactionAdapter(
            daftar = emptyList(),
            onClick = { formEdit(it) },
            onDeleteClick = { konfirmasiHapus(it) },
            onLongClick = { konfirmasiHapus(it) }
        )
        recyclerViewTransaksi.layoutManager = LinearLayoutManager(this)
        recyclerViewTransaksi.adapter = adapter

        setupSwipeToDelete()

        findViewById<ExtendedFloatingActionButton>(R.id.btnTambahTransaksi).setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addLauncher.launch(intent)
        }

        setupFilterChips()
        loadData(transactionDao.getAll())
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position in daftarTransaksiSaatIni.indices) {
                    val deletedItem = daftarTransaksiSaatIni[position]
                    lifecycleScope.launch {
                        transactionDao.delete(deletedItem)
                        Snackbar.make(recyclerViewTransaksi, "Transaksi \"${deletedItem.title}\" dihapus", Snackbar.LENGTH_LONG)
                            .setAction("BATAL") {
                                lifecycleScope.launch {
                                    transactionDao.insert(deletedItem)
                                }
                            }.show()
                    }
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerViewTransaksi)
    }

    private fun setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull() ?: R.id.chipSemua
            when (checkedId) {
                R.id.chipSemua -> {
                    loadData(transactionDao.getAll())
                }
                R.id.chipBulanIni -> {
                    loadData(transactionDao.getByRange(awalBulanIni(), akhirBulanIni()))
                }
                R.id.chipBulanLalu -> {
                    loadData(transactionDao.getByRange(awalBulanLalu(), akhirBulanLalu()))
                }
                R.id.chipTigaBulan -> {
                    loadData(transactionDao.getByRange(awalTigaBulanLalu(), System.currentTimeMillis()))
                }
                R.id.chipPilihBulan -> {
                    tampilkanPilihBulan()
                }
                R.id.chipRentang -> {
                    tampilkanPilihRentang()
                }
            }
        }
    }

    private fun tampilkanPilihRentang() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .setCalendarConstraints(batasTidakMasaDepan())
            .build()

        picker.addOnPositiveButtonClickListener { selection: Pair<Long, Long>? ->
            if (selection != null) {
                val startUtc = selection.first ?: return@addOnPositiveButtonClickListener
                val endUtc = selection.second ?: return@addOnPositiveButtonClickListener

                val calStart = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = startUtc
                }
                val calEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = endUtc
                }

                val lokalStart = Calendar.getInstance().apply {
                    set(calStart.get(Calendar.YEAR), calStart.get(Calendar.MONTH), calStart.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val lokalEnd = Calendar.getInstance().apply {
                    set(calEnd.get(Calendar.YEAR), calEnd.get(Calendar.MONTH), calEnd.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }

                loadData(transactionDao.getByRange(lokalStart.timeInMillis, lokalEnd.timeInMillis))
            }
        }
        picker.show(supportFragmentManager, "pilih_rentang")
    }

    private fun tampilkanPilihBulan() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal dalam Bulan")
            .setCalendarConstraints(batasTidakMasaDepan())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = selection
            }
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, utc.get(Calendar.YEAR))
                set(Calendar.MONTH, utc.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val awal = cal.timeInMillis

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val akhir = cal.timeInMillis

            loadData(transactionDao.getByRange(awal, akhir))
        }
        picker.show(supportFragmentManager, "pilih_bulan")
    }

    private fun loadData(sumber: LiveData<List<Transaction>>) {
        sumberSaatIni?.removeObservers(this)
        sumberSaatIni = sumber
        sumber.observe(this) { daftar ->
            daftarTransaksiSaatIni = daftar
            adapter.refresh(daftar)
            hitungSaldo(daftar)

            if (daftar.isEmpty()) {
                layoutEmptyState.visibility = View.VISIBLE
                recyclerViewTransaksi.visibility = View.GONE
                textCountTransaksi.text = "0 transaksi"
            } else {
                layoutEmptyState.visibility = View.GONE
                recyclerViewTransaksi.visibility = View.VISIBLE
                textCountTransaksi.text = "${daftar.size} transaksi"
            }
        }
    }

    private fun hitungSaldo(daftar: List<Transaction>) {
        val pemasukan = daftar.filter { it.type == "Pemasukan" }.sumOf { it.amount }
        val pengeluaran = daftar.filter { it.type == "Pengeluaran" }.sumOf { it.amount }
        textPemasukan.text = formatRupiah(pemasukan)
        textPengeluaran.text = formatRupiah(pengeluaran)
        textSaldo.text = formatRupiah(pemasukan - pengeluaran)
    }

    private fun formEdit(transaksi: Transaction) {
        val intent = Intent(this, AddTransactionActivity::class.java).apply {
            putExtra(EXTRA_ID, transaksi.id)
            putExtra(EXTRA_JUDUL, transaksi.title)
            putExtra(EXTRA_NOMINAL, transaksi.amount)
            putExtra(EXTRA_TIPE, transaksi.type)
            putExtra(EXTRA_KATEGORI, transaksi.category)
            putExtra(EXTRA_TANGGAL, transaksi.date)
        }
        addLauncher.launch(intent)
    }

    private fun konfirmasiHapus(transaksi: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus \"${transaksi.title}\"?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    transactionDao.delete(transaksi)
                    Toast.makeText(this@MainActivity, "Transaksi dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}