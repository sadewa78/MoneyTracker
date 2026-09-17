package com.example.workchop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workchop.Transaction

/**
 * TransactionAdapter = penghubung antara DATA (list transaksi) dan
 * TAMPILAN (RecyclerView). Tugasnya: mengubah setiap objek Transaction
 * menjadi satu baris tampilan (item_transaction.xml).
 *
 * RecyclerView memanggil 3 fungsi penting:
 * - onCreateViewHolder : membuat 1 baris kosong dari XML
 * - onBindViewHolder   : mengisi baris dengan data transaksi
 * - getItemCount       : jumlah baris = jumlah transaksi
 **/
class TransactionAdapter(
    private var daftar: List<Transaction>,
    private val onClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit,
    private val onLongClick: ((Transaction) -> Unit)? = null
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    /**
     * ViewHolder = "wadah" yang menyimpan referensi view di satu baris,
     * supaya tidak perlu findViewById berulang-ulang (lebih cepat).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textJudul: TextView = view.findViewById(R.id.textJudul)
        val textTanggal: TextView = view.findViewById(R.id.textTanggal)
        val textNominal: TextView = view.findViewById(R.id.textNominal)
        val imageKategori: android.widget.ImageView = view.findViewById(R.id.imageKategori)
        val btnDelete: android.widget.ImageButton = view.findViewById(R.id.btnDelete)
    }

    // Membuat baris baru dari layout item_transaction.xml.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    // Mengisi baris ke-"position" dengan data transaksi yang sesuai.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = daftar[position]

        holder.textJudul.text = transaksi.title
        holder.textTanggal.text = formatTanggal(transaksi.date)

        // Set icon kategori yang sesuai
        val iconRes = CategoryUtil.getCategoryIcon(transaksi.category)
        holder.imageKategori.setImageResource(iconRes)

        val isIncome = transaksi.type == "Pemasukan"
        val tanda = if (isIncome) "+ " else "- "
        holder.textNominal.text = tanda + formatRupiah(transaksi.amount)

        if (isIncome) {
            holder.textNominal.setTextColor(holder.itemView.context.getColor(R.color.income_green_dark))
            holder.textNominal.setBackgroundResource(R.drawable.bg_pill_income)
        } else {
            holder.textNominal.setTextColor(holder.itemView.context.getColor(R.color.expense_red_dark))
            holder.textNominal.setBackgroundResource(R.drawable.bg_pill_expense)
        }

        holder.itemView.setOnClickListener {
            onClick(transaksi)
        }
        holder.btnDelete.setOnClickListener {
            onDeleteClick(transaksi)
        }
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(transaksi)
            true
        }
    }

    // Jumlah item = jumlah transaksi dalam list.
    override fun getItemCount(): Int = daftar.size

    fun refresh(dataBaru: List<Transaction>) {
        daftar = dataBaru
        notifyDataSetChanged()
    }
}