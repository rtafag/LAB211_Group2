# Pharmacy Chain Simulation

Mô phỏng quản lý chuỗi nhà thuốc - LAB211 (FPT University)  
Semester 3 | OOP with Java | Kiến trúc MVC

---

## 1. Mô tả đề tài

Hệ thống mô phỏng quy trình quản lý chuỗi nhà thuốc với dữ liệu lớn trên file CSV. Mục tiêu:
- Đảm bảo xuất thuốc đúng quy trình FEFO (First Expired First Out)
- Hỗ trợ quản lý tồn kho, đơn thuốc, lô thuốc, nhân sự
- Sử dụng kiến trúc MVC và lưu trữ dữ liệu bằng CSV

---

## 2. Yêu cầu môi trường

- JDK 25
- Maven 3.9+
- Windows PowerShell / CMD

Kiểm tra nhanh:

```bash
java -version
mvn -version
```

---

## 3. Compile và chạy ứng dụng

Từ thư mục gốc dự án:

```bash
mvn clean compile
```

Chạy ứng dụng:

```bash
mvn exec:java -Dexec.mainClass=Main
```

Đóng gói file jar:

```bash
mvn clean package
java -jar target/LAB211-app.jar
```

---

## 4. Chạy DataGenerator

File DataGenerator đang nằm ở thư mục gốc `util/`, không nằm trong `src/`, vì vậy cần compile riêng:

```bash
javac -d target/classes util/DataGenerator.java
java -cp target/classes util.DataGenerator
```

Kết quả: các file CSV dữ liệu sẽ được tạo/cập nhật trong thư mục `data/`.

---

## 5. Chạy test

```bash
mvn test
```

---

## 6. Cấu trúc thư mục (hiện tại)

```text
.
|- pom.xml
|- README.md
|- all_sources.txt
|- compile.txt
|- sources.txt
|- ai_logs/
|- data/
|  |- batch_lots.csv
|  |- branches.csv
|  |- dispense_records.csv
|  |- medicines.csv
|  |- pharmacists.csv
|  |- prescription_items.csv
|  |- prescriptions.csv
|  |- stocks.csv
|- docs/
|- src/
|  |- Main.java
|  |- controller/
|  |- model/
|  |- repository/
|  |- test/
|  |- util/
|  |- view/
|- test/
|  |- MedicineTests.java
|  |- ModelTests.java
|  |- RepositoryTests.java
|- util/
|  |- DataGenerator.java
|- target/
```

