# Modernisasi UI & UX Pocket Tracker

Rencana ini bertujuan untuk mengubah tampilan aplikasi menjadi lebih modern, bersih, dan profesional dengan mengikuti prinsip desain terkini (Material 3 look & feel) dan palet warna yang lebih segar.

## User Review Required

> [!IMPORTANT]
> Saya akan memperbarui palet warna utama dari Indigo tua menjadi Indigo yang lebih cerah dan menggunakan Slate untuk warna teks agar lebih kontras dan mudah dibaca. Apakah Anda memiliki preferensi warna khusus? Jika tidak, saya akan melanjutkan dengan desain modern standar.

## Proposed Changes

### 1. Refactor Warna & Tema
Memperbarui `colors.xml` dan `themes.xml` untuk mendukung palet warna baru yang lebih kohesif.

#### [MODIFY] [colors.xml](file:///C:/Users/Lenovo/Documents/2026/Workshop App/workchop/app/src/main/res/values/colors.xml)
- Memperbarui warna primer, background, dan status transaksi (income/expense).
- Menambahkan variasi warna untuk shadow dan border yang lebih halus.

### 2. Peningkatan Halaman Utama (Main Screen)
Merapikan layout di `activity_main.xml`.

#### [MODIFY] [activity_main.xml](file:///C:/Users/Lenovo/Documents/2026/Workshop App/workchop/app/src/main/res/layout/activity_main.xml)
- **Balance Card**: Menambahkan gradient yang lebih halus dan memperbaiki tipografi angka saldo.
- **Filter Chips**: Memberikan padding dan radius yang lebih estetik.
- **Header List**: Memperbaiki jarak (spacing) antara kartu saldo dan riwayat transaksi.

### 3. Peningkatan Item Transaksi (List Item)
Mendesain ulang `item_transaction.xml` agar terlihat lebih rapi.

#### [MODIFY] [item_transaction.xml](file:///C:/Users/Lenovo/Documents/2026/Workshop App/workchop/app/src/main/res/layout/item_transaction.xml)
- Menghilangkan border kartu yang terlalu kaku, menggantinya dengan shadow halus.
- Merapikan ikon kategori dengan background lingkaran yang lebih lembut warnanya.
- Mengatur ulang posisi tombol hapus agar tidak mengganggu keterbacaan nominal.

### 4. Peningkatan Halaman Tambah Transaksi
Merapikan form input di `activity_add_transaction.xml`.

#### [MODIFY] [activity_add_transaction.xml](file:///C:/Users/Lenovo/Documents/2026/Workshop App/workchop/app/src/main/res/layout/activity_add_transaction.xml)
- Memperbaiki style `TextInputLayout` agar lebih minimalis.
- Memperbaiki tampilan Toggle Button untuk Pemasukan/Pengeluaran.
- Menstandarisasi margin dan padding antar elemen input.

## Verification Plan

### Automated Tests
- Menjalankan aplikasi untuk memastikan tidak ada layout yang pecah setelah perubahan atribut XML.
- Verifikasi warna pada mode terang.

### Manual Verification
- Cek keterbacaan teks pada kartu saldo.
- Pastikan tombol "Hapus" pada item tetap mudah ditekan namun tidak merusak visual.
- Pastikan transisi antar halaman tetap mulus.
