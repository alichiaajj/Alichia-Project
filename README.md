# Civic Pulse — Proyek Android Kotlin

Aplikasi tugas mobile programming dengan enam halaman utama berdasarkan papan UI Civic Pulse yang disertakan di `design/reference.png`.

## Membuka proyek

1. Ekstrak ZIP, lalu di Android Studio pilih **Open** dan buka folder **CivicPulse**, bukan folder `app`.
2. Gunakan **Gradle JDK 17** pada Settings → Build, Execution, Deployment → Build Tools → Gradle.
3. Tunggu Gradle Sync. Jika diminta, instal **Android SDK Platform 35** dan SDK Build Tools. Koneksi internet diperlukan untuk unduhan pertama.
4. Jalankan konfigurasi **app** pada emulator atau perangkat **Android 8.0 / API 26** ke atas.

`local.properties` sengaja tidak dimasukkan agar lokasi SDK mengikuti komputer kamu. Android Studio akan membuatnya otomatis. Gradle Wrapper lengkap disertakan; tidak perlu menginstal Gradle secara terpisah.

## Halaman dan interaksi

| Halaman | Interaksi |
| --- | --- |
| Splash | Logo, slogan, ilustrasi, tombol Mulai ke onboarding |
| Onboarding | Tiga langkah; Lanjut, Mulai Bersama, Lewati; tombol kembali perangkat mundur satu langkah |
| Beranda | Sapaan sesuai nama profil, pencarian event, empat pintasan, banner komunitas, event terdekat |
| Jelajah Lokal | Pencarian nama/alamat/kategori, filter fasilitas, daftar lokasi, pin peta yang dapat diketuk, detail lokasi |
| Event | Filter Terdekat/Mendatang/Selesai/Terdaftar, pencarian, konfirmasi dan pembatalan pendaftaran |
| Mutual Help | Filter permintaan/penawaran, pencarian, konfirmasi minat, formulir membuat postingan dengan validasi |

Menu bawah **Beranda, Komunitas, Event, Mutual Help, Profil** seluruhnya berfungsi. Jelajah Lokal dibuka dari pintasan Beranda. Komunitas, Profil, dan Notifikasi adalah halaman pendukung agar menu tidak menjadi pajangan.

Profil mendukung perubahan nama dan melihat event terdaftar. Komunitas mendukung gabung/keluar dan tautan ke kegiatan, fasilitas, serta bantuan. Ikon lonceng membuka notifikasi contoh yang mengarah ke halaman terkait.

## ListView yang diwajibkan

- `MainActivity.simplePage()`: `ListView` + `ArrayAdapter` + **`android.R.layout.simple_list_item_1`**, dipakai untuk Komunitas, Profil, dan Notifikasi.
- `EventAdapter`, `HelpAdapter`, `PlaceAdapter`: **Custom Adapter** berbasis `BaseAdapter`, ViewHolder, serta `row_card.xml`.
- `submit()` memperbarui daftar ketika pencarian/filter berubah. Tampilan kosong ditangani.

## Struktur

- `MainActivity.kt`: navigasi halaman, onboarding, bottom navigation, pencarian, filter, dialog dan formulir.
- `Models.kt`: model, data contoh, penyimpanan lokal SharedPreferences.
- `CivicAdapters.kt`: custom adapter event, bantuan, dan lokasi.
- `NeighborhoodMapView.kt`: peta ilustratif offline yang digambar dengan Canvas.
- `res/layout/`: activity utama, enam layout halaman, kartu daftar, dan halaman pendukung.
- `res/drawable/`: logo dari papan referensi, logo tambahan asli unggahan, ikon vektor, latar, dan ilustrasi.
- `res/font/`: Poppins SemiBold untuk judul dan Inter untuk teks; lisensi OFL disertakan di `design/`.
- `app/src/test/`: tes alur UI lokal dengan Robolectric.

## Penyimpanan dan batas implementasi

Pendaftaran event, minat bantuan, postingan baru, nama profil dan status anggota disimpan di perangkat menggunakan SharedPreferences. Status halaman, kata kunci, filter, dan langkah onboarding dipertahankan saat Activity dibuat ulang.

Ini aplikasi demo **offline**, bukan layanan komunitas dengan server. Tidak ada akun/login, pengiriman pesan ke warga, transaksi, pendaftaran ke penyelenggara sungguhan atau koordinat GPS. Peta, jarak, alamat, pengguna dan kegiatan adalah data contoh. Penjelasan tersebut tampil pada tindakan yang relevan agar pengguna mengetahui apa yang disimpan.

Data tetap ada setelah aplikasi ditutup, sampai data aplikasi dihapus atau aplikasi diuninstal.

## Build dan tes

Windows:

```bat
gradlew.bat assembleDebug testDebugUnitTest
```

macOS/Linux:

```sh
./gradlew assembleDebug testDebugUnitTest
```

APK yang sudah dibuat juga disertakan di `releases/CivicPulse-debug.apk`.

APK hasil build: `app/build/outputs/apk/debug/app-debug.apk`.
Hasil pemeriksaan yang benar-benar dijalankan tercatat di **HASIL_PENGUJIAN.md**.

## Aset visual

Logo utama diambil dari papan desain supaya identitas tanda Civic Pulse tetap sama. `supplied_logo.png` menyimpan logo terpisah unggahan pengguna secara utuh sebagai aset alternatif. Ilustrasi hero dibuat menggunakan imagegen berdasarkan gaya referensi, bukan tangkapan layar UI. Prompt dan asal aset dicatat di `design/ASSETS.md`.
