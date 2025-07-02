# Hướng dẫn tạo tài khoản Admin và Owner

## Tổng quan
Script này tạo hai tài khoản cần thiết cho hệ thống:
1. **Admin Account**: Tài khoản quản trị viên với quyền ROLE_ADMIN
2. **Owner Account**: Tài khoản chủ sân đã được phê duyệt với quyền ROLE_OWNER

## Cách sử dụng

### Bước 1: Tạo Roles (nếu chưa có)
```sql
-- Chạy script tạo roles trước
sqlcmd -S your_server -d Project4 -i insert_roles.sql
```

### Bước 2: Tạo tài khoản Admin và Owner
```sql
-- Chạy script tạo tài khoản
sqlcmd -S your_server -d Project4 -i create_admin_owner_accounts.sql
```

## Thông tin tài khoản được tạo

### 🔑 Admin Account
- **Username**: `admin`
- **Email**: `admin@playerconnect.com`
- **Password**: `password` (cần đổi sau lần đăng nhập đầu tiên)
- **Full Name**: System Administrator
- **Phone**: +84901234567
- **Role**: ROLE_ADMIN
- **Status**: ACTIVE
- **Email Verified**: Yes

### 🏟️ Owner Account
- **Username**: `owner1`
- **Email**: `owner1@playerconnect.com`
- **Password**: `password` (cần đổi sau lần đăng nhập đầu tiên)
- **Full Name**: Nguyen Van Owner
- **Phone**: +84907654321
- **Role**: ROLE_OWNER
- **Status**: ACTIVE (đã được phê duyệt)
- **Email Verified**: Yes

## Quyền hạn của từng role

### ROLE_ADMIN
- Quản lý tất cả người dùng
- Phê duyệt/từ chối đăng ký owner
- Xem thống kê tổng quan hệ thống
- Quản lý cấu hình hệ thống
- Truy cập admin dashboard

### ROLE_OWNER
- Quản lý sân của mình
- Xem booking và doanh thu
- Cập nhật thông tin sân
- Truy cập owner dashboard
- Quản lý lịch đặt sân

### ROLE_USER (không tạo trong script này)
- Đặt sân
- Xem thông tin sân
- Quản lý booking của mình

## Lưu ý bảo mật

⚠️ **QUAN TRỌNG**: 
- Mật khẩu mặc định là `password` cho cả hai tài khoản
- **BẮT BUỘC** phải đổi mật khẩu sau lần đăng nhập đầu tiên
- Mật khẩu được mã hóa bằng BCrypt với strength 10
- Không sử dụng tài khoản này trong môi trường production mà không đổi mật khẩu

## Kiểm tra tài khoản đã tạo

```sql
-- Kiểm tra users và roles
SELECT 
    u.username,
    u.email,
    u.full_name,
    u.status,
    u.is_active,
    STRING_AGG(r.name, ', ') as roles
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
WHERE u.username IN ('admin', 'owner1')
GROUP BY u.id, u.username, u.email, u.full_name, u.status, u.is_active;
```

## Đăng nhập vào Frontend

### Admin Dashboard
1. Truy cập: `http://localhost:3000/en/login`
2. Đăng nhập với:
   - Username: `admin`
   - Password: `password`
3. Sẽ được redirect đến: `/en/admin/dashboard`

### Owner Dashboard
1. Truy cập: `http://localhost:3000/en/login`
2. Đăng nhập với:
   - Username: `owner1`
   - Password: `password`
3. Sẽ được redirect đến: `/en/owner/dashboard`

## Troubleshooting

### Lỗi "User already exists"
- Script sẽ bỏ qua việc tạo user nếu username hoặc email đã tồn tại
- Kiểm tra database để xác nhận

### Lỗi "Role not found"
- Chạy `insert_roles.sql` trước
- Kiểm tra bảng `roles` có đủ 3 roles: ROLE_USER, ROLE_OWNER, ROLE_ADMIN

### Không thể đăng nhập
- Kiểm tra `is_active = 1` và `status = 'ACTIVE'`
- Kiểm tra `email_verified = 1`
- Kiểm tra user có role được gán đúng không

## Tương thích với Frontend

Trong code frontend, bạn đã sử dụng:
- `user.role === 'ADMIN'` 
- `user.role === 'OWNER'`

Trong database backend:
- `ROLE_ADMIN`
- `ROLE_OWNER`

Điều này hoàn toàn tương thích vì:
1. Backend sẽ trả về role name không có prefix "ROLE_"
2. Hoặc frontend sẽ xử lý mapping từ "ROLE_ADMIN" → "ADMIN"

Ví dụ trong AuthContext.jsx:
```javascript
// Backend trả về: "ROLE_ADMIN"
// Frontend xử lý: role.replace('ROLE_', '') → "ADMIN"
const userRole = response.data.roles[0].name.replace('ROLE_', '');
```