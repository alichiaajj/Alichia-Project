package com.civicpulse.app

// SharedPreferences digunakan untuk menyimpan data sederhana di perangkat.
import android.content.SharedPreferences

// JSONArray menyimpan kumpulan objek JSON.
// JSONObject menyimpan pasangan nama properti dan nilainya.
import org.json.JSONArray
import org.json.JSONObject

// Model data kegiatan komunitas.
// data class otomatis menyediakan fungsi seperti copy(), equals(), dan toString().
data class CivicEvent(
    val id: String,          // Kode pengenal event.
    val title: String,       // Nama kegiatan.
    val date: String,        // Tanggal dan waktu dalam bentuk teks.
    val location: String,   // Nama lokasi kegiatan.
    val category: String,   // Kategori, misalnya Lingkungan atau Edukasi.
    val price: String,      // Harga dalam bentuk teks, misalnya Gratis.
    val description: String,// Penjelasan kegiatan.

    // Nilai awal digunakan jika phase tidak diisi saat membuat objek.
    // Status ini ditentukan dari data, bukan dihitung otomatis dari tanggal.
    val phase: String = "Terdekat"
)

// Model data permintaan atau penawaran bantuan.
data class HelpPost(
    val id: String,          // Kode pengenal postingan.
    val user: String,        // Nama pembuat postingan.
    val title: String,       // Judul bantuan.
    val description: String,// Penjelasan kebutuhan atau penawaran.
    val kind: String,        // Jenis: Membutuhkan atau Menawarkan.
    val time: String         // Keterangan waktu dalam bentuk teks.
)

// Model data fasilitas atau tempat di lingkungan sekitar.
data class LocalPlace(
    val name: String,        // Nama tempat.
    val category: String,    // Kategori fasilitas.
    val distance: String,    // Jarak contoh dalam bentuk teks.
    val hours: String,       // Jam operasional.
    val address: String,     // Alamat tempat.

    // Posisi relatif marker pada peta ilustratif.
    // Contoh x = 0.23f berarti posisi horizontal 23% dari lebar peta.
    // Nilai ini bukan koordinat lintang dan bujur GPS.
    val x: Float,
    val y: Float
)

// object membuat satu objek bersama yang dapat diakses melalui SampleData.
// Data contoh ini ditulis di kode dan tidak diambil dari server.
object SampleData {

    // Daftar event contoh untuk Beranda dan halaman Event.
    val events = listOf(
        // phase tidak diisi sehingga menggunakan nilai awal "Terdekat".
        CivicEvent(
            "clean",
            "Aksi Bersih Lingkungan",
            "Sab, 31 Okt 2026 • 07.00–10.00",
            "Taman Kota Harapan",
            "Lingkungan",
            "Gratis",
            "Bersama warga RW 04 membersihkan taman dan memilah sampah. Bawa botol minum serta sarung tangan."
        ),

        CivicEvent(
            "garden",
            "Workshop Urban Farming",
            "Min, 1 Nov 2026 • 09.00–12.00",
            "Balai RW 04",
            "Edukasi",
            "Rp25.000",
            "Belajar menanam sayuran di lahan sempit. Biaya contoh mencakup bibit dan media tanam; tidak ada pembayaran di aplikasi."
        ),

        // Status ditentukan secara langsung menjadi "Mendatang".
        CivicEvent(
            "market",
            "Pasar UMKM Warga",
            "Sab, 7 Nov 2026 • 08.00–15.00",
            "Lapangan RW 04",
            "Ekonomi",
            "Gratis",
            "Kenali produk dan kuliner buatan tetangga. Terbuka untuk seluruh warga.",
            "Mendatang"
        ),

        // Event ini masuk ke daftar kegiatan yang sudah selesai.
        CivicEvent(
            "trees",
            "Menanam untuk Esok",
            "Min, 18 Okt 2026 • 07.00–10.00",
            "Kebun Komunitas",
            "Lingkungan",
            "Gratis",
            "Kegiatan penanaman pohon telah selesai. Terima kasih kepada seluruh relawan.",
            "Selesai"
        )
    )

    // Daftar fasilitas contoh untuk halaman Jelajah Lokal.
    // Akhiran f pada angka menunjukkan tipe Float.
    val places = listOf(
        LocalPlace(
            "Puskesmas Kecamatan",
            "Kesehatan",
            "1,2 km",
            "08.00–16.00",
            "Jl. Sehat No. 8",
            .23f,
            .30f
        ),

        LocalPlace(
            "Toko Sembako Makmur",
            "Toko & Kuliner",
            "0,6 km",
            "07.00–21.00",
            "Jl. Melati No. 12",
            .65f,
            .40f
        ),

        LocalPlace(
            "Taman Kota Harapan",
            "Fasilitas Umum",
            "0,8 km",
            "06.00–22.00",
            "Jl. Harapan Raya",
            .43f,
            .75f
        ),

        LocalPlace(
            "Balai RW 04",
            "Fasilitas Umum",
            "0,3 km",
            "08.00–17.00",
            "Jl. Melati No. 4",
            .78f,
            .72f
        )
    )

    // Daftar postingan bantuan contoh.
    // Postingan baru dari pengguna disimpan terpisah melalui CivicStore.
    val help = listOf(
        HelpPost(
            "h1",
            "Budi Santoso",
            "Butuh bantuan angkat barang",
            "Pindahan rumah di Jl. Melati No. 12. Membutuhkan dua orang untuk memindahkan lemari pada Sabtu pagi.",
            "Membutuhkan",
            "2 jam lalu"
        ),

        HelpPost(
            "h2",
            "Siti Nurhaliza",
            "Mencari tutor matematika",
            "Mencari pendamping belajar matematika untuk anak SD kelas 4, dua kali seminggu di lingkungan RW 04.",
            "Membutuhkan",
            "4 jam lalu"
        ),

        HelpPost(
            "h3",
            "Andi Pratama",
            "Bantu jaga rumah sementara",
            "Saya bisa membantu menyiram tanaman dan mengecek rumah tetangga selama bepergian, maksimal tiga hari.",
            "Menawarkan",
            "5 jam lalu"
        )
    )
}

// Kelas pembungkus SharedPreferences agar akses data lokal lebih sederhana.
// Objek prefs diberikan dari MainActivity saat CivicStore dibuat.
class CivicStore(private val prefs: SharedPreferences) {

    // Properti nama pengguna dengan getter dan setter khusus.
    var name: String

        // Membaca nama tersimpan.
        // Jika belum ada atau hasilnya null, gunakan nama "Chia".
        get() = prefs.getString("name", "Chia") ?: "Chia"

        // Dipanggil ketika kode menjalankan store.name = namaBaru.
        set(value) {
            prefs.edit()
                .putString("name", value)
                .apply()
        }

    // Properti status keanggotaan komunitas.
    var joined: Boolean

        // Nilai awal false berarti belum bergabung.
        get() = prefs.getBoolean("joined", false)

        // Dipanggil ketika status keanggotaan diubah.
        set(value) {
            prefs.edit()
                .putBoolean("joined", value)
                .apply()
        }

    // Memeriksa apakah sebuah event sudah didaftarkan.
    // Setiap event memiliki kunci sendiri, misalnya "event_clean".
    fun registered(id: String) =
        prefs.getBoolean("event_$id", false)

    // Menyimpan status pendaftaran event.
    // true = terdaftar, false = tidak terdaftar atau dibatalkan.
    fun register(id: String, value: Boolean) {
        prefs.edit()
            .putBoolean("event_$id", value)
            .apply()
    }

    // Memeriksa apakah pengguna sudah menyimpan minat pada postingan bantuan.
    // Contoh kunci penyimpanan: "help_h1".
    fun helped(id: String) =
        prefs.getBoolean("help_$id", false)

    // Menyimpan atau membatalkan status minat bantuan.
    fun help(id: String, value: Boolean) {
        prefs.edit()
            .putBoolean("help_$id", value)
            .apply()
    }

    // Membaca postingan bantuan lokal dan mengubahnya menjadi List<HelpPost>.
    // Fungsi ini hanya mengembalikan postingan tersimpan, bukan SampleData.help.
    fun posts(): List<HelpPost> {
        return try {
            // SharedPreferences menyimpan postingan sebagai teks JSON.
            // "[]" berarti array JSON kosong jika belum ada postingan.
            val data = JSONArray(
                prefs.getString("posts", "[]")
            )

            // until menghasilkan indeks dari 0 sampai sebelum data.length().
            // map mengubah setiap objek JSON menjadi objek HelpPost.
            (0 until data.length()).map { index ->
                val o = data.getJSONObject(index)

                HelpPost(
                    o.getString("id"),
                    o.getString("user"),
                    o.getString("title"),
                    o.getString("description"),
                    o.getString("kind"),
                    o.getString("time")
                )
            }
        } catch (_: org.json.JSONException) {
            // Jika format JSON atau properti yang diperlukan tidak valid,
            // kembalikan daftar kosong.
            // Penanganan ini tidak menghapus data JSON yang tersimpan.
            emptyList()
        }
    }

    // Menambahkan postingan baru ke penyimpanan lokal.
    // Validasi judul dan deskripsi dilakukan oleh formulir di MainActivity.
    fun addPost(
        title: String,
        description: String,
        kind: String
    ) {
        // Membaca array postingan yang sudah tersimpan.
        // Berbeda dari posts(), proses ini belum memiliki try-catch.
        val data = JSONArray(
            prefs.getString("posts", "[]")
        )

        // Membentuk objek JSON untuk satu postingan baru.
        data.put(
            JSONObject()

                // Waktu saat ini digunakan sebagai bagian pengenal postingan.
                // Ini pengenal sederhana, bukan jaminan ID unik seperti UUID.
                .put("id", "local_${System.currentTimeMillis()}")

                // Menyalin nama pengguna saat postingan dibuat.
                .put("user", name)

                .put("title", title)
                .put("description", description)
                .put("kind", kind)

                // Keterangan ini berupa teks tetap, bukan penghitung waktu.
                .put("time", "Baru saja")
        )

        // Mengubah seluruh array JSON menjadi teks lalu menyimpannya.
        // apply() memperbarui nilai di memori dan menjadwalkan penulisan ke disk.
        prefs.edit()
            .putString("posts", data.toString())
            .apply()
    }
}