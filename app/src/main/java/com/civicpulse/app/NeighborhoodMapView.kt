package com.civicpulse.app

// Context menyediakan akses ke resource dan informasi lingkungan aplikasi.
import android.content.Context

// Canvas adalah bidang gambar untuk membuat peta secara manual.
import android.graphics.Canvas

// Color mengubah kode warna menjadi nilai warna Android.
import android.graphics.Color

// Paint mengatur warna, ukuran teks, dan ketebalan garis.
import android.graphics.Paint

// AttributeSet menerima atribut ketika View dibuat melalui layout XML.
import android.util.AttributeSet

// MotionEvent memberikan informasi sentuhan pengguna.
import android.view.MotionEvent
import android.view.View

// hypot menghitung jarak: akar dari (x² + y²).
import kotlin.math.hypot

// Custom View untuk menggambar peta lingkungan secara ilustratif.
// Peta ini bekerja offline tanpa GPS, API key, atau koneksi internet.
//
// @JvmOverloads membuat variasi constructor agar kelas dapat digunakan
// dari kode maupun dibuat oleh Android melalui layout XML.
class NeighborhoodMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Objek Paint digunakan bersama untuk seluruh gambar.
    // ANTI_ALIAS_FLAG membuat tepi bentuk dan teks lebih halus.
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Daftar tempat yang akan ditampilkan sebagai marker.
    // Nilai awal menggunakan fasilitas contoh dari SampleData.
    var places = SampleData.places
        set(value) {
            // field adalah tempat penyimpanan nilai properti ini.
            // Menggunakan field mencegah setter memanggil dirinya sendiri.
            field = value

            // Meminta Android menggambar ulang View.
            // onDraw() nantinya akan menggunakan daftar tempat terbaru.
            invalidate()
        }

    // Tempat yang sedang dipilih.
    // Tanda ? berarti nilainya boleh null jika belum ada pilihan.
    var selected: LocalPlace? = null
        set(value) {
            field = value

            // Gambar ulang agar warna marker terpilih diperbarui.
            invalidate()
        }

    // Callback opsional ketika marker dipilih.
    // Pemanggil View dapat mengisinya untuk membuka dialog detail lokasi.
    var onPlaceClick: ((LocalPlace) -> Unit)? = null

    // Blok init dijalankan saat objek NeighborhoodMapView dibuat.
    init {
        isClickable = true
        isFocusable = true
    }

    // Fungsi bantu untuk mengganti warna Paint menggunakan kode heksadesimal.
    private fun color(hex: String) {
        paint.color = Color.parseColor(hex)
    }

    // Dipanggil Android saat View perlu digambar.
    // Canvas menggunakan koordinat piksel dengan titik (0, 0) di kiri atas.
    // Nilai x bertambah ke kanan, sedangkan y bertambah ke bawah.
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Lebar dan tinggi area peta dalam piksel.
        val w = width.toFloat()
        val h = height.toFloat()

        // Faktor kepadatan layar untuk mengubah ukuran dp menjadi piksel.
        // Contoh: 18 * dp berarti ukuran visual sebesar 18 dp.
        val dp = resources.displayMetrics.density

        // Menggambar latar peta dengan sudut membulat.
        color("#E4EFE7")
        canvas.drawRoundRect(
            0f,
            0f,
            w,
            h,
            18 * dp,
            18 * dp,
            paint
        )

        // Menggambar area hijau pertama.
        // Posisi menggunakan persentase ukuran View agar menyesuaikan layar.
        color("#CCE4D1")
        canvas.drawRoundRect(
            w * .04f,
            h * .12f,
            w * .31f,
            h * .54f,
            10f,
            10f,
            paint
        )

        // Menggambar area hijau kedua.
        // Radius sudut 10f di sini memakai satuan piksel.
        canvas.drawRoundRect(
            w * .55f,
            h * .48f,
            w * .94f,
            h * .86f,
            10f,
            10f,
            paint
        )

        // Jalan digambarkan sebagai garis putih setebal 12 dp.
        color("#FFFFFF")
        paint.strokeWidth = 12 * dp

        // Membuat empat garis jalan miring dari atas menuju bawah.
        for (i in 1..4) {
            canvas.drawLine(
                w * i / 5,
                0f,
                w * (i - 1) / 5,
                h,
                paint
            )
        }

        // Menambahkan dua jalan yang melintasi peta.
        canvas.drawLine(
            0f,
            h * .6f,
            w,
            h * .2f,
            paint
        )

        canvas.drawLine(
            0f,
            h * .9f,
            w,
            h * .5f,
            paint
        )

        // Mengatur ukuran dan warna tulisan nama jalan.
        paint.textSize = 10 * dp
        color("#64748B")

        // Koordinat y pada drawText menunjukkan posisi garis dasar teks.
        canvas.drawText(
            "Jl. Melati",
            w * .42f,
            h * .18f,
            paint
        )

        // Menggambar marker untuk setiap tempat pada daftar.
        for (place in places) {

            // Mengubah posisi relatif menjadi koordinat piksel.
            // Contoh place.x = 0.23 berarti 23% dari lebar peta.
            val x = w * place.x
            val y = h * place.y

            // Marker terpilih menggunakan warna gold.
            // Marker lainnya menggunakan warna teal.
            color(
                if (selected == place) {
                    "#FBBF24"
                } else {
                    "#0F766E"
                }
            )

            // Bagian kepala marker berbentuk lingkaran.
            canvas.drawCircle(
                x,
                y,
                11 * dp,
                paint
            )

            // Bagian bawah marker dibuat sebagai segitiga menggunakan Path.
            canvas.drawPath(
                android.graphics.Path().apply {
                    // Titik awal di bagian kiri bawah lingkaran.
                    moveTo(
                        x - 6 * dp,
                        y + 7 * dp
                    )

                    // Titik ujung marker.
                    lineTo(
                        x,
                        y + 17 * dp
                    )

                    // Titik kanan bawah lingkaran.
                    lineTo(
                        x + 6 * dp,
                        y + 7 * dp
                    )

                    // Menutup bentuk dengan menghubungkan titik akhir ke awal.
                    close()
                },
                paint
            )

            // Lingkaran putih kecil di tengah kepala marker.
            color("#FFFFFF")
            canvas.drawCircle(
                x,
                y,
                4 * dp,
                paint
            )
        }

        // Menambahkan keterangan pada bagian bawah peta.
        color("#1F2937")
        paint.textSize = 10 * dp

        canvas.drawText(
            "Peta ilustratif • RW 04 • ketuk pin",
            12 * dp,
            h - 10 * dp,
            paint
        )
    }

    // Menjalankan mekanisme klik bawaan View.
    // Pemanggilan super membantu menjaga perilaku klik dan aksesibilitas.
    // Pemilihan marker ditangani secara terpisah di onTouchEvent().
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // Menangani sentuhan pada area peta.
    override fun onTouchEvent(event: MotionEvent): Boolean {

        // ACTION_UP terjadi ketika pengguna mengangkat jari.
        // Kode memilih marker berdasarkan posisi akhir sentuhan.
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()

            // Cari tempat yang pusat markernya paling dekat dengan sentuhan.
            // minByOrNull mengembalikan null jika daftar places kosong.
            val nearest = places.minByOrNull {
                hypot(
                    (event.x - width * it.x).toDouble(),
                    (event.y - height * it.y).toDouble()
                )
            }

            // Marker hanya dipilih jika:
            // 1. Ada tempat yang ditemukan.
            // 2. Jarak sentuhan ke pusat marker kurang dari 42 dp.
            if (
                nearest != null &&
                hypot(
                    (event.x - width * nearest.x).toDouble(),
                    (event.y - height * nearest.y).toDouble()
                ) < 42 * resources.displayMetrics.density
            ) {
                // Mengubah pilihan akan memanggil setter dan invalidate().
                selected = nearest

                // Jalankan callback jika sudah diisi oleh pemanggil View.
                // ?. mencegah pemanggilan ketika callback masih null.
                onPlaceClick?.invoke(nearest)
            }

            // Sentuhan akhir dianggap sudah ditangani,
            // termasuk jika tidak ada marker dalam batas jarak.
            return true
        }

        // ACTION_DOWN diterima agar View dapat melanjutkan penanganan sentuhan.
        // Untuk jenis event lain, kode ini mengembalikan false.
        return event.action == MotionEvent.ACTION_DOWN
    }
}