# Pharmacy Chain Simulation

**Mô phỏng quản lý chuỗi nhà thuốc – LAB211 (FPT University)**  
Semester 3 | OOP with Java | Kiến trúc MVC

---

## 1. Mô tả đề tài

Hệ thống mô phỏng quy trình quản lý chuỗi nhà thuốc với dữ liệu lớn trên file CSV (≥14.820 dòng). Mục tiêu:
- Đảm bảo xuất thuốc đúng quy trình FEFO (First Expired First Out)
- Ngăn chặn đồng thời 3 loại race condition (inventory inconsistency, double dispensing, expired batch dispensing)
- So sánh thực nghiệm hiệu suất, độ chính xác giữa 4 cơ chế đồng bộ (NoLock, FileLock, Synchronized, Optimistic Locking)

---

## 2. Cấu trúc thư mục

```
.
├── src/
│   ├── model/
│   ├── repository/
│   ├── controller/
│   └── view/
│   ├── class/
├── util/
│   ├── DataGenerator.java
│   ├── DispenseCalculator.java
│   └── StockAllocator.java
├── data/
│   ├── branches.csv
│   ├── medicines.csv
│   ├── pharmacists.csv
│   ├── stocks.csv
│   ├── batch_lots.csv
│   ├── prescriptions.csv
│   ├── prescription_items.csv
│   └── dispense_records.csv
├── docs/
│   ├── report.docx
│   ├── slide.pptx
│   ├── class_diagram.png
│   └── flowcharts/
├── ai_logs/
│   ├── member1_ai_log.md
│   └── ...
│──test/
└── README.md
```

