package com.civicpulse.app

// Import untuk membuat layout kartu dan mengakses komponen tampilannya.
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

// Adapter dasar yang digunakan bersama oleh adapter event, bantuan, dan lokasi.
//
// <T> adalah tipe data generik.
// Artinya, adapter ini dapat digunakan untuk berbagai jenis data.
//
// abstract berarti kelas ini menjadi dasar bagi kelas turunan.
// protected membuat context dapat diakses oleh kelas turunannya.
abstract class CivicAdapter<T>(
    protected val context: Context
) : BaseAdapter() {

    // Daftar data yang sedang ditampilkan.
    // Awalnya kosong sampai submit() dipanggil.
    private var rows: List<T> = emptyList()

    // Mengganti data adapter dengan daftar terbaru.
    fun submit(items: List<T>) {
        rows = items

        // Memberi tahu tampilan yang memakai adapter bahwa datanya berubah.
        notifyDataSetChanged()
    }

    // Mengembalikan jumlah item dalam daftar.
    override fun getCount() = rows.size

    // Mengambil data berdasarkan posisi baris.
    override fun getItem(position: Int): T = rows[position]

    // Menggunakan posisi sebagai ID baris bertipe Long.
    // ID ini mengikuti posisi, bukan ID permanen dari data.
    override fun getItemId(position: Int) = position.toLong()

    // ViewHolder menyimpan referensi komponen dalam satu kartu.
    // Dengan demikian, findViewById tidak perlu diulang setiap kali
    // kartu yang sama digunakan untuk menampilkan data lain.
    class Holder(view: View) {
        val image: ImageView = view.findViewById(R.id.card_image)
        val title: TextView = view.findViewById(R.id.card_title)
        val meta: TextView = view.findViewById(R.id.card_meta)
        val body: TextView = view.findViewById(R.id.card_body)
        val tag: TextView = view.findViewById(R.id.card_tag)
        val action: Button = view.findViewById(R.id.card_action)
    }

    // Membuat atau menggunakan ulang tampilan untuk sebuah baris.
    //
    // position: posisi data yang akan ditampilkan.
    // convertView: tampilan lama yang dapat digunakan kembali.
    // parent: wadah daftar yang meminta tampilan baris.
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        // Jika convertView tersedia, gunakan kembali kartu tersebut.
        // Jika null, buat kartu baru dari layout row_card.xml.
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.row_card, parent, false)
            .also {
                // Menyimpan Holder pada properti tag milik View kartu.
                // Ini berbeda dari TextView bernama tag di dalam Holder.
                it.tag = Holder(it)
            }

        // Mengambil Holder yang tersimpan, lalu mengisi kartu dengan data terbaru.
        // "as Holder" mengubah referensi tag menjadi tipe Holder.
        bind(view.tag as Holder, getItem(position))

        // Mengembalikan kartu yang sudah diisi.
        return view
    }

    // Setiap kelas turunan wajib menentukan cara mengisi kartu.
    // Pengisian ulang juga memperbarui listener agar mengikuti item terbaru.
    protected abstract fun bind(holder: Holder, item: T)
}

// Adapter khusus untuk data kegiatan komunitas.
class EventAdapter(
    context: Context,

    // Digunakan untuk memeriksa status pendaftaran event.
    private val store: CivicStore,

    // Callback yang dijalankan saat tombol event ditekan.
    // Adapter menyerahkan data event kepada pemanggil callback.
    private val onRegister: (CivicEvent) -> Unit
) : CivicAdapter<CivicEvent>(context) {

    // with(holder) memungkinkan akses image, title, dan komponen lainnya
    // tanpa menulis holder.image, holder.title, dan seterusnya.
    override fun bind(holder: Holder, item: CivicEvent) = with(holder) {

        // Kartu event menampilkan gambar.
        image.visibility = View.VISIBLE

        // Event berkode garden memakai ilustrasi berkebun.
        // Event lainnya memakai ilustrasi komunitas.
        image.setImageResource(
            if (item.id == "garden") {
                R.drawable.event_garden
            } else {
                R.drawable.community_hero
            }
        )

        // Menampilkan nama kegiatan.
        title.text = item.title

        // Menampilkan tanggal dan lokasi pada dua baris.
        // \n digunakan untuk membuat baris baru.
        meta.text = "◷ ${item.date}\n⌖ ${item.location}"

        // Menampilkan penjelasan kegiatan.
        body.text = item.description

        // Menampilkan kategori dan harga kegiatan.
        tag.text = "${item.category} • ${item.price}"

        // Label tombol ditentukan berdasarkan urutan kondisi berikut.
        action.text = when {
            // Event selesai selalu menampilkan tombol Lihat hasil.
            item.phase == "Selesai" -> "Lihat hasil"

            // Event yang sudah didaftarkan memiliki penanda Terdaftar.
            store.registered(item.id) -> "Terdaftar ✓"

            // Event lain dapat didaftarkan.
            else -> "Daftar"
        }

        // Mengirim event yang dipilih ke callback.
        // Dialog dan perubahan status ditangani oleh pemanggilnya.
        action.setOnClickListener {
            onRegister(item)
        }
    }
}

// Adapter khusus untuk postingan permintaan dan penawaran bantuan.
class HelpAdapter(
    context: Context,

    // Digunakan untuk memeriksa status minat bantuan.
    private val store: CivicStore,

    // Callback ketika pengguna menekan tombol pada kartu bantuan.
    private val onHelp: (HelpPost) -> Unit
) : CivicAdapter<HelpPost>(context) {

    override fun bind(holder: Holder, item: HelpPost) = with(holder) {

        // Kartu bantuan tidak menggunakan gambar besar.
        // GONE menyembunyikan gambar sekaligus menghilangkan ruangnya.
        image.visibility = View.GONE

        // take(1) mengambil satu karakter pertama dari nama pengguna.
        // Karakter tersebut ditampilkan sebagai inisial sebelum judul.
        title.text = "${item.user.take(1)}  ${item.title}"

        // Menampilkan nama pembuat postingan dan keterangan waktunya.
        meta.text = "${item.user} • ${item.time}"

        // Menampilkan kebutuhan atau bantuan yang ditawarkan.
        body.text = item.description

        // Menampilkan jenis postingan: Membutuhkan atau Menawarkan.
        tag.text = item.kind

        // Menentukan label tombol sesuai status dan jenis postingan.
        action.text = if (store.helped(item.id)) {
            // Pengguna sudah menyimpan minat pada postingan ini.
            "Tersambung ✓"
        } else if (item.kind == "Menawarkan") {
            // Pengguna dapat meminta bantuan yang ditawarkan.
            "Minta Bantuan"
        } else {
            // Pengguna dapat menawarkan bantuan kepada pembuat permintaan.
            "Bantu Warga"
        }

        // Mengirim postingan yang dipilih kepada pemanggil callback.
        action.setOnClickListener {
            onHelp(item)
        }
    }
}

// Adapter khusus untuk fasilitas atau tempat di halaman Jelajah Lokal.
class PlaceAdapter(
    context: Context,

    // Callback ketika tombol Rute ditekan.
    private val onRoute: (LocalPlace) -> Unit
) : CivicAdapter<LocalPlace>(context) {

    override fun bind(holder: Holder, item: LocalPlace) = with(holder) {

        // Kartu lokasi tidak menampilkan gambar besar.
        image.visibility = View.GONE

        // Menampilkan nama tempat dengan simbol lokasi.
        title.text = "⌖ ${item.name}"

        // Menampilkan jarak dan jam operasional.
        meta.text = "${item.distance} • ${item.hours}"

        // Menampilkan alamat tempat.
        body.text = item.address

        // Menampilkan kategori, misalnya Kesehatan atau Fasilitas Umum.
        tag.text = item.category

        // Memberikan label pada tombol tindakan.
        action.text = "Rute"

        // Mengirim lokasi yang dipilih kepada pemanggil callback.
        // Adapter sendiri tidak menghitung atau membuka rute GPS.
        action.setOnClickListener {
            onRoute(item)
        }
    }
}