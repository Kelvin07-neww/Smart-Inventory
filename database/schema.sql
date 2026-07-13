CREATE DATABASE IF NOT EXISTS smart_inventory_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE smart_inventory_db;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nama VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS kategori (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_kategori VARCHAR(20) NOT NULL,
  nama_kategori VARCHAR(100) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_kategori_kode UNIQUE (kode_kategori)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS supplier (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_supplier VARCHAR(20) NOT NULL,
  nama_supplier VARCHAR(150) NOT NULL,
  alamat TEXT,
  telepon VARCHAR(30),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_supplier_kode UNIQUE (kode_supplier)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS barang (
  id INT AUTO_INCREMENT PRIMARY KEY,
  kode_barang VARCHAR(20) NOT NULL,
  nama_barang VARCHAR(150) NOT NULL,
  kategori_id INT NOT NULL,
  supplier_id INT NOT NULL,
  harga_beli DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  harga_jual DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  stok INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uk_barang_kode UNIQUE (kode_barang),
  CONSTRAINT fk_barang_kategori FOREIGN KEY (kategori_id) REFERENCES kategori (id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_barang_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_barang_harga_beli CHECK (harga_beli >= 0),
  CONSTRAINT chk_barang_harga_jual CHECK (harga_jual >= 0),
  CONSTRAINT chk_barang_stok CHECK (stok >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS penjualan (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nomor_faktur VARCHAR(40) NOT NULL,
  tanggal DATETIME NOT NULL,
  user_id INT NOT NULL,
  subtotal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  diskon DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  ppn DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  grand_total DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_penjualan_nomor_faktur UNIQUE (nomor_faktur),
  CONSTRAINT fk_penjualan_user FOREIGN KEY (user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_penjualan_subtotal CHECK (subtotal >= 0),
  CONSTRAINT chk_penjualan_diskon CHECK (diskon >= 0),
  CONSTRAINT chk_penjualan_ppn CHECK (ppn >= 0),
  CONSTRAINT chk_penjualan_grand_total CHECK (grand_total >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detail_penjualan (
  id INT AUTO_INCREMENT PRIMARY KEY,
  penjualan_id INT NOT NULL,
  barang_id INT NOT NULL,
  harga DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  qty INT NOT NULL,
  subtotal DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  CONSTRAINT fk_detail_penjualan_penjualan FOREIGN KEY (penjualan_id) REFERENCES penjualan (id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_detail_penjualan_barang FOREIGN KEY (barang_id) REFERENCES barang (id) ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_detail_penjualan_harga CHECK (harga >= 0),
  CONSTRAINT chk_detail_penjualan_qty CHECK (qty > 0),
  CONSTRAINT chk_detail_penjualan_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB;

INSERT INTO users (nama, email, password, role)
VALUES ('Administrator', 'admin@serbaada.local', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE email = VALUES(email);
