# Hasil Pengujian Civic Pulse

Tanggal: 24 Oktober 2026 (WIB).

## Build akhir

Perintah Gradle yang dijalankan: `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`.

**BUILD SUCCESSFUL**, 51 task diproses (24 dijalankan, 27 sudah mutakhir).

Lingkungan build: OpenJDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Kotlin 2.0.21, compile/target SDK 35, minimum API 26.

APK debug disertakan di `releases/CivicPulse-debug.apk`. `apksigner verify --verbose` berhasil, dengan tanda tangan APK v2 yang valid. APK ini untuk pengujian, bukan paket release untuk Play Store.

## Tes otomatis — 8 lulus, 0 gagal, 0 dilewati

Tes memakai Robolectric pada API 28; ini pengujian Activity, layout Android, listener, adapter, dan penyimpanan lokal di JVM.

| Tes | Hasil |
| --- | --- |
| Splash → tiga langkah onboarding → Beranda | Lulus |
| Lewati dan seluruh tujuan Bottom Navigation; jenis Simple/Custom Adapter | Lulus |
| Pencarian lokasi memperbarui ListView dan marker peta; hasil kosong | Lulus |
| Konfirmasi pendaftaran event tersimpan; filter Terdaftar dan Selesai | Lulus |
| Filter penawaran bantuan dan konfirmasi permintaan tersimpan | Lulus |
| Formulir bantuan menolak input kosong lalu menyimpan postingan valid | Lulus |
| Pembuatan ulang Activity mempertahankan halaman dan pencarian | Lulus |
| Gabung komunitas, edit nama profil, sapaan di Beranda | Lulus |

Bukti hasil runner: `qa/unit-test-results.xml`.

## Android Lint

**0 error, 61 warning.** Warning belum seluruhnya dibereskan; laporan lengkap ada di `qa/lint-results.txt`.

Kategori warning: HardcodedText (28), SetTextI18n (12), UnusedResources (7), IconLocation (5), Autofill (4), serta masing-masing satu untuk DataExtractionRules, DrawAllocation, Overdraw, IconLauncherShape, dan RtlSymmetry. Sebagian besar menyangkut lokalisasi teks, penempatan ikon, resource cadangan, dan penyempurnaan aplikasi di luar demo ini.

Kesalahan kompatibilitas tema API 26 dan orientasi layout dinamis yang ditemukan oleh lint sudah diperbaiki sebelum build akhir. Semua file PNG resource juga telah dibaca dan diverifikasi valid.

## Pemeriksaan langsung pada emulator

APK dipasang dan dijalankan pada emulator Android 9 / API 28. Splash, onboarding, Beranda, Jelajah Lokal, Event, dan Mutual Help berhasil dibuka. Keenam tampilan diperiksa secara visual; tangkapan layar ada di `qa/screenshots/`.

Pendaftaran event dikonfirmasi sampai tombol menampilkan **Terdaftar ✓**. Filter penawaran bantuan dibuka, tombol **Minta Bantuan** dikonfirmasi, dan status **Tersambung ✓** berhasil terlihat.

Menu Komunitas, tombol Gabung/Keluar Komunitas, menu Profil, dan kembali ke Beranda juga berhasil diperiksa langsung. Pemeriksaan log AndroidRuntime tidak menemukan FATAL EXCEPTION. Hasil akhir: **SMOKE TEST PASSED**, tersimpan di `qa/emulator-smoke-test.txt` dan `qa/emulator-smoke-log.txt`.

Emulator berjalan tanpa akselerasi hardware dan sempat menampilkan dialog sistem **System UI isn't responding**. Gangguan sistem emulator ditangani, lalu pemeriksaan diteruskan. Otomasi memakai penantian tambahan karena tampilan emulator terlambat memperbarui layar.

## Batas pengujian

Belum diuji pada HP fisik, semua ukuran layar, maupun perangkat Android 15. Tes JVM memakai API 28. Pendaftaran, bantuan, profil dan komunitas adalah data demo yang disimpan lokal; tidak ada pengujian server, pembayaran atau pengiriman pesan karena fitur tersebut memang tidak diimplementasikan.
