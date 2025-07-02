# Complete Data Setup Guide

Script này sẽ tạo đầy đủ dữ liệu roles và users cho hệ thống PlayerConnect.
https
## Mục đích

Script `create_complete_data.sql` được thiết kế để:
1. Tạo tất cả các roles cần thiết (ROLE_USER, ROLE_OWNER, ROLE_ADMIN)
2. Tạo các user accounts với đầy đủ thông tin
3. Gán roles cho từng user một cách chính xác
4. Đảm bảo tất cả dữ liệu được insert đúng cấu trúc database

## Cách sử dụng

### 1. Kết nối SQL Server
Mở Azure Data Studio hoặc SQL Server Management Studio và kết nối đến database.

### 2. Chạy script
```sql
-- Chạy script hoàn chỉnh
EXEC sqlcmd -S your_server -d Project4 -i create_complete_data.sql
```

Hoặc copy toàn bộ nội dung file và chạy trong query editor.

### 3. Kiểm tra kết quả
Script sẽ tự động hiển thị kết quả verification ở cuối.

## Dữ liệu được tạo

### Roles
- `ROLE_USER` - Cho người dùng thông thường
- `ROLE_OWNER` - Cho chủ sân
- `ROLE_ADMIN` - Cho quản trị viên

### Users

#### 1. Admin Account
- **Username**: `Admin`
- **Email**: `Admin@gmail.com`
- **Password**: `123123a`
- **Role**: `ROLE_ADMIN`
- **Status**: `ACTIVE`
- **Email Verified**: `true`
- **Full Name**: `System Administrator`
- **Phone**: `+84901234567`
- **Address**: `Ho Chi Minh City, Vietnam`
- **Provider**: `LOCAL`

#### 2. Owner Account
- **Username**: `Owner`
- **Email**: `Owner@gmail.com`
- **Password**: `123123a`
- **Role**: `ROLE_OWNER`
- **Status**: `ACTIVE`
- **Email Verified**: `true`
- **Full Name**: `Field Owner`
- **Phone**: `+84907654321`
- **Address**: `District 12, Ho Chi Minh City, Vietnam`
- **Provider**: `LOCAL`

#### 3. Regular User Account
- **Username**: `user1`
- **Email**: `user1@gmail.com`
- **Password**: `123123a`
- **Role**: `ROLE_USER`
- **Status**: `ACTIVE`
- **Email Verified**: `true`
- **Full Name**: `Regular User`
- **Phone**: `+84909876543`
- **Address**: `Ho Chi Minh City, Vietnam`
- **Provider**: `LOCAL`

## Cấu trúc Database

Script này tương thích với cấu trúc database được định nghĩa trong các model Java:

### User Model Fields
- `id` (BIGINT, AUTO_INCREMENT)
- `username` (VARCHAR, UNIQUE, NOT NULL)
- `password` (VARCHAR, NULLABLE)
- `email` (VARCHAR, UNIQUE, NOT NULL)
- `full_name` (VARCHAR)
- `phone_number` (VARCHAR)
- `address` (VARCHAR)
- `profile_picture` (VARCHAR)
- `image_url` (VARCHAR)
- `provider` (ENUM: LOCAL, GOOGLE, FACEBOOK)
- `provider_id` (VARCHAR)
- `is_active` (BOOLEAN, DEFAULT TRUE)
- `email_verified` (BOOLEAN, DEFAULT FALSE)
- `verification_token` (VARCHAR)
- `verification_token_expiry` (DATETIME)
- `reset_password_token` (VARCHAR)
- `reset_password_token_expiry` (DATETIME)
- `status` (ENUM: ACTIVE, PENDING_APPROVAL, REJECTED, INACTIVE, SUSPENDED)

### Role Model Fields
- `id` (INT, AUTO_INCREMENT)
- `name` (ENUM: ROLE_USER, ROLE_OWNER, ROLE_ADMIN)

### User_Roles Junction Table
- `user_id` (BIGINT, FK to users.id)
- `role_id` (INT, FK to roles.id)

## Bảo mật

### Password Hashing
- Tất cả passwords được hash bằng BCrypt với strength 10
- Hash mẫu: `$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi`
- Password gốc: `123123a`

### Lưu ý bảo mật
1. **Đổi password ngay sau lần đăng nhập đầu tiên**
2. Tất cả accounts đều có `email_verified = true` để test
3. Không có verification tokens được set
4. Provider được set là `LOCAL` cho tất cả accounts

## Đăng nhập Frontend

### Admin Dashboard
- URL: `http://localhost:3000/admin/dashboard`
- Login: `Admin@gmail.com` / `123123a`

### Owner Dashboard
- URL: `http://localhost:3000/owner/dashboard`
- Login: `Owner@gmail.com` / `123123a`

### User Interface
- URL: `http://localhost:3000`
- Login: `user1@gmail.com` / `123123a`

## Troubleshooting

### Lỗi thường gặp

1. **"Database 'Project4' does not exist"**
   - Tạo database trước: `CREATE DATABASE Project4;`

2. **"Table 'roles' doesn't exist"**
   - Chạy migration scripts trước để tạo tables

3. **"Duplicate entry for key 'username'"**
   - Users đã tồn tại, script sẽ skip và thông báo

4. **"Role assignment failed"**
   - Kiểm tra xem roles và users đã được tạo thành công chưa

### Xóa dữ liệu cũ (nếu cần)

Nếu muốn bắt đầu lại từ đầu, uncomment các dòng sau trong script:
```sql
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;
```

## Verification

Sau khi chạy script, kiểm tra:

1. **Roles được tạo**:
```sql
SELECT * FROM roles;
```

2. **Users được tạo**:
```sql
SELECT id, username, email, status, is_active FROM users;
```

3. **Role assignments**:
```sql
SELECT u.username, r.name as role 
FROM users u 
JOIN user_roles ur ON u.id = ur.user_id 
JOIN roles r ON ur.role_id = r.id;
```

## Tích hợp với Backend

Script này tương thích 100% với:
- Spring Boot Security configuration
- JPA Entity models
- Authentication và Authorization logic
- Role-based access control

Tất cả users có thể đăng nhập ngay lập tức mà không cần thêm setup gì.