// ============ WARRANTY REQUEST JS ============
// Handles the warranty request flow for customers/employees

const API = window.API_BASE || 'http://localhost:8000/api';

let selectedOrder = null;
let uploadedImages = [];

// ============ INIT ============
document.addEventListener('DOMContentLoaded', () => {
    // Set user info
    const name = sessionStorage.getItem('username') || sessionStorage.getItem('fullName') || 'Nhân viên';
    const avatar = document.getElementById('userAvatar');
    const userName = document.getElementById('userName');
    if (avatar) avatar.textContent = name.charAt(0).toUpperCase();
    if (userName) userName.textContent = name;

    // Bind search
    document.getElementById('searchOrderBtn')?.addEventListener('click', searchOrders);
    document.getElementById('searchOrderInput')?.addEventListener('keydown', e => {
        if (e.key === 'Enter') searchOrders();
    });

    // Bind submit
    document.getElementById('submitRequestBtn')?.addEventListener('click', submitWarrantyRequest);

    // Bind image upload
    const uploadZone = document.getElementById('uploadZone');
    const imageUpload = document.getElementById('imageUpload');
    if (uploadZone && imageUpload) {
        uploadZone.addEventListener('click', () => imageUpload.click());
        uploadZone.addEventListener('dragover', e => { e.preventDefault(); uploadZone.classList.add('dragover'); });
        uploadZone.addEventListener('dragleave', () => uploadZone.classList.remove('dragover'));
        uploadZone.addEventListener('drop', e => {
            e.preventDefault();
            uploadZone.classList.remove('dragover');
            handleFiles(e.dataTransfer.files);
        });
        imageUpload.addEventListener('change', e => handleFiles(e.target.files));
    }

    // Load my requests
    loadMyRequests();
});

// ============ Auth Headers ============
function getAuthHeaders() {
    const token = sessionStorage.getItem('token') || sessionStorage.getItem('accessToken');
    return token ? { 'Authorization': 'Bearer ' + token } : {};
}

// ============ Toast ============
function showToast(msg, type = 'info') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999;max-width:400px;';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    const bg = type === 'error' ? '#ef4444' : type === 'success' ? '#10b981' : '#3b82f6';
    toast.style.cssText = `background:${bg};color:#fff;padding:12px 20px;border-radius:10px;margin-bottom:8px;font-size:14px;box-shadow:0 4px 12px rgba(0,0,0,0.15);animation:slideIn 0.3s ease;`;
    toast.textContent = msg;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; setTimeout(() => toast.remove(), 300); }, 4000);
}

// ============ Step Navigation ============
function goToStep(step) {
    document.querySelectorAll('.rr-step').forEach(s => {
        if (s.id === 'myRequestsSection') return;
        s.classList.add('hidden');
    });
    const target = document.getElementById('step' + step);
    if (target) target.classList.remove('hidden');
}

// ============ SEARCH ORDERS ============
async function searchOrders() {
    const query = document.getElementById('searchOrderInput')?.value?.trim();
    if (!query) {
        showToast('Vui lòng nhập số hoá đơn hoặc SĐT', 'error');
        return;
    }

    const resultsDiv = document.getElementById('orderResults');
    resultsDiv.innerHTML = '<div class="rr-empty-state"><div class="rr-spinner"></div><p>Đang tìm kiếm...</p></div>';

    try {
        const res = await fetch(`${API}/orders/search?keyword=${encodeURIComponent(query)}`, {
            headers: getAuthHeaders()
        });

        if (!res.ok) throw new Error('Không thể tìm kiếm');

        const orders = await res.json();
        const list = Array.isArray(orders) ? orders : (orders?.content || []);

        if (list.length === 0) {
            resultsDiv.innerHTML = '<div class="rr-empty-state"><p>Không tìm thấy đơn hàng nào</p></div>';
            return;
        }

        // Only show paid/received/completed orders
        const validOrders = list.filter(o => {
            const s = (o.status || '').toUpperCase();
            return s === 'PAID' || s === 'RECEIVED' || s === 'COMPLETED';
        });

        if (validOrders.length === 0) {
            resultsDiv.innerHTML = '<div class="rr-empty-state"><p>Không có đơn hàng nào đủ điều kiện bảo hành</p></div>';
            return;
        }

        resultsDiv.innerHTML = validOrders.map(o => `
            <div class="rr-order-card" onclick="selectOrder(${o.id})">
                <div class="rr-order-card-header">
                    <span class="rr-order-id">${o.invoiceNumber || '#' + o.id}</span>
                    <span class="rr-status-badge ${(o.status||'').toLowerCase()}">${formatStatus(o.status)}</span>
                </div>
                <div class="rr-order-card-body">
                    <span class="label">Khách hàng</span><span class="value">${o.customerName || 'Khách lẻ'}</span>
                    <span class="label">SĐT</span><span class="value">${o.customerPhone || '-'}</span>
                    <span class="label">Tổng tiền</span><span class="value">${formatMoney(o.totalAmount)}đ</span>
                    <span class="label">Ngày mua</span><span class="value">${formatDate(o.createdAt)}</span>
                </div>
            </div>
        `).join('');

    } catch (err) {
        console.error(err);
        resultsDiv.innerHTML = '<div class="rr-empty-state"><p>Lỗi khi tìm kiếm: ' + err.message + '</p></div>';
    }
}

// ============ SELECT ORDER ============
async function selectOrder(orderId) {
    try {
        const res = await fetch(`${API}/orders/${orderId}`, { headers: getAuthHeaders() });
        if (!res.ok) throw new Error('Không tìm thấy đơn hàng');
        const order = await res.json();
        selectedOrder = order;

        // Show order info
        const info = document.getElementById('selectedOrderInfo');
        info.innerHTML = `
            <div class="rr-order-card selected">
                <div class="rr-order-card-header">
                    <span class="rr-order-id">${order.invoiceNumber || '#' + order.id}</span>
                    <span class="rr-status-badge ${(order.status||'').toLowerCase()}">${formatStatus(order.status)}</span>
                </div>
                <div class="rr-order-card-body">
                    <span class="label">Khách hàng</span><span class="value">${order.customerName || 'Khách lẻ'}</span>
                    <span class="label">Ngày mua</span><span class="value">${formatDate(order.createdAt)}</span>
                </div>
            </div>
        `;

        // Show products (only pick 1 for warranty)
        const items = order.items || [];
        const productList = document.getElementById('productList');
        productList.innerHTML = items.map((item, idx) => `
            <label class="rr-product-item">
                <input type="radio" name="warrantyProduct" value="${idx}" ${idx === 0 ? 'checked' : ''}>
                <div class="rr-product-card">
                    <div class="rr-product-info">
                        <span class="rr-product-name">${item.productName || 'Sản phẩm #' + item.productId}</span>
                        <span class="rr-product-meta">SL: ${item.quantity} · ${formatMoney(item.unitPrice)}đ</span>
                    </div>
                </div>
            </label>
        `).join('');

        goToStep(2);
    } catch (err) {
        showToast('Lỗi: ' + err.message, 'error');
    }
}

// ============ IMAGE UPLOAD ============
function handleFiles(files) {
    const maxFiles = 5;
    const maxSize = 2 * 1024 * 1024;

    for (const file of files) {
        if (uploadedImages.length >= maxFiles) {
            showToast('Tối đa ' + maxFiles + ' ảnh', 'error');
            break;
        }
        if (file.size > maxSize) {
            showToast('Ảnh "' + file.name + '" quá lớn (max 2MB)', 'error');
            continue;
        }
        if (!file.type.startsWith('image/')) continue;

        const reader = new FileReader();
        reader.onload = (e) => {
            uploadedImages.push(e.target.result);
            renderPreviews();
        };
        reader.readAsDataURL(file);
    }
}

function renderPreviews() {
    const container = document.getElementById('imagePreviews');
    const placeholder = document.getElementById('uploadPlaceholder');
    container.innerHTML = uploadedImages.map((src, i) => `
        <div class="rr-image-preview">
            <img src="${src}" alt="Ảnh ${i + 1}">
            <button type="button" class="rr-image-remove" onclick="event.stopPropagation();removeImage(${i})">×</button>
        </div>
    `).join('');
    if (placeholder) placeholder.style.display = uploadedImages.length >= 5 ? 'none' : '';
}

function removeImage(idx) {
    uploadedImages.splice(idx, 1);
    renderPreviews();
}

// ============ SUBMIT WARRANTY REQUEST ============
async function submitWarrantyRequest() {
    if (!selectedOrder) {
        showToast('Vui lòng chọn đơn hàng trước', 'error');
        return;
    }

    const description = document.getElementById('warrantyDescription')?.value?.trim();
    if (!description) {
        showToast('Vui lòng mô tả lỗi của sản phẩm', 'error');
        return;
    }

    // Get selected product
    const items = selectedOrder.items || [];
    const selectedRadio = document.querySelector('input[name="warrantyProduct"]:checked');
    if (!selectedRadio) {
        showToast('Vui lòng chọn sản phẩm cần bảo hành', 'error');
        return;
    }
    const selectedItem = items[parseInt(selectedRadio.value)];

    const btn = document.getElementById('submitRequestBtn');
    btn.disabled = true;
    btn.innerHTML = '<div class="rr-spinner" style="width:16px;height:16px;border-width:2px;"></div> Đang gửi...';

    try {
        const payload = {
            orderId: selectedOrder.id,
            invoiceNumber: selectedOrder.invoiceNumber,
            customerId: selectedOrder.customerId,
            customerName: selectedOrder.customerName || null,
            customerPhone: selectedOrder.customerPhone || null,
            productId: selectedItem.productId,
            productName: selectedItem.productName,
            description: description,
            evidenceImages: uploadedImages.length > 0 ? uploadedImages : null,
            createdBy: sessionStorage.getItem('userId') ? parseInt(sessionStorage.getItem('userId')) : null
        };

        const res = await fetch(`${API}/orders/warranty-requests`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...getAuthHeaders()
            },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => null);
            throw new Error(errData?.message || 'Không thể gửi yêu cầu');
        }

        const data = await res.json();

        // Show success
        const detail = document.getElementById('successDetail');
        detail.innerHTML = `
            <span class="label">Đơn hàng</span><span class="value">${selectedOrder.invoiceNumber || '#' + selectedOrder.id}</span>
            <span class="label">Sản phẩm</span><span class="value">${selectedItem.productName || '#' + selectedItem.productId}</span>
            <span class="label">Trạng thái</span><span class="value" style="color:#f59e0b;">⏳ Chờ xử lý</span>
        `;

        goToStep(3);
        showToast('Gửi yêu cầu bảo hành thành công!', 'success');
        loadMyRequests();

    } catch (err) {
        console.error(err);
        showToast('Lỗi: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `
            <svg viewBox="0 0 24 24" width="18" height="18"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Gửi yêu cầu bảo hành
        `;
    }
}

// ============ LOAD MY REQUESTS ============
async function loadMyRequests() {
    const container = document.getElementById('myRequestsList');
    try {
        const userId = sessionStorage.getItem('userId');
        const url = userId ? `${API}/orders/warranty-requests/by-employee/${userId}` : `${API}/orders/warranty-requests`;
        const res = await fetch(url, { headers: getAuthHeaders() });

        if (!res.ok) {
            container.innerHTML = '<div class="rr-empty-state"><p>Không thể tải danh sách yêu cầu</p></div>';
            return;
        }

        const requests = await res.json();
        if (!requests || requests.length === 0) {
            container.innerHTML = '<div class="rr-empty-state"><p>Chưa có yêu cầu bảo hành nào</p></div>';
            return;
        }

        container.innerHTML = requests.map(r => `
            <div class="rr-request-card">
                <div class="rr-request-card-header">
                    <span class="rr-order-id">${r.invoiceNumber || '#' + r.orderId}</span>
                    <span class="rr-status-badge ${(r.status||'').toLowerCase()}">${formatStatusWarranty(r.status)}</span>
                </div>
                <div class="rr-request-card-body">
                    <span class="label">Sản phẩm</span><span class="value">${r.productName || '#' + r.productId}</span>
                    <span class="label">Mô tả lỗi</span><span class="value">${r.description || '-'}</span>
                    <span class="label">Ngày gửi</span><span class="value">${formatDate(r.createdAt)}</span>
                    ${r.adminNote ? `<span class="label">Ghi chú Admin</span><span class="value">${r.adminNote}</span>` : ''}
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error(err);
        container.innerHTML = '<div class="rr-empty-state"><p>Lỗi tải danh sách</p></div>';
    }
}

// ============ RESET ============
function resetForm() {
    selectedOrder = null;
    uploadedImages = [];
    document.getElementById('warrantyDescription').value = '';
    const previews = document.getElementById('imagePreviews');
    if (previews) previews.innerHTML = '';
    const placeholder = document.getElementById('uploadPlaceholder');
    if (placeholder) placeholder.style.display = '';
    goToStep(1);
}

// ============ FORMATTERS ============
function formatMoney(val) {
    return (Number(val) || 0).toLocaleString('vi-VN');
}

function formatDate(dt) {
    if (!dt) return '-';
    const d = new Date(dt);
    return d.toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function formatStatus(s) {
    const map = { PENDING: 'Chờ xử lý', PAID: 'Đã thanh toán', RECEIVED: 'Đã nhận hàng', COMPLETED: 'Hoàn tất', CANCELLED: 'Đã hủy' };
    return map[(s||'').toUpperCase()] || s || '-';
}

function formatStatusWarranty(s) {
    const map = {
        PENDING: '⏳ Chờ xử lý',
        APPROVED: '✅ Đã duyệt',
        REJECTED: '❌ Từ chối',
        REPAIRING: '🔧 Đang sửa',
        COMPLETED: '✅ Hoàn tất'
    };
    return map[(s||'').toUpperCase()] || s || '-';
}
