# Hướng dẫn Setup ZEGOCLOUD cho Video Call

## Bước 1: Đăng ký tài khoản ZEGOCLOUD
1. Truy cập [ZEGOCLOUD Console](https://console.zegocloud.com/)
2. Đăng ký tài khoản mới
3. Tạo một project mới

## Bước 2: AppID và AppSign đã được cấu hình
AppID và AppSign đã được thêm vào `VideoCallScreen.kt`:
```kotlin
val appID: Long = 1847715171
val appSign = "faf0680b96dad072ccf66018ca764e519dbc986706bcd03a278d77d0990863df"
```

## Bước 3: Cấu hình đã hoàn thành
- ✅ Repository đã được thêm vào `settings.gradle`
- ✅ Dependency đã được thêm vào `app/build.gradle.kts` (ZEGOCLOUD SDK + UI Kit)
- ✅ Permissions đã được thêm vào `AndroidManifest.xml`
- ✅ VideoCallScreen đã được tạo với ZEGOCLOUD SDK
- ✅ Navigation đã được cấu hình
- ✅ AppID và AppSign đã được cấu hình
- ✅ Icons đã được tạo cho video call controls

## Bước 4: Test Video Call
1. Build và chạy ứng dụng
2. Đăng nhập với 2 tài khoản khác nhau
3. Vào chat với người dùng khác
4. Nhấn nút video call (icon camera)
5. Chấp nhận permissions camera và microphone
6. Cuộc gọi sẽ được thiết lập

## Tính năng đã implement:
- ✅ Video call 2 chiều
- ✅ Mute/unmute microphone
- ✅ Bật/tắt camera
- ✅ Chuyển đổi camera trước/sau
- ✅ Kết thúc cuộc gọi
- ✅ Hiển thị trạng thái kết nối
- ✅ Tự động cập nhật trạng thái trong Firebase

## Lưu ý:
- Đảm bảo có kết nối internet ổn định
- Test trên thiết bị thật (không phải emulator)
- Cần có camera và microphone trên thiết bị

## Troubleshooting:
- Nếu gặp lỗi "AppID invalid", kiểm tra lại AppID và AppSign
- Nếu không có video, kiểm tra permissions camera
- Nếu không có âm thanh, kiểm tra permissions microphone
- Nếu gặp lỗi với ZegoRoomState, sử dụng giá trị số:
  - DISCONNECTED = 0
  - CONNECTING = 1
  - CONNECTED = 2
  - RECONNECTING = 3

## Test SDK:
Sử dụng `ZegoTest.testZegoSDK(context)` để test SDK
Sử dụng `ZegoTest.testRoomStates()` để xem các giá trị room state 