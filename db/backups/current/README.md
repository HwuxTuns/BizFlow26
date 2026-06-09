# BizFlow Current Data Backup

Bo backup nay duoc tao tu du lieu thuc te dang chay trong Docker, de push len Git va cho nguoi moi clone co du du lieu.

## Noi dung backup

- `databases/bizflow_auth_db.sql`
- `databases/bizflow_catalog_db.sql`
- `databases/bizflow_customer_db.sql`
- `databases/bizflow_db.sql`
- `databases/bizflow_inventory_db.sql`
- `databases/bizflow_promotion_db.sql`
- `databases/bizflow_report_db.sql`
- `databases/bizflow_sales_db.sql`
- `databases/bizflow_all_databases.sql` (gom tat ca DB tren)
- `product-images/img_sanpham/*` (anh san pham)
- `product-images/data/product-image-files.json` (index anh)

## Restore nhanh tren Windows (PowerShell)

Chay tu thu muc goc project:

```powershell
.\db\backups\current\restore-current-backup.ps1
```

## Restore nhanh tren Linux/Mac

```bash
bash ./db/backups/current/restore-current-backup.sh
```

## Ghi chu quan trong

- Anh san pham trong he thong nay dang luu tren filesystem (khong nam trong MySQL blob).
- Script restore se copy anh vao:
  - `BizFlow.Frontend/assets/img/img_sanpham`
  - `BizFlow.Frontend/assets/data/product-image-files.json`
- Script restore KHONG xoa Docker volume, KHONG dung `docker compose down -v`.
