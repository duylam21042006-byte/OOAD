# 📅 Calendar Appointment System - Đồ án OOAD

Hệ thống quản lý Lịch Trình được thiết kế tuân thủ **100% Sơ đồ Lớp và Tuần tự**, tích hợp các thuật toán thông minh và UI/UX chuẩn mực.

---

## 🎬 KỊCH BẢN QUAY VIDEO DEMO (Ngắn gọn & Ăn điểm)

Người quay video hãy làm theo đúng **5 bước** sau để phô diễn toàn bộ sự "thông minh" của hệ thống cho cô giáo xem:

### Bước 1: Trải nghiệm UI/UX mượt mà
- **Thao tác:** Chạy chương trình. Rê chuột qua lại giữa các ô lịch trong tháng và các nút bấm. Bấm nút `+ Add Appointment`.
- **Thuyết minh:** Giao diện có hiệu ứng Hover xịn xò, tương tác cao ở từng ô lịch và nút bấm. "Điểm tinh tế là hệ thống tự động bắt giờ mặc định là giờ hiện hành của máy tính (được làm tròn), giúp user đỡ phải chỉnh tay nhiều."

### Bước 2: Validation (Chặn nhập bậy)
- **Thao tác:** Trong bảng tạo mới, cố tình để trống *Title*, hoặc chỉnh *Start Time* (10:00) lớn hơn *End Time* (09:00) rồi bấm **Save**.
- **Thuyết minh:** Hệ thống bắt lỗi lập tức và hiện Popup (đúng khối `alt [Thông tin không hợp lệ]` trên sơ đồ), ngăn không cho rác lọt vào Database.

### Bước 3: Tạo lịch thành công (Happy Path)
- **Thao tác:** Điền đúng Title (*Học OOAD*), chọn giờ (VD: 09:00 - 11:00). Tick chọn `Enable Reminder`. Bấm **Save**.
- **Thuyết minh:** Cuộc hẹn lập tức xuất hiện tuyệt đẹp trên danh sách bên phải. Dữ liệu đã được Serialize và lưu sâu vào file `appointments.dat` cũng như add vào list `reminders`.

### Bước 4: Thuật toán Gợi ý Giờ (Smart Time Suggestion)
- **Thao tác:** Tạo thêm 1 lịch mới đè đúng vào khung giờ của lịch ở Bước 3. Popup "Time Conflict" hiện ra. Bấm nút **Available Time**.
- **Thuyết minh:** "Điểm ăn tiền nhất đây cô: Thuật toán tự động quét tìm khung giờ trống gần nhất trong ngày và **tự động nảy số nhảy giờ mới** vào ô Spinner cho user. Thuật toán này đã được nhóm chặn lỗi lặp vô hạn qua đêm (Midnight Wrap-around)." 
- Bấm tiếp **Replace**, lịch cũ sẽ bị xóa và đè lịch mới vào an toàn.

### Bước 5: Thuật toán Gom Nhóm (Smart Group Meeting)
- **Thao tác:** Tạo lịch mới tên `Họp Nhóm`, thời lượng 2 tiếng, tick vào `Group Meeting`. Sau đó, tạo thêm 1 lịch nữa **y hệt tên và thời lượng (2 tiếng) vào cùng ngày hôm đó**.
- **Thuyết minh:** Hệ thống nhận diện ra ngay và hỏi *"Bạn có muốn Join không?"*. Bấm **YES**, hệ thống không tạo thêm lịch mới gây rác mà tự động gắn user vào danh sách `participants` của lịch cũ. "Thuật toán này cực kỳ chặt chẽ vì nó check đúng Tên, đúng Thời lượng, và **bắt buộc phải cùng Ngày** mới cho join".

---

## 🛠 KIẾN TRÚC & CLEAN CODE (Dành cho báo cáo)
1. **Tuân thủ OOAD:** Tách biệt Model (`CalendarModel`), View (`CalendarApp`), Database (`DatabaseUtil`).
2. **Thuật toán chặt chẽ:** Hàm `overlapsWith()` bắt giao thoa thời gian hoàn hảo. Thuật toán gợi ý giờ chặn được logic vượt biên qua đêm (23:59).
3. **Mã nguồn sạch (Clean Code & Technical Debt):** Lớp `Appointment` đã được Override đầy đủ `equals()` và `hashCode()` (so sánh qua Title + Date + StartTime). Việc này giúp hàm xóa `remove()` và logic truy xuất chạy mượt mà kể cả khi Load dữ liệu object mới từ Database lên, không gặp lỗi tham chiếu bộ nhớ.