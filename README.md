# escape-from-oop

## Hướng dẫn clone repository

Lấy link GitHub:
```
https://github.com/billy2204/escape-from-oop.git
```

Di chuyển đến thư mục làm việc:
```cmd
cd path/to/your/directory
```

Khởi tạo và kéo code về:
```cmd
git init
git remote add origin https://github.com/billy2204/escape-from-oop.git
git pull origin main
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

## Cấu trúc thư mục

- `lab5/` - Bài tập Lab 5 (8 câu hỏi)
  - `q1/` - JOptionPane Input Dialog
  - `q2/` - Draw Four Lines
  - `q3/` - Fan Lines Design
  - `q4/` - Line Art with Loops
  - `q5/` - Drawing Spirals
  - `q6/` - Concentric Circles
  - `q7/` - Random Shapes
  - `q8/` - Bar Chart Program

Mỗi thư mục chứa:
- File chính (ví dụ: `InputDialog.java`)
- File test (`Test.java`)
