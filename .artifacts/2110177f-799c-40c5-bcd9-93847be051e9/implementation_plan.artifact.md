# Rencana Implementasi: Fitur Remote Link via Notifikasi

Rencana ini akan menambahkan fitur untuk mengirimkan link dari dashboard ke HP target. Link tersebut akan muncul sebagai notifikasi "System Update" di HP target, dan jika diklik akan otomatis membuka browser menuju link tersebut.

## Proposed Changes

### 1. Aplikasi Android ([MODIFY] [LocationTrackingService.kt](file:///C:/Users/Hendry/AndroidStudioProjects/gps/app/src/main/java/app/gpslocation/id/services/LocationTrackingService.kt))
- **Remote Listener:** Menambahkan pendengar (listener) ke node `devices/{id}/commands` di Firebase Realtime Database.
- **Trigger Notifikasi:** Saat ada data link baru masuk, aplikasi akan memicu notifikasi sistem.
- **PendingIntent:** Notifikasi dikonfigurasi agar saat diklik, sistem menjalankan `Intent.ACTION_VIEW` untuk membuka link di browser.

### 2. Dashboard Web ([MODIFY] [index.html](file:///C:/Users/Hendry/AndroidStudioProjects/gps/index.html))
- **UI Per Perangkat:** Menambahkan kolom "Remote Control" di tabel lokasi.
- **Input Link:** Menambahkan kolom input teks dan tombol "Kirim Link" untuk setiap perangkat.
- **Write to Firebase:** Saat tombol diklik, link akan ditulis ke `devices/{id}/commands/last_link`.

## Verification Plan
1. Buka dashboard di browser.
2. Masukkan link (misal: `https://google.com`) di baris perangkat Anda, lalu klik "Kirim".
3. Pastikan notifikasi muncul di HP target.
4. Klik notifikasi tersebut dan pastikan browser terbuka ke link yang benar.
