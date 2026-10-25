package com.civicpulse.app

// Import kelas Android untuk Activity, dialog, warna, layout, dan komponen tampilan.
import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

// Activity utama menjadi wadah semua halaman aplikasi.
// Pergantian halaman dilakukan dengan mengganti View di dalam FrameLayout.
class MainActivity : Activity() {

    // Menangani data lokal, seperti nama, pendaftaran event, dan postingan bantuan.
    // lateinit berarti variabel akan diisi kemudian sebelum digunakan.
    private lateinit var store: CivicStore

    // Wadah untuk menampilkan layout halaman yang sedang dibuka.
    private lateinit var content: FrameLayout

    // Nama halaman aktif. Saat pertama dibuka, aplikasi menampilkan Splash.
    private var page = "splash"

    // Posisi onboarding: 0 = pertama, 1 = kedua, 2 = ketiga.
    private var step = 0

    // Menyimpan teks pencarian pengguna.
    private var query = ""

    // Menyimpan kategori atau status yang sedang dipilih.
    private var filter = ""

    // Menyimpan riwayat halaman untuk navigasi kembali.
    private val history = ArrayDeque<String>()

    // Warna utama untuk komponen aktif.
    private val teal = Color.parseColor("#0F766E")

    // Warna untuk teks tambahan dan menu yang tidak aktif.
    private val muted = Color.parseColor("#64748B")

    // Data tiga halaman onboarding.
    // Operator "to" membentuk Pair:
    // first berisi judul dan second berisi deskripsi.
    private val onboarding = listOf(
        "Kita Lebih Kuat\nBersama" to
                "Bergabung dengan komunitas, akses layanan lokal, dan jadikan lingkungan sekitar lebih baik.",

        "Kenali Sekitarmu" to
                "Temukan fasilitas, layanan warga, dan kegiatan menarik yang dekat dengan rumahmu.",

        "Hal Kecil,\nDampak Besar" to
                "Tawarkan bantuan atau temukan tetangga yang siap membantu. Mulai kebaikan dari sekitar kita."
    )

    // Dipanggil saat Activity dibuat, termasuk ketika dibuat ulang setelah rotasi.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Membuka SharedPreferences bernama civic_pulse.
        // CivicStore menggunakan penyimpanan ini untuk data aplikasi.
        store = CivicStore(
            getSharedPreferences("civic_pulse", MODE_PRIVATE)
        )

        // Memasang layout utama yang berisi toolbar, content, dan menu bawah.
        setContentView(R.layout.activity_main)

        // Menambahkan jarak sesuai ukuran bilah status dan navigasi sistem.
        // Tujuannya agar komponen aplikasi tidak tertutup oleh bilah tersebut.
        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(
                insets.systemWindowInsetLeft,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                insets.systemWindowInsetBottom
            )

            // Mengembalikan insets agar dapat diteruskan ke hierarki View.
            insets
        }

        // Meminta Android mengirimkan ukuran area bilah sistem.
        findViewById<View>(R.id.root).requestApplyInsets()

        // Menghubungkan variabel content dengan FrameLayout pada XML.
        content = findViewById(R.id.content)

        // Mengembalikan status tampilan jika Activity sebelumnya dibuat ulang.
        // ?. membaca nilai hanya jika savedInstanceState tidak null.
        // ?: memberikan nilai awal jika tidak ada data tersimpan.
        step = savedInstanceState?.getInt("step") ?: 0
        query = savedInstanceState?.getString("query") ?: ""
        filter = savedInstanceState?.getString("filter") ?: ""

        // Mengembalikan riwayat navigasi.
        savedInstanceState
            ?.getStringArrayList("history")
            ?.forEach {
                history.addLast(it)
            }

        // Membuka halaman terakhir, atau Splash jika belum ada status tersimpan.
        // push = false: tidak menambah riwayat.
        // restore = true: mempertahankan pencarian dan filter yang dipulihkan.
        show(
            savedInstanceState?.getString("page") ?: "splash",
            false,
            true
        )
    }

    // Menyimpan status UI sementara sebelum Activity dibuat ulang.
    // Data ini berbeda dari data pengguna yang disimpan melalui CivicStore.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("page", page)
        outState.putInt("step", step)
        outState.putString("query", query)
        outState.putString("filter", filter)

        // Mengubah ArrayDeque menjadi ArrayList agar dapat disimpan dalam Bundle.
        outState.putStringArrayList("history", ArrayList(history))
    }

    // Mengubah ukuran dp menjadi piksel sesuai kepadatan layar perangkat.
    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    // Mengubah judul pada toolbar.
    private fun title(value: String) {
        findViewById<TextView>(R.id.page_title).text = value
    }

    // Mengubah layout XML menjadi View lalu memasangnya ke content.
    // false berarti pemasangan ke parent dilakukan sendiri melalui addView().
    private fun inflate(layout: Int): View =
        layoutInflater.inflate(layout, content, false).also {
            content.addView(it)
        }

    // Fungsi utama untuk berpindah halaman.
    // target: nama halaman tujuan.
    // push: menentukan apakah halaman lama masuk ke riwayat.
    // restore: menentukan apakah pencarian dan filter dipertahankan.
    private fun show(
        target: String,
        push: Boolean = true,
        restore: Boolean = false
    ) {
        // Simpan halaman lama jika tujuan berbeda dan halaman lama bukan intro.
        if (
            push &&
            target != page &&
            page !in listOf("splash", "onboarding")
        ) {
            history.addLast(page)
        }

        // Mengubah halaman aktif.
        page = target

        // Halaman baru dimulai tanpa pencarian/filter,
        // kecuali sedang melakukan refresh atau pemulihan status.
        if (!restore) {
            query = ""
            filter = ""
        }

        // Menghapus tampilan halaman sebelumnya.
        content.removeAllViews()

        // Splash dan onboarding termasuk halaman pengenalan.
        val intro = page == "splash" || page == "onboarding"

        // Toolbar dan menu bawah disembunyikan pada halaman pengenalan.
        findViewById<View>(R.id.toolbar).visibility =
            if (intro) View.GONE else View.VISIBLE

        findViewById<View>(R.id.bottom_nav).visibility =
            if (intro) View.GONE else View.VISIBLE

        // Tombol kembali tidak ditampilkan pada Beranda.
        findViewById<View>(R.id.back).visibility =
            if (page == "home") View.GONE else View.VISIBLE

        // Menjalankan navigasi kembali saat tombol panah ditekan.
        findViewById<View>(R.id.back).setOnClickListener {
            goBack()
        }

        // Membuka halaman notifikasi ketika ikon lonceng ditekan.
        findViewById<View>(R.id.notifications).setOnClickListener {
            showNotifications()
        }

        // Memilih tampilan berdasarkan nama halaman.
        when (page) {
            "splash" -> {
                inflate(R.layout.page_splash)
                    .findViewById<View>(R.id.start)
                    .setOnClickListener {
                        // Mulai onboarding dari langkah pertama.
                        step = 0
                        show("onboarding", false)
                    }
            }

            "onboarding" -> showOnboarding()
            "home" -> showHome()
            "local" -> showLocal()
            "events" -> showEvents()
            "help" -> showHelp()
            "community" -> showCommunity()
            "profile" -> showProfile()
            "notifications" -> notificationsPage()
        }

        // Memperbarui navigasi bawah untuk halaman utama.
        if (!intro) {
            bottomNavigation()
        }
    }

    // Menampilkan onboarding sesuai indeks step.
    private fun showOnboarding() {
        val view = inflate(R.layout.page_onboarding)

        // Mengambil judul dan deskripsi dari Pair pada daftar onboarding.
        view.findViewById<TextView>(R.id.onboard_title).text =
            onboarding[step].first

        view.findViewById<TextView>(R.id.onboard_body).text =
            onboarding[step].second

        // Menampilkan tiga indikator posisi onboarding.
        view.findViewById<TextView>(R.id.dots).apply {
            text = (0..2).joinToString("  ") {
                if (it == step) "●" else "○"
            }

            // Teks bantuan untuk pembaca layar.
            contentDescription = "Halaman ${step + 1} dari 3"
        }

        view.findViewById<Button>(R.id.next).apply {
            // Langkah terakhir menggunakan label yang berbeda.
            text = if (step == 2) "Mulai Bersama" else "Lanjut"

            setOnClickListener {
                if (step < 2) {
                    // Berpindah ke langkah berikutnya.
                    step++
                    show("onboarding", false)
                } else {
                    // Setelah langkah terakhir, buka Beranda.
                    show("home", false)
                }
            }
        }

        // Lewati langsung membuka Beranda.
        view.findViewById<View>(R.id.skip).setOnClickListener {
            show("home", false)
        }

        // Mengganti ilustrasi pada langkah kedua.
        view.findViewById<ImageView>(R.id.onboard_image).setImageResource(
            if (step == 1) {
                R.drawable.neighborhood
            } else {
                R.drawable.community_hero
            }
        )
    }

    // Membuat lima menu navigasi bawah melalui kode.
    private fun bottomNavigation() {
        val bar = findViewById<LinearLayout>(R.id.bottom_nav)

        // Hapus menu lama agar tidak terjadi penumpukan.
        bar.removeAllViews()

        // Triple berisi tujuan halaman, label, dan ikon.
        val items = listOf(
            Triple("home", "Beranda", R.drawable.ic_home),
            Triple("community", "Komunitas", R.drawable.ic_people),
            Triple("events", "Event", R.drawable.ic_event),
            Triple("help", "Mutual Help", R.drawable.ic_heart),
            Triple("profile", "Profil", R.drawable.ic_profile)
        )

        items.forEach { (target, label, icon) ->
            // Wadah setiap menu berisi ikon di atas dan teks di bawah.
            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                minimumHeight = dp(56)
                contentDescription = label
                isClickable = true
                isFocusable = true
            }

            // Jelajah Lokal tetap menandai Beranda sebagai menu aktif.
            val active =
                target == page || (page == "local" && target == "home")

            // Ikon menggunakan warna utama jika menu aktif.
            val image = ImageView(this).apply {
                setImageResource(icon)
                imageTintList = ColorStateList.valueOf(
                    if (active) teal else muted
                )
            }

            box.addView(
                image,
                LinearLayout.LayoutParams(dp(22), dp(22))
            )

            // Label menu juga mengikuti warna status aktif.
            box.addView(
                TextView(this).apply {
                    text = label
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setTextColor(if (active) teal else muted)
                    setPadding(0, dp(5), 0, 0)
                }
            )

            box.setOnClickListener {
                // Perpindahan tab memulai riwayat navigasi baru.
                history.clear()
                show(target, false)
            }

            // Lebar 0 dan weight 1 membagi ruang menu secara merata.
            bar.addView(
                box,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
            )
        }
    }

    // Menampilkan Beranda beserta pintasan dan event terdekat.
    private fun showHome() {
        title("Civic Pulse")

        val view = inflate(R.layout.page_home)

        // Nama sapaan mengikuti nama yang tersimpan di Profil.
        view.findViewById<TextView>(R.id.greeting).text =
            "Halo, ${store.name} ☀"

        val shortcuts = view.findViewById<LinearLayout>(R.id.shortcuts)

        // Menyiapkan empat pintasan fitur pada Beranda.
        listOf(
            Triple("community", "Komunitas", R.drawable.ic_people),
            Triple("local", "Lokasi Lokal", R.drawable.ic_pin),
            Triple("events", "Event", R.drawable.ic_event),
            Triple("help", "Mutual Help", R.drawable.ic_heart)
        ).forEachIndexed { i, (target, label, icon) ->

            val box = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                contentDescription = label
            }

            val circle = ImageView(this).apply {
                setImageResource(icon)
                imageTintList = ColorStateList.valueOf(Color.WHITE)
                setPadding(dp(12), dp(12), dp(12), dp(12))

                // Membuat latar lingkaran dengan warna berbeda untuk setiap fitur.
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(
                        Color.parseColor(
                            listOf(
                                "#0F766E",
                                "#F59E0B",
                                "#8B5CF6",
                                "#14B8A6"
                            )[i]
                        )
                    )
                }
            }

            box.addView(
                circle,
                LinearLayout.LayoutParams(dp(48), dp(48))
            )

            box.addView(
                TextView(this).apply {
                    text = label
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setTextColor(teal)
                    setPadding(0, dp(8), 0, 0)
                }
            )

            // Setiap pintasan memperoleh bagian lebar yang sama.
            shortcuts.addView(
                box,
                LinearLayout.LayoutParams(0, dp(86), 1f)
            )

            box.setOnClickListener {
                show(target)
            }
        }

        // Banner membuka komunitas.
        view.findViewById<View>(R.id.banner).setOnClickListener {
            show("community")
        }

        // Lihat semua membuka halaman daftar event.
        view.findViewById<View>(R.id.see_events).setOnClickListener {
            show("events")
        }

        // Pada Beranda, kartu event ditempatkan dalam LinearLayout.
        val holder = view.findViewById<LinearLayout>(R.id.home_events)

        // Custom Adapter menghasilkan tampilan kartu dan menangani klik Daftar.
        val adapter = EventAdapter(this, store) {
            registerEvent(it)
        }

        // Fungsi lokal untuk menampilkan ulang hasil pencarian event.
        fun render(term: String) {
            val matches = SampleData.events.filter {
                // Cari event yang belum selesai dan cocok dengan kata pencarian.
                // true pada contains berarti pencarian mengabaikan besar/kecil huruf.
                it.phase != "Selesai" &&
                        "${it.title} ${it.location} ${it.category}"
                            .contains(term, true)
            }.take(2) // Beranda hanya menampilkan maksimal dua hasil.

            adapter.submit(matches)
            holder.removeAllViews()

            // Menampilkan pesan jika pencarian tidak menghasilkan event.
            if (matches.isEmpty()) {
                holder.addView(
                    TextView(this).apply {
                        text =
                            "Tidak ada event yang cocok. Coba menu Lokasi Lokal untuk mencari fasilitas."
                        setTextColor(muted)
                    }
                )
            }

            // Mengambil tampilan kartu dari adapter lalu memasangnya ke Beranda.
            matches.indices.forEach { i ->
                holder.addView(
                    adapter.getView(i, null, holder),
                    LinearLayout.LayoutParams(-1, -2).apply {
                        // -1 = MATCH_PARENT, -2 = WRAP_CONTENT.
                        bottomMargin = dp(12)
                    }
                )
            }
        }

        // Pencarian memanggil render setiap kali teks berubah.
        watchSearch(view, "Cari event atau kegiatan…") {
            render(it)
        }

        // Menampilkan data awal tanpa menunggu pengguna mengetik.
        render(query)
    }

    // Menghubungkan EditText pencarian dengan fungsi pembaruan hasil.
    // update menerima teks pencarian terbaru.
    private fun watchSearch(
        view: View,
        hint: String,
        update: (String) -> Unit
    ) {
        view.findViewById<EditText>(R.id.search).apply {
            this.hint = hint

            // Memulihkan kata pencarian dan menaruh kursor di akhir teks.
            setText(query)
            setSelection(text.length)

            addTextChangedListener(object : TextWatcher {
                // Callback wajib dari TextWatcher, tetapi tidak digunakan di sini.
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                // Dipanggil ketika isi teks berubah.
                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    query = s.toString()
                    update(query)
                }

                // Callback wajib lain yang tidak memerlukan tindakan tambahan.
                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }

    // Membuat tombol kategori berbentuk chip.
    private fun chips(
        view: View,
        labels: List<String>,
        update: () -> Unit
    ) {
        // Gunakan pilihan pertama jika filter lama tidak ada pada halaman ini.
        if (filter !in labels) {
            filter = labels.first()
        }

        val group = view.findViewById<LinearLayout>(R.id.filters)

        // Menggambar ulang chip agar warna pilihan aktif selalu sesuai.
        fun draw() {
            group.removeAllViews()

            labels.forEach { label ->
                val chip = TextView(this).apply {
                    text = label
                    textSize = 12f
                    gravity = Gravity.CENTER
                    setPadding(dp(14), 0, dp(14), 0)

                    setTextColor(
                        if (filter == label) Color.WHITE else teal
                    )

                    // Membuat latar membulat dan membedakan warna chip aktif.
                    background = GradientDrawable().apply {
                        cornerRadius = dp(24).toFloat()
                        setColor(
                            if (filter == label) {
                                teal
                            } else {
                                Color.parseColor("#ECF6F4")
                            }
                        )
                    }

                    isClickable = true
                    isFocusable = true
                    isSelected = filter == label

                    setOnClickListener {
                        // Simpan pilihan, perbarui chip, lalu perbarui daftar.
                        filter = label
                        draw()
                        update()
                    }
                }

                group.addView(
                    chip,
                    LinearLayout.LayoutParams(-2, dp(36)).apply {
                        marginEnd = dp(8)
                    }
                )
            }
        }

        draw()
    }

    // Menampilkan jumlah hasil dan menentukan View untuk kondisi daftar kosong.
    private fun bindEmpty(view: View, count: Int, noun: String) {
        view.findViewById<TextView>(R.id.result_count).text =
            "$count $noun • contoh lingkungan RW 04"

        // ListView akan menampilkan pesan empty saat jumlah item adapter nol.
        view.findViewById<ListView>(R.id.list).emptyView =
            view.findViewById(R.id.empty)
    }

    // Menampilkan fasilitas lokal, pencarian, kategori, dan peta ilustratif.
    private fun showLocal() {
        title("Jelajah Lokal")

        val view = inflate(R.layout.page_local)

        // Tombol pada kartu lokasi membuka dialog detail melalui route().
        val adapter = PlaceAdapter(this) {
            route(it)
        }

        view.findViewById<ListView>(R.id.list).adapter = adapter

        val map =
            view.findViewById<NeighborhoodMapView>(R.id.local_map)

        // Memperbarui daftar dan marker berdasarkan kategori serta pencarian.
        fun update() {
            val items = SampleData.places.filter {
                (filter == "Semua" || it.category == filter) &&
                        "${it.name} ${it.address} ${it.category}"
                            .contains(query, true)
            }

            adapter.submit(items)
            map.places = items
            bindEmpty(view, items.size, "lokasi")
        }

        // Mengetuk marker membuka dialog yang sama dengan tombol Rute.
        map.onPlaceClick = {
            route(it)
        }

        chips(
            view,
            listOf(
                "Semua",
                "Fasilitas Umum",
                "Toko & Kuliner",
                "Kesehatan"
            )
        ) {
            update()
        }

        watchSearch(view, "Cari lokasi, layanan, atau tempat…") {
            update()
        }

        update()
    }

    // Menampilkan informasi lokasi dan pilihan untuk menyorotnya pada peta.
    // Tidak melakukan perhitungan rute GPS sungguhan.
    private fun route(place: LocalPlace) {
        AlertDialog.Builder(this)
            .setTitle(place.name)
            .setMessage(
                "${place.address}\n" +
                        "${place.category} • ${place.hours}\n\n" +
                        "Jarak contoh: ${place.distance} dari Balai RW 04.\n\n" +
                        "Peta ini representasi ilustratif lingkungan, bukan rute GPS atau petunjuk jalan nyata."
            )
            .setPositiveButton("Lihat di peta") { _, _ ->
                // Pastikan halaman lokasi sudah terbuka.
                if (page != "local") {
                    show("local")
                }

                // Menyorot marker fasilitas yang dipilih.
                content.findViewById<NeighborhoodMapView>(R.id.local_map)
                    ?.selected = place

                // Mencari posisi fasilitas pada daftar yang sedang difilter.
                val index = SampleData.places.filter {
                    (filter == "Semua" || it.category == filter) &&
                            "${it.name} ${it.address} ${it.category}"
                                .contains(query, true)
                }.indexOf(place)

                // Scroll hanya dilakukan jika fasilitas ditemukan pada hasil.
                if (index >= 0) {
                    content.findViewById<ListView>(R.id.list)
                        ?.smoothScrollToPosition(index)
                }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    // Menampilkan daftar event menggunakan Custom Adapter.
    private fun showEvents() {
        title("Event")

        val view = inflate(R.layout.page_events)

        val adapter = EventAdapter(this, store) {
            registerEvent(it)
        }

        view.findViewById<ListView>(R.id.list).adapter = adapter

        fun update() {
            val items = SampleData.events.filter {
                // Filter Terdaftar menggunakan status di penyimpanan lokal.
                // Filter lain menggunakan nilai phase pada data event.
                (
                        if (filter == "Terdaftar") {
                            store.registered(it.id)
                        } else {
                            it.phase == filter
                        }
                        ) &&
                        "${it.title} ${it.category} ${it.location}"
                            .contains(query, true)
            }

            adapter.submit(items)
            bindEmpty(view, items.size, "kegiatan")
        }

        chips(
            view,
            listOf("Terdekat", "Mendatang", "Selesai", "Terdaftar")
        ) {
            update()
        }

        watchSearch(view, "Cari nama event, kategori, atau lokasi…") {
            update()
        }

        update()
    }

    // Mengatur konfirmasi pendaftaran dan pembatalan pendaftaran event.
    private fun registerEvent(event: CivicEvent) {
        // Event selesai hanya membuka informasi arsip.
        if (event.phase == "Selesai") {
            message(
                event.title,
                "${event.description}\n\nArsip kegiatan contoh komunitas RW 04."
            )
            return
        }

        // Memeriksa status pendaftaran yang tersimpan di perangkat.
        val registered = store.registered(event.id)

        AlertDialog.Builder(this)
            .setTitle(
                if (registered) {
                    "Pendaftaran tersimpan"
                } else {
                    "Daftar ${event.title}?"
                }
            )
            .setMessage(
                "${event.date}\n${event.location}\n${event.price}\n\n" +
                        if (registered) {
                            "Kamu sudah terdaftar pada demo ini."
                        } else {
                            "Pendaftaran disimpan di perangkat ini sebagai data demo. Tidak dikirim ke penyelenggara dan tidak ada pembayaran."
                        }
            )
            .setPositiveButton(
                if (registered) {
                    "Batalkan Pendaftaran"
                } else {
                    "Konfirmasi Daftar"
                }
            ) { _, _ ->
                // !registered membalik status:
                // belum terdaftar menjadi terdaftar, dan sebaliknya.
                store.register(event.id, !registered)
                refresh()
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    // Menampilkan permintaan dan penawaran bantuan.
    private fun showHelp() {
        title("Mutual Help")

        val view = inflate(R.layout.page_help)

        val adapter = HelpAdapter(this, store) {
            connectHelp(it)
        }

        view.findViewById<ListView>(R.id.list).adapter = adapter

        fun update() {
            // Postingan lokal ditampilkan dari yang terbaru,
            // kemudian digabung dengan postingan contoh.
            val items = (
                    store.posts().reversed() + SampleData.help
                    ).filter {
                    (filter == "Semua" || it.kind == filter) &&
                            "${it.title} ${it.description} ${it.user}"
                                .contains(query, true)
                }

            adapter.submit(items)
            bindEmpty(view, items.size, "postingan")
        }

        chips(
            view,
            listOf("Semua", "Membutuhkan", "Menawarkan")
        ) {
            update()
        }

        watchSearch(view, "Cari kebutuhan atau bantuan…") {
            update()
        }

        // Membuka formulir untuk membuat postingan bantuan baru.
        view.findViewById<View>(R.id.add_help).setOnClickListener {
            createHelp()
        }

        update()
    }

    // Menyimpan atau membatalkan minat pada sebuah postingan bantuan.
    private fun connectHelp(post: HelpPost) {
        val connected = store.helped(post.id)

        AlertDialog.Builder(this)
            .setTitle(post.title)
            .setMessage(
                "${post.user}\n\n${post.description}\n\n" +
                        if (connected) {
                            "Minat bantuanmu sudah disimpan."
                        } else {
                            "Konfirmasi untuk menyimpan minat bantuan di perangkat ini. Demo ini tidak mengirim pesan kepada pengguna lain."
                        }
            )
            .setPositiveButton(
                // Label mengikuti status minat dan jenis postingan.
                if (connected) {
                    "Batalkan Minat"
                } else if (post.kind == "Menawarkan") {
                    "Konfirmasi Minta Bantuan"
                } else {
                    "Saya Siap Membantu"
                }
            ) { _, _ ->
                store.help(post.id, !connected)
                refresh()
            }
            .setNegativeButton("Tutup", null)
            .show()
    }

    // Membuat formulir postingan bantuan melalui kode.
    private fun createHelp() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)
        }

        // Kolom judul dengan kapital awal kalimat.
        val inputTitle = EditText(this).apply {
            hint = "Judul bantuan"
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            maxLines = 2
        }

        // Kolom deskripsi mendukung beberapa baris.
        val description = EditText(this).apply {
            hint = "Jelaskan kebutuhan, waktu, dan lokasi"
            minLines = 3
            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        // Pilihan jenis postingan menggunakan Spinner.
        val kind = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Membutuhkan", "Menawarkan")
            )
        }

        form.addView(inputTitle)
        form.addView(description)
        form.addView(kind)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Bantuan untuk Warga")
            .setView(form)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        // Tombol diambil setelah dialog tampil agar sudah tersedia.
        // Listener khusus mencegah dialog langsung tertutup saat input salah.
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    when {
                        // Judul harus memiliki minimal lima karakter.
                        inputTitle.text.toString().trim().length < 5 -> {
                            inputTitle.error = "Isi judul minimal 5 karakter"
                        }

                        // Deskripsi harus memiliki minimal 15 karakter.
                        description.text.toString().trim().length < 15 -> {
                            description.error =
                                "Isi deskripsi minimal 15 karakter"
                        }

                        else -> {
                            // Simpan data hanya jika seluruh validasi lolos.
                            store.addPost(
                                inputTitle.text.toString().trim(),
                                description.text.toString().trim(),
                                kind.selectedItem.toString()
                            )

                            dialog.dismiss()
                            refresh()
                        }
                    }
                }
        }

        dialog.show()
    }

    // Membuat halaman sederhana yang digunakan ulang untuk
    // Komunitas, Profil, dan Notifikasi.
    //
    // onAction: aksi tombol utama.
    // onRow: aksi ketika baris ListView diklik.
    private fun simplePage(
        heading: String,
        body: String,
        action: String,
        rows: List<String>,
        onAction: () -> Unit,
        onRow: (Int) -> Unit
    ) {
        val view = inflate(R.layout.page_simple)

        view.findViewById<TextView>(R.id.simple_heading).text = heading
        view.findViewById<TextView>(R.id.simple_body).text = body

        view.findViewById<Button>(R.id.simple_action).apply {
            text = action

            setOnClickListener {
                onAction()
            }
        }

        // Simple ListView menggunakan ArrayAdapter dan layout bawaan Android.
        view.findViewById<ListView>(R.id.simple_list).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_list_item_1,
                rows
            )

            // Hanya indeks baris yang digunakan.
            // Parameter lain ditulis _ karena tidak diperlukan.
            setOnItemClickListener { _, _, position, _ ->
                onRow(position)
            }
        }
    }

    // Menampilkan informasi dan menu komunitas.
    private fun showCommunity() {
        title("Komunitas")

        simplePage(
            "Komunitas RW 04",
            "123 anggota • Lingkungan Harapan\n\nBerbagi informasi, agenda kegiatan, dan saling membantu antarwarga.",

            // Label mengikuti status keanggotaan lokal.
            if (store.joined) {
                "Keluar Komunitas"
            } else {
                "Gabung Komunitas"
            },

            listOf(
                "Diskusi: Selamat datang, tetangga!",
                "Agenda kegiatan komunitas",
                "Bantuan antarwarga",
                "Fasilitas di sekitar kita"
            ),

            {
                // Membalik status gabung/keluar lalu memperbarui tampilan.
                store.joined = !store.joined
                refresh()
            }
        ) { i ->
            // Menentukan aksi berdasarkan posisi baris yang dipilih.
            when (i) {
                0 -> message(
                    "Diskusi Warga",
                    "Siti: Mari jaga taman tetap bersih.\nBudi: Sampai jumpa di kegiatan akhir pekan!\n\nIni contoh percakapan komunitas."
                )

                1 -> show("events")
                2 -> show("help")
                3 -> show("local")
            }
        }
    }

    // Menampilkan profil serta ringkasan aktivitas pengguna.
    private fun showProfile() {
        title("Profil")

        // Menghitung event yang statusnya terdaftar pada perangkat.
        val registered = SampleData.events.count {
            store.registered(it.id)
        }

        simplePage(
            store.name,
            "Warga RW 04\n$registered event terdaftar • ${store.posts().size} postingan bantuan\n" +
                    if (store.joined) {
                        "Anggota Komunitas RW 04"
                    } else {
                        "Belum bergabung dengan komunitas"
                    },

            "Edit Nama",

            listOf(
                "Event yang Saya Ikuti",
                "Postingan Bantuan",
                "Komunitas Saya",
                "Tentang Civic Pulse",
                "Lihat Onboarding"
            ),

            {
                editName()
            }
        ) { i ->
            when (i) {
                0 -> {
                    // Membuka event, lalu memilih filter Terdaftar.
                    show("events")
                    filter = "Terdaftar"
                    refresh()
                }

                1 -> show("help")
                2 -> show("community")

                3 -> message(
                    "Civic Pulse",
                    "Bersama, Untuk Lingkungan Kita.\n\nProyek tugas Kotlin • XML • ListView. Seluruh data merupakan demo lokal; tidak ada server, login, GPS, pembayaran, atau pesan nyata."
                )

                4 -> {
                    // Mengulang onboarding dari langkah pertama.
                    step = 0
                    show("onboarding")
                }
            }
        }
    }

    // Membuka dialog untuk mengganti nama pengguna.
    private fun editName() {
        val input = EditText(this).apply {
            setText(store.name)
            setSingleLine()
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Nama Panggilan")
            .setView(input)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    // Menghapus spasi pada awal dan akhir nama.
                    val name = input.text.toString().trim()

                    if (name.length !in 2..30) {
                        input.error = "Gunakan 2–30 karakter"
                    } else {
                        // Nama valid disimpan dan tampilan profil diperbarui.
                        store.name = name
                        dialog.dismiss()
                        refresh()
                    }
                }
        }

        dialog.show()
    }

    // Membuka halaman notifikasi melalui pusat navigasi.
    private fun showNotifications() {
        show("notifications")
    }

    // Menampilkan notifikasi contoh yang dapat membuka halaman terkait.
    private fun notificationsPage() {
        title("Notifikasi")

        simplePage(
            "Kabar Lingkungan",
            "Pemberitahuan contoh dan aktivitas yang tersimpan di perangkat.",
            "Lihat Event",
            listOf(
                "Aksi Bersih Lingkungan • 31 Oktober",
                "Komunitas RW 04 • Sapa tetanggamu",
                "Mutual Help • Ada warga yang perlu bantuan"
            ),
            {
                show("events")
            }
        ) { i ->
            // Posisi notifikasi dipetakan ke nama halaman tujuan.
            show(listOf("events", "community", "help")[i])
        }
    }

    // Memperbarui halaman aktif tanpa menambah riwayat
    // dan tanpa menghapus kata pencarian atau filter.
    private fun refresh() = show(page, false, true)

    // Fungsi bantu untuk menampilkan dialog informasi.
    private fun message(heading: String, body: String) {
        AlertDialog.Builder(this)
            .setTitle(heading)
            .setMessage(body)
            .setPositiveButton("Mengerti", null)
            .show()
    }

    // Menentukan perilaku tombol kembali.
    private fun goBack() {
        // Pada onboarding, kembali berarti mundur satu langkah dahulu.
        if (page == "onboarding" && step > 0) {
            step--
            show("onboarding", false)
            return
        }

        if (history.isNotEmpty()) {
            // Membuka halaman terakhir yang tercatat.
            show(history.removeLast(), false)
        } else if (page != "home" && page != "splash") {
            // Tanpa riwayat, kembali ke Splash dari onboarding,
            // atau ke Beranda dari halaman utama lainnya.
            show(
                if (page == "onboarding") "splash" else "home",
                false
            )
        } else {
            // Jika sudah di Beranda/Splash dan riwayat kosong, tutup Activity.
            finish()
        }
    }

    // Menangani tombol kembali melalui API Activity yang lama.
    @Deprecated("Compatibility with the platform Activity back dispatch")
    override fun onBackPressed() {
        goBack()
    }
}