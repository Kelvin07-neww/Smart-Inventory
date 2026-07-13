# SmartInventorySystem

SmartInventorySystem adalah aplikasi desktop JavaFX untuk manajemen inventori dan penjualan CV. SERBA ADA.

## Fitur Utama

- Login pengguna dengan dukungan migrasi password plaintext ke BCrypt.
- Dashboard ringkasan inventori dan penjualan.
- CRUD kategori, supplier, dan barang.
- Transaksi penjualan dengan pengurangan stok otomatis.
- Laporan dan ekspor PDF.
- Pengaturan profil dan tema tampilan.

## Kebutuhan

- JDK 21 atau lebih baru.
- MySQL 8 atau kompatibel.
- Maven Wrapper sudah tersedia melalui `mvnw` dan `mvnw.cmd`.

## Setup Database

1. Buat database dan tabel dari `database/schema.sql`.
2. Sesuaikan koneksi database di `src/main/resources/application.properties` untuk development, atau buat file eksternal `application.properties` di folder kerja aplikasi.

Contoh konfigurasi:

```properties
db.url=jdbc:mysql://127.0.0.1:3306/smart_inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta&connectTimeout=5000&socketTimeout=30000
db.username=root
db.password=
```

File konfigurasi eksternal juga bisa diarahkan dengan system property `smartinventory.config`.

## Menjalankan Aplikasi

```bash
./mvnw javafx:run
```

Di Windows:

```powershell
.\mvnw.cmd javafx:run
```

## Verifikasi Build

```bash
./mvnw test
```

Di Windows:

```powershell
.\mvnw.cmd test
```

## Membuat Installer Windows

Pastikan `jpackage` tersedia dari JDK, lalu jalankan:

```powershell
.\mvnw.cmd -P windows-installer package
```

Output installer berada di `target/installer/SmartInventorySetup.exe`.

## Akun Awal

Schema database menyertakan akun admin awal:

- Email: `admin@serbaada.local`
- Password: `admin123`

Ganti password setelah login pertama.
