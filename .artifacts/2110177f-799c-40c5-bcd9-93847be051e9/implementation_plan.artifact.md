# Rencana Implementasi: Perbaikan Notifikasi Duplikat & Sorting Dashboard

Rencana ini bertujuan untuk mengatasi masalah notifikasi yang muncul ganda di dashboard dan memperbaiki urutan tampilan agar selalu yang terbaru berada di atas secara global.

## Proposed Changes

### 1. Aplikasi Android ([MODIFY] [NotificationMonitorService.kt](file:///C:/Users/Hendry/AndroidStudioProjects/gps/app/src/main/java/app/gpslocation/id/services/NotificationMonitorService.kt))
- **Deduplikasi:** Menambahkan sistem pengecekan notifikasi terakhir. Jika judul dan isi pesan sama persis dengan yang dikirim sebelumnya dalam waktu singkat, maka tidak akan dikirim ulang ke Firebase.
- Ini akan menyaring notifikasi dari WhatsApp yang sering mengirimkan update ganda (Summary & Message).

### 2. Dashboard Web ([MODIFY] [index.html](file:///C:/Users/Hendry/AndroidStudioProjects/gps/dashboard/index.html))
- **Global Sorting:** Mengubah logika pengambilan data. Semua notifikasi dari semua perangkat akan dikumpulkan dulu, kemudian diurutkan berdasarkan waktu secara keseluruhan.
- **Limit 10 Global:** Dashboard hanya akan menampilkan 10 notifikasi terbaru dari seluruh HP yang terdeteksi (bukan 10 per HP).
- **Pembersihan Baris:** Memastikan baris dihapus dengan benar saat data di Firebase dibersihkan.

## Verification Plan
1. Jalankan aplikasi dan kirim pesan tes berkali-kali.
2. Pastikan di dashboard hanya muncul satu baris untuk satu pesan yang sama.
3. Coba kirim pesan dari dua perangkat berbeda, pastikan urutannya benar berdasarkan waktu masuk.
