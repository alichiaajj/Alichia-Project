package com.civicpulse.app

// Komponen Android yang digunakan dalam pengujian.
import android.app.AlertDialog
import android.widget.*
import android.view.View

// Fungsi pemeriksaan hasil, seperti assertEquals dan assertTrue.
import org.junit.Assert.*

// Anotasi untuk persiapan dan penandaan fungsi tes.
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Robolectric menjalankan tes komponen Android pada JVM,
// tanpa harus menyalakan emulator atau menghubungkan HP.
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.android.controller.ActivityController

// Menggunakan test runner Robolectric untuk menjalankan kelas pengujian ini.
@RunWith(RobolectricTestRunner::class)

// Menjalankan tes dengan lingkungan Android API 28, yaitu Android 9.
@Config(sdk = [28])
class CivicFlowTest {

    // Controller mengatur siklus hidup Activity selama pengujian.
    // Contohnya membuat Activity dan membuat ulang Activity.
    private lateinit var controller: ActivityController<MainActivity>

    // Referensi Activity yang sedang diuji.
    private lateinit var activity: MainActivity

    // @Before membuat fungsi ini dijalankan sebelum setiap tes.
    @Before
    fun setup() {
        // Membuat MainActivity dan menjalankan tahapan awal siklus hidupnya.
        controller = Robolectric
            .buildActivity(MainActivity::class.java)
            .setup()

        // Mengambil objek Activity dari controller.
        activity = controller.get()

        // Menghapus data preferensi dalam lingkungan tes agar status pengguna
        // dari tes lain tidak memengaruhi pemeriksaan berikutnya.
        // Angka 0 berarti MODE_PRIVATE.
        // commit() menunggu proses penyimpanan selesai dan mengembalikan Boolean.
        activity.getSharedPreferences("civic_pulse", 0)
            .edit()
            .clear()
            .commit()
    }

    // Fungsi bantu untuk mengeklik View berdasarkan ID resource.
    private fun click(id: Int) {
        activity.findViewById<View>(id).performClick()

        // Memproses tugas yang sedang antre pada Looper utama Robolectric.
        // Ini membantu memastikan aksi klik diproses sebelum hasil diperiksa.
        shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    // Mengumpulkan sebuah View beserta seluruh turunannya secara rekursif.
    // Fungsi ini dipakai untuk mencari komponen di dalam layout atau dialog.
    private fun all(view: View): List<View> =
        listOf(view) + if (view is android.view.ViewGroup) {
            // ViewGroup dapat memiliki anak, sehingga setiap anak ditelusuri.
            (0 until view.childCount).flatMap {
                all(view.getChildAt(it))
            }
        } else {
            // View biasa tidak memiliki anak untuk ditelusuri.
            emptyList()
        }

    // Fungsi bantu untuk mengeklik komponen berdasarkan teks yang ditampilkan.
    private fun tap(text: String) {
        // Mengambil wadah utama tampilan Activity.
        val root = activity.findViewById<View>(android.R.id.content)

        // Mencari TextView pertama yang teksnya sama persis.
        // Button juga merupakan turunan TextView sehingga ikut dapat ditemukan.
        // first() akan membuat tes gagal jika komponen tidak ditemukan.
        val target = all(root)
            .filterIsInstance<TextView>()
            .first {
                it.text.toString() == text
            }

        if (target.isClickable) {
            // Klik langsung jika komponen teks dapat diklik.
            target.performClick()
        } else {
            // Label navigasi dapat berada di dalam wadah yang menangani klik.
            (target.parent as View).performClick()
        }

        // Menyelesaikan tugas UI yang antre setelah klik.
        shadowOf(android.os.Looper.getMainLooper()).idle()
    }

    // Membuka Beranda melalui tombol Mulai dan Lewati.
    private fun home() {
        click(R.id.start)
        click(R.id.skip)
    }

    // Memeriksa apakah judul toolbar sesuai dengan halaman yang diharapkan.
    private fun page(expected: String) {
        assertEquals(
            expected,
            activity.findViewById<TextView>(R.id.page_title)
                .text.toString()
        )
    }

    // TES 1:
    // Memastikan Splash dan ketiga langkah onboarding dapat menuju Beranda.
    @Test
    fun splashAndThreeOnboardingStepsReachHome() {
        // Tombol Mulai harus tersedia pada Splash.
        assertNotNull(
            activity.findViewById<View>(R.id.start)
        )

        click(R.id.start)

        // Judul onboarding pertama harus memuat kata Bersama.
        assertTrue(
            activity.findViewById<TextView>(R.id.onboard_title)
                .text.contains("Bersama")
        )

        // Klik Lanjut untuk membuka langkah kedua.
        click(R.id.next)

        assertEquals(
            "Kenali Sekitarmu",
            activity.findViewById<TextView>(R.id.onboard_title)
                .text.toString()
        )

        // Klik Lanjut untuk membuka langkah ketiga.
        click(R.id.next)

        // Pada langkah terakhir, label tombol harus berubah.
        assertEquals(
            "Mulai Bersama",
            activity.findViewById<Button>(R.id.next)
                .text.toString()
        )

        // Tombol terakhir harus membuka Beranda.
        click(R.id.next)
        page("Civic Pulse")
    }

    // TES 2:
    // Memastikan Lewati bekerja dan semua tujuan navigasi bawah dapat dibuka.
    @Test
    fun skipAndEveryBottomNavigationDestinationWorks() {
        home()

        tap("Komunitas")
        page("Komunitas")

        // Halaman komunitas harus menggunakan ArrayAdapter.
        // Tes ini memeriksa jenis adapter, bukan langsung resource layout barisnya.
        assertTrue(
            activity.findViewById<ListView>(R.id.simple_list)
                .adapter is ArrayAdapter<*>
        )

        tap("Event")
        page("Event")

        // Daftar event harus menggunakan Custom Adapter EventAdapter.
        assertTrue(
            activity.findViewById<ListView>(R.id.list)
                .adapter is EventAdapter
        )

        tap("Mutual Help")
        page("Mutual Help")

        // Daftar bantuan harus menggunakan Custom Adapter HelpAdapter.
        assertTrue(
            activity.findViewById<ListView>(R.id.list)
                .adapter is HelpAdapter
        )

        tap("Profil")
        page("Profil")

        tap("Beranda")
        page("Civic Pulse")
    }

    // TES 3:
    // Memastikan pencarian lokasi memperbarui daftar dan data marker peta.
    @Test
    fun localSearchFiltersPlacesAndMap() {
        home()
        tap("Lokasi Lokal")
        page("Jelajah Lokal")

        val list = activity.findViewById<ListView>(R.id.list)

        // Data awal berisi empat fasilitas.
        assertEquals(4, list.adapter.count)

        // setText() memicu TextWatcher yang dipasang pada kolom pencarian.
        activity.findViewById<EditText>(R.id.search)
            .setText("Puskesmas")

        // Pencarian Puskesmas harus menghasilkan satu fasilitas.
        assertEquals(1, list.adapter.count)

        // Data tempat yang digambar pada peta juga harus berjumlah satu.
        assertEquals(
            1,
            activity.findViewById<NeighborhoodMapView>(R.id.local_map)
                .places.size
        )

        // Kata yang tidak cocok harus menghasilkan daftar kosong.
        activity.findViewById<EditText>(R.id.search)
            .setText("xyz-no-result")

        assertEquals(0, list.adapter.count)
    }

    // TES 4:
    // Memastikan konfirmasi pendaftaran disimpan
    // dan filter event menghasilkan jumlah data yang sesuai.
    @Test
    fun eventRegistrationPersistsAndFilters() {
        home()
        tap("Event")

        val list = activity.findViewById<ListView>(R.id.list)

        // Meminta adapter membuat kartu untuk data pertama.
        // null berarti tidak menggunakan View lama yang didaur ulang.
        val row = list.adapter.getView(0, null, list)

        // Menekan tombol Daftar pada kartu pertama.
        row.findViewById<Button>(R.id.card_action).performClick()

        // Mengambil dialog terakhir dan menekan tombol konfirmasi.
        ShadowAlertDialog.getLatestAlertDialog()
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()

        // Callback tombol dialog dapat masuk ke antrean Looper utama.
        shadowOf(android.os.Looper.getMainLooper()).idle()

        // Membuat pembungkus baru untuk membaca preferensi yang sama.
        val store = CivicStore(
            activity.getSharedPreferences("civic_pulse", 0)
        )

        // Event pertama dengan ID clean harus tercatat sebagai terdaftar.
        assertTrue(store.registered("clean"))

        // Filter Terdaftar harus menampilkan satu event.
        tap("Terdaftar")

        assertEquals(
            1,
            activity.findViewById<ListView>(R.id.list).adapter.count
        )

        // Data contoh memiliki satu event yang berstatus Selesai.
        tap("Selesai")

        assertEquals(
            1,
            activity.findViewById<ListView>(R.id.list).adapter.count
        )
    }

    // TES 5:
    // Memastikan filter penawaran dan konfirmasi permintaan bantuan bekerja.
    @Test
    fun helpConfirmationPersistsAndOfferFilterWorks() {
        home()
        tap("Mutual Help")
        tap("Menawarkan")

        val list = activity.findViewById<ListView>(R.id.list)

        // Data contoh memiliki satu postingan penawaran bantuan.
        assertEquals(1, list.adapter.count)

        val row = list.adapter.getView(0, null, list)

        // Kartu penawaran harus menampilkan tombol Minta Bantuan.
        assertEquals(
            "Minta Bantuan",
            row.findViewById<Button>(R.id.card_action)
                .text.toString()
        )

        row.findViewById<Button>(R.id.card_action).performClick()

        // Menyetujui dialog permintaan bantuan.
        ShadowAlertDialog.getLatestAlertDialog()
            .getButton(AlertDialog.BUTTON_POSITIVE)
            .performClick()

        shadowOf(android.os.Looper.getMainLooper()).idle()

        // Status minat bantuan untuk postingan h3 harus tersimpan.
        assertTrue(
            CivicStore(
                activity.getSharedPreferences("civic_pulse", 0)
            ).helped("h3")
        )
    }

    // TES 6:
    // Memastikan formulir tetap terbuka saat kosong
    // dan dapat menyimpan postingan setelah diisi dengan data valid.
    @Test
    fun helpFormRejectsEmptyAndSavesValidPost() {
        home()
        tap("Mutual Help")
        click(R.id.add_help)

        val dialog = ShadowAlertDialog.getLatestAlertDialog()

        // Mencoba menyimpan ketika kolom belum diisi.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        // Dialog harus tetap terbuka karena input belum valid.
        assertTrue(dialog.isShowing)

        // Mengambil seluruh EditText dari hierarki tampilan dialog.
        // !! mengasumsikan window tersedia; tes akan gagal jika ternyata null.
        val inputs = all(dialog.window!!.decorView)
            .filterIsInstance<EditText>()

        // Urutan kolom mengikuti susunan formulir: judul, lalu deskripsi.
        inputs[0].setText("Butuh pinjam tangga")
        inputs[1].setText(
            "Untuk memperbaiki lampu teras Sabtu pagi di RW 04."
        )

        // Menyimpan ulang setelah input valid.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        // Penyimpanan lokal harus berisi satu postingan baru.
        assertEquals(
            1,
            CivicStore(
                activity.getSharedPreferences("civic_pulse", 0)
            ).posts().size
        )

        // Daftar menampilkan satu postingan lokal ditambah tiga data contoh.
        assertEquals(
            4,
            activity.findViewById<ListView>(R.id.list).adapter.count
        )
    }

    // TES 7:
    // Memastikan halaman dan pencarian dipulihkan saat Activity dibuat ulang.
    @Test
    fun recreationRestoresPageAndSearch() {
        home()
        tap("Lokasi Lokal")

        activity.findViewById<EditText>(R.id.search)
            .setText("Taman")

        // Mensimulasikan pembuatan ulang Activity,
        // seperti yang dapat terjadi saat perubahan konfigurasi layar.
        controller.recreate()

        // Mengambil instance Activity yang baru.
        activity = controller.get()

        // Halaman harus tetap Jelajah Lokal.
        page("Jelajah Lokal")

        // Kata pencarian harus tetap tersimpan pada kolom.
        assertEquals(
            "Taman",
            activity.findViewById<EditText>(R.id.search)
                .text.toString()
        )

        // Hasil pencarian juga harus tetap berjumlah satu.
        assertEquals(
            1,
            activity.findViewById<ListView>(R.id.list).adapter.count
        )
    }

    // TES 8:
    // Memastikan status gabung komunitas dan perubahan nama
    // tersimpan serta digunakan pada sapaan Beranda.
    @Test
    fun profileNameAndCommunityMembershipPersist() {
        home()
        tap("Komunitas")

        // Menekan tombol Gabung Komunitas.
        click(R.id.simple_action)

        // Status keanggotaan harus berubah menjadi true.
        assertTrue(
            CivicStore(
                activity.getSharedPreferences("civic_pulse", 0)
            ).joined
        )

        tap("Profil")

        // Menekan tombol Edit Nama.
        click(R.id.simple_action)

        val dialog = ShadowAlertDialog.getLatestAlertDialog()

        // Mengambil kolom nama pertama di dalam dialog dan mengisinya.
        all(dialog.window!!.decorView)
            .filterIsInstance<EditText>()
            .first()
            .setText("Alichia")

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick()

        // Nama baru harus dapat dibaca dari penyimpanan lokal.
        assertEquals(
            "Alichia",
            CivicStore(
                activity.getSharedPreferences("civic_pulse", 0)
            ).name
        )

        // Sapaan Beranda harus memakai nama yang baru.
        tap("Beranda")

        assertTrue(
            activity.findViewById<TextView>(R.id.greeting)
                .text.contains("Alichia")
        )
    }
}