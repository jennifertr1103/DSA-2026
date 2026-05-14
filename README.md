# DSA 2026

## Hướng dẫn clone repository

Lấy link GitHub:
```
https://github.com/jennifertr1103/DSA-2026/tree/master
```

Di chuyển đến thư mục làm việc:
```cmd
cd path/to/your/directory
```

Khởi tạo và kéo code về:
```cmd
git init
git remote add origin https://github.com/jennifertr1103/DSA-2026/tree/master
git pull origin master
```

## Hướng dẫn push code lên nhánh riêng

### 1. Tạo và chuyển sang nhánh mới
```cmd
git checkout -b <tên-nhánh>
```
*Hiển thị: `Switched to a new branch '<tên-nhánh>'` là thành công*

### 2. Thêm các thay đổi
```cmd
git add .
```

### 3. Commit với mô tả
```cmd
git commit -m "Mô tả thay đổi của bạn"
```

### 4. Push lên nhánh của bạn
```cmd
git push origin <tên-nhánh>
```

⚠️ **LƯU Ý:** Tuyệt đối **KHÔNG** dùng `git push origin main`


