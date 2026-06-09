$file = "d:\1.9.1\BizFlow\BizFlow.Frontend\pages\owner-promotions.html"
$content = [IO.File]::ReadAllText($file, [Text.Encoding]::Default)

# Replace common Vietnamese encoding errors
$replacements = @{
    'M� khuy\?n m�i' = 'Mã khuyến mãi'
    'Gi\?m %' = 'Giảm %'
    'Gi\?m ti\?n' = 'Giảm tiền'
    'Theo danh m\?c' = 'Theo danh mục'
    'Theo s\?n ph\?m' = 'Theo sản phẩm'
    'Ng�y b\?t d\?u' = 'Ngày bắt đầu'
    'Ng�y k\?t th�c' = 'Ngày kết thúc'
    'M� t\?' = 'Mô tả'
    '\?i tu\?ng �p d\?ng' = 'Đối tượng áp dụng'
    'Th�m s\?n ph\?m ho\?c danh m\?c �p d\?ng' = 'Thêm sản phẩm hoặc danh mục áp dụng'
    'Th�m d\?i tu\?ng' = 'Thêm đối tượng'
    'Ch\?n s\?n ph\?m mua v� s\?n ph\?m t\?ng, k�m m\?u combo' = 'Chọn sản phẩm mua và sản phẩm tặng, kèm mẫu combo'
    'Luu khuy\?n m�i' = 'Lưu khuyến mãi'
    'H\?y' = 'Hủy'
    'C\?p nh\?t khuy\?n m�i' = 'Cập nhật khuyến mãi'
    'T\?ng quan' = 'Tổng quan'
    'B�o c�o doanh thu' = 'Báo cáo doanh thu'
    'Danh m\?c' = 'Danh mục'
    'Qu\?n l� kho' = 'Quản lý kho'
    'Nh\?p h�ng & Gi� v\?n' = 'Nhập hàng & Giá vốn'
    'Qu\?n l� nh�n vi�n' = 'Quản lý nhân viên'
    'Thi\?t l\?p' = 'Thiết lập'
    'T\?i l\?i' = 'Tải lại'
    'M�' = 'Mã'
    'Nu?c Gi\?i Kh�t' = 'Nước Giải Khát'
    '�? An V\?t' = 'Đồ Ăn Vặt'
    'H�a M\? Ph\?m' = 'Hóa Mỹ Phẩm'
    'Gia V\? & Nu?c Ch\?m' = 'Gia Vị & Nước Chấm'
    'S\?n Ph\?m Cham S�c Nh� C\?a' = 'Sản Phẩm Chăm Sóc Nhà Cửa'
    'B�nh K\?o' = 'Bánh Kẹo'
    'Bia & Ru\?u' = 'Bia & Rượu'
    'M�, Ph?, Ch�o G�i' = 'Mì, Phở, Cháo Gói'
    '�? H\?p & Th\?c Ph\?m ��ng H\?p' = 'Đồ Hộp & Thực Phẩm Đóng Hộp'
    'Thu\?c L� & Di\?m' = 'Thuốc Lá & Diêm'
    'Kh�ng th\? t\?i' = 'Không thể tải'
    'Kh�ng th\? t�m' = 'Không thể tìm'
    'Kh�ng c� s\?n ph\?m' = 'Không có sản phẩm'
    'ch\?n' = 'chọn'
    'Mua 1 t\?ng 1' = 'Mua 1 tặng 1'
    'Mua 2 t\?ng 1' = 'Mua 2 tặng 1'
    'Mua 3 t\?ng 1' = 'Mua 3 tặng 1'
    'AI t\? d\?ng t\?o t�n & code' = 'AI tự động tạo tên & code'
    'Kh�ng c� khuy\?n m�i ph� h\?p' = 'Không có khuyến mãi phù hợp'
    'Ch\?n % --' = 'Chọn % --'
    '\? Kh�ng th\? k\?t n\?i AI Service\. Vui l�ng ki\?m tra service dang ch\?y\.' = '⚠ Không thể kết nối AI Service. Vui lòng kiểm tra service đang chạy.'
    'Kh�ng t\?i du\?c d\? li\?u' = 'Không tải được dữ liệu'
    'Kh�ng th\? t\?i danh s�ch' = 'Không thể tải danh sách'
    'Lo\?i gi\?m' = 'Loại giảm'
    'Gi� tr\? gi\?m' = 'Giá trị giảm'
    '\?\?' = '📊'
    '\?\?\?' = '🔎'
}

foreach ($key in $replacements.Keys) {
    $content = $content -replace $key, $replacements[$key]
}

[IO.File]::WriteAllText($file, $content, [Text.Encoding]::UTF8)
Write-Host "Fixed encoding in owner-promotions.html"
