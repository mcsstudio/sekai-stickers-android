# Project Sekai Stickers (Android)

Một bản viết lại bằng Android Jetpack Compose của công cụ tạo **Sekai Stickers** (`mcsstudio/sekai-stickers`), giữ nguyên toàn bộ bộ sưu tập gồm 359 mẫu sticker nhân vật Project Sekai, tính năng tùy chỉnh văn bản, tự động đổi màu giao diện theo nhân vật và quy trình xuất ảnh.

## Tính năng

- **359 mẫu sticker nhân vật**: Duyệt và tìm kiếm theo tên nhân vật, ID hoặc tiêu đề sticker trong một lưới chọn thích ứng.
- **Canvas Sticker Trực tiếp**: Xem trước sticker theo thời gian thực với hệ tọa độ logic `296×256`, viền trắng bo góc sắc nét, văn bản nhiều dòng và chế độ chữ uốn cong.
- **Điều khiển Vị trí & Kiểu chữ Tương tác**:
  - Kéo trực tiếp văn bản trên canvas hoặc tinh chỉnh bằng thanh trượt ngang (`0–296`) và dọc (`0–256`).
  - Điều chỉnh độ xoay (`-10` đến `+10`), cỡ chữ (`10–100px`), khoảng cách dòng (`18–100px`) và bật/tắt chế độ chữ uốn cong.
  - Đặt lại về văn bản và vị trí mặc định của từng nhân vật bất kỳ lúc nào.
- **Đổi màu Giao diện theo Nhân vật**: Trích xuất màu chủ đạo của từng nhân vật bằng thuật toán `sqrt` và tạo ra một bảng màu nền tối phù hợp.
- **Xuất & Chia sẻ**:
  - **Sao chép (Copy)**: Sao chép sticker PNG `592×512` có nền trong suốt vào clipboard của Android thông qua `FileProvider`.
  - **Tải xuống (Download)**: Lưu trực tiếp file PNG vào thư mục `Pictures/SekaiStickers` thông qua `MediaStore` mà không cần xin quyền truy cập bộ nhớ rộng rãi.
  - **Chia sẻ (Share)**: Mở bảng chia sẻ của Android với sticker PNG đã được render.

## Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **Giao diện**: Jetpack Compose + Material 3 (Bố cục thích ứng cho điện thoại và máy tính bảng)
- **Tải ảnh**: Coil Compose
- **Tuần tự hóa**: `kotlinx.serialization`
- **Hệ thống Build**: Gradle Kotlin DSL (AGP 9.1.1, Android SDK 36, minSdk 26)

## Ghi nhận đóng góp

- Ý tưởng gốc & ứng dụng web: [TheOriginalAyaka](https://github.com/theoriginalayaka) & [Modder4869](https://github.com/modder4869)
- Tác phẩm sticker: [u/SherenPlaysGames](https://www.reddit.com/r/ProjectSekai/comments/x1h4v1/after_an_ungodly_amount_of_time_i_finally_made/)
- Việt hóa & mã nguồn: [mcsstudio/sekai-stickers](https://github.com/mcsstudio/sekai-stickers)

## Giấy phép

Dự án được phân phối dưới giấy phép MIT. Xem chi tiết tại file [`LICENSE`](./LICENSE).
