/**
 * BizFlow - Return Request Page Logic
 * Handles the 3-step return/exchange request flow
 */

// ============ API Configuration ============
const API = (() => {
    const configured = window.API_BASE_URL || window.API_BASE;
    if (configured) return configured.replace(/\/$/, '');
    if (window.location.protocol === 'file:') return 'http://localhost:8000/api';
    if (['localhost', '127.0.0.1'].includes(window.location.hostname)) {
        if (window.location.port === '3000') return 'http://localhost:8000/api';
    }
    return `${window.location.origin}/api`;
})();

// ============ State ============
let selectedOrder = null;
let selectedProducts = new Map(); // productItemId -> { checked, qty }
let uploadedImages = []; // base64 strings

// ============ Utilities ============
function formatPrice(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency', currency: 'VND', minimumFractionDigits: 0
    }).format(amount || 0).replace('₫', 'đ');
}

function formatDate(value) {
    if (!value) return '—';
    const d = new Date(value);
    if (isNaN(d.getTime())) return '—';
    return d.toLocaleDateString('vi-VN');
}

function showToast(message, type = 'success') {
    const existing = document.querySelector('.rr-toast');
    if (existing) existing.remove();
    const toast = document.createElement('div');
    toast.className = `rr-toast ${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}

function getAuthHeaders() {
    const token = sessionStorage.getItem('accessToken');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
}

const REASON_LABELS = {
    'DAMAGED': 'Sản phẩm hư hỏng',
    'WRONG_PRODUCT': 'Sai sản phẩm',
    'NOT_SATISFIED': 'Không ưng ý',
    'DEFECTIVE': 'Lỗi kỹ thuật',
    'EXPIRED': 'Hết hạn sử dụng',
    'OTHER': 'Lý do khác'
};

// ============ Step Navigation ============
function goToStep(step) {
    document.getElementById('step1').classList.toggle('hidden', step !== 1);
    document.getElementById('step2').classList.toggle('hidden', step !== 2);
    document.getElementById('step3').classList.toggle('hidden', step !== 3);

    if (step === 1) {
        document.getElementById('myRequestsSection').classList.remove('hidden');
    } else if (step === 3) {
        document.getElementById('myRequestsSection').classList.add('hidden');
    }

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// ============ Step 1: Search Orders ============
async function searchOrders() {
    const keyword = document.getElementById('searchOrderInput').value.trim();
    if (!keyword) {
        showToast('Vui lòng nhập số hoá đơn hoặc SĐT', 'error');
        return;
    }

    const container = document.getElementById('orderResults');
    container.innerHTML = `<div class="rr-loading"><div class="rr-spinner"></div>Đang tìm kiếm...</div>`;

    try {
        const params = new URLSearchParams({ keyword });
        const res = await fetch(`${API}/orders/returns/search?${params}`, {
            headers: getAuthHeaders()
        });

        if (!res.ok) throw new Error('Không thể tìm đơn hàng');

        const orders = await res.json();

        if (!orders || orders.length === 0) {
            container.innerHTML = `
                <div class="rr-empty-state">
                    <svg viewBox="0 0 64 64" width="48" height="48" opacity="0.4">
                        <circle cx="32" cy="32" r="28" stroke="currentColor" stroke-width="2" fill="none"/>
                        <path d="M22 22l20 20M42 22L22 42" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/>
                    </svg>
                    <p>Không tìm thấy đơn hàng phù hợp</p>
                </div>`;
            return;
        }

        container.innerHTML = orders.map(order => `
            <div class="rr-order-card" onclick="selectOrder(${order.id})" data-order-id="${order.id}">
                <div class="rr-order-card-info">
                    <div class="rr-order-card-code">${order.invoiceNumber || '#' + order.id}</div>
                    <div class="rr-order-card-meta">
                        <span>📅 ${formatDate(order.createdAt)}</span>
                        <span>👤 ${order.customerName || 'Khách lẻ'}</span>
                        <span>📞 ${order.customerPhone || '—'}</span>
                    </div>
                </div>
                <div class="rr-order-card-amount">${formatPrice(order.totalAmount)}</div>
                <div class="rr-order-card-arrow">
                    <svg viewBox="0 0 24 24" width="20" height="20"><path d="M9 6l6 6-6 6" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round"/></svg>
                </div>
            </div>
        `).join('');
    } catch (err) {
        console.error(err);
        container.innerHTML = `<div class="rr-empty-state"><p style="color:#f87171;">Lỗi: ${err.message}</p></div>`;
    }
}

// ============ Step 2: Select order and show products ============
async function selectOrder(orderId) {
    try {
        const res = await fetch(`${API}/orders/${orderId}`, {
            headers: getAuthHeaders()
        });
        if (!res.ok) throw new Error('Không thể tải chi tiết đơn hàng');

        selectedOrder = await res.json();
        selectedProducts.clear();

        // Show selected order info
        document.getElementById('selectedOrderInfo').innerHTML = `
            <div>
                <div class="rr-selected-order-label">Đơn hàng đã chọn</div>
                <div class="rr-selected-order-value">${selectedOrder.invoiceNumber || '#' + selectedOrder.id} — ${formatPrice(selectedOrder.totalAmount)}</div>
            </div>
        `;

        // Render product list
        const productList = document.getElementById('productList');
        const items = selectedOrder.items || [];

        if (items.length === 0) {
            productList.innerHTML = '<div class="rr-empty-state"><p>Đơn hàng không có sản phẩm</p></div>';
        } else {
            productList.innerHTML = items.map((item, idx) => `
                <div class="rr-product-item" data-idx="${idx}" onclick="toggleProduct(${idx}, this)">
                    <input type="checkbox" class="product-check" data-idx="${idx}" onclick="event.stopPropagation()">
                    <div>
                        <div class="rr-product-name">${item.productName || 'Sản phẩm #' + item.productId}</div>
                        <div class="rr-product-qty-info">Đã mua: ${item.quantity} ${item.unit || ''}</div>
                    </div>
                    <input type="number" class="rr-product-qty-input" min="1" max="${item.quantity}" value="${item.quantity}" disabled data-idx="${idx}" onclick="event.stopPropagation()">
                    <div class="rr-product-price">${formatPrice(item.price)}</div>
                </div>
            `).join('');
        }

        // Bind checkbox events
        productList.querySelectorAll('.product-check').forEach(cb => {
            cb.addEventListener('change', (e) => {
                const idx = parseInt(e.target.dataset.idx);
                const row = e.target.closest('.rr-product-item');
                const qtyInput = row.querySelector('.rr-product-qty-input');
                if (e.target.checked) {
                    row.classList.add('selected');
                    qtyInput.disabled = false;
                    selectedProducts.set(idx, { checked: true, qty: parseInt(qtyInput.value) });
                } else {
                    row.classList.remove('selected');
                    qtyInput.disabled = true;
                    selectedProducts.delete(idx);
                }
            });
        });

        goToStep(2);
    } catch (err) {
        console.error(err);
        showToast('Lỗi tải chi tiết đơn hàng: ' + err.message, 'error');
    }
}

function toggleProduct(idx, el) {
    const checkbox = el.querySelector('.product-check');
    checkbox.checked = !checkbox.checked;
    checkbox.dispatchEvent(new Event('change'));
}

// ============ Image Upload ============
function setupImageUpload() {
    const zone = document.getElementById('uploadZone');
    const input = document.getElementById('imageUpload');
    const placeholder = document.getElementById('uploadPlaceholder');

    zone.addEventListener('click', (e) => {
        if (e.target.closest('.rr-image-remove')) return;
        input.click();
    });

    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        zone.classList.add('dragover');
    });
    zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('dragover');
        handleFiles(e.dataTransfer.files);
    });

    input.addEventListener('change', () => {
        handleFiles(input.files);
        input.value = '';
    });
}

function handleFiles(files) {
    if (!files || files.length === 0) return;

    for (const file of files) {
        if (uploadedImages.length >= 5) {
            showToast('Tối đa 5 ảnh', 'error');
            break;
        }
        if (file.size > 2 * 1024 * 1024) {
            showToast(`Ảnh "${file.name}" vượt quá 2MB`, 'error');
            continue;
        }
        if (!file.type.startsWith('image/')) continue;

        const reader = new FileReader();
        reader.onload = (e) => {
            uploadedImages.push(e.target.result);
            renderImagePreviews();
        };
        reader.readAsDataURL(file);
    }
}

function renderImagePreviews() {
    const container = document.getElementById('imagePreviews');
    container.innerHTML = uploadedImages.map((src, idx) => `
        <div class="rr-image-preview">
            <img src="${src}" alt="Ảnh ${idx + 1}">
            <button class="rr-image-remove" onclick="removeImage(${idx}, event)" title="Xoá">×</button>
        </div>
    `).join('');

    const placeholder = document.getElementById('uploadPlaceholder');
    if (uploadedImages.length > 0) {
        placeholder.innerHTML = `<p>Đã chọn ${uploadedImages.length}/5 ảnh. <span class="rr-upload-link">Thêm ảnh</span></p>`;
    } else {
        placeholder.innerHTML = `
            <svg viewBox="0 0 48 48" width="40" height="40"><rect x="6" y="10" width="36" height="28" rx="4" stroke="currentColor" stroke-width="2" fill="none" opacity="0.4"/><circle cx="18" cy="22" r="4" stroke="currentColor" stroke-width="2" fill="none" opacity="0.5"/><path d="M6 34l10-10 8 8 6-6 12 12" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round" opacity="0.4"/></svg>
            <p>Kéo thả hoặc <span class="rr-upload-link">chọn ảnh</span></p>
            <p class="rr-upload-hint">Tối đa 5 ảnh, mỗi ảnh ≤ 2MB</p>
        `;
    }
}

function removeImage(idx, event) {
    event.stopPropagation();
    uploadedImages.splice(idx, 1);
    renderImagePreviews();
}

// ============ Submit Return Request ============
async function submitReturnRequest() {
    if (!selectedOrder) {
        showToast('Vui lòng chọn đơn hàng', 'error');
        return;
    }

    const reason = document.getElementById('returnReason').value;
    if (!reason) {
        showToast('Vui lòng chọn lý do đổi/trả', 'error');
        return;
    }

    const requestType = document.querySelector('input[name="requestType"]:checked')?.value || 'REFUND';
    const reasonDetail = document.getElementById('reasonDetail').value.trim();

    // Collect selected products
    const items = selectedOrder.items || [];
    const selectedEntries = [];
    selectedProducts.forEach((val, idx) => {
        if (val.checked && items[idx]) {
            const qtyInput = document.querySelector(`.rr-product-qty-input[data-idx="${idx}"]`);
            const qty = parseInt(qtyInput?.value || items[idx].quantity);
            selectedEntries.push({
                item: items[idx],
                qty: Math.min(qty, items[idx].quantity)
            });
        }
    });

    if (selectedEntries.length === 0) {
        showToast('Vui lòng chọn ít nhất 1 sản phẩm', 'error');
        return;
    }

    const btn = document.getElementById('submitRequestBtn');
    btn.disabled = true;
    btn.innerHTML = '<div class="rr-spinner" style="width:16px;height:16px;border-width:2px;"></div> Đang gửi...';

    try {
        // Send one request per selected product
        const results = [];
        for (const entry of selectedEntries) {
            const payload = {
                orderId: selectedOrder.id,
                invoiceNumber: selectedOrder.invoiceNumber,
                customerId: selectedOrder.customerId,
                customerName: selectedOrder.customerName || null,
                customerPhone: selectedOrder.customerPhone || null,
                productId: entry.item.productId,
                productName: entry.item.productName,
                quantity: entry.qty,
                reason: reason,
                reasonDetail: reasonDetail,
                requestType: requestType,
                evidenceImages: uploadedImages.length > 0 ? uploadedImages : null,
                createdBy: sessionStorage.getItem('userId') ? parseInt(sessionStorage.getItem('userId')) : null
            };

            const res = await fetch(`${API}/orders/return-requests`, {
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
            results.push(data);
        }

        // Show success
        const detail = document.getElementById('successDetail');
        detail.innerHTML = `
            <span class="label">Đơn hàng</span><span class="value">${selectedOrder.invoiceNumber || '#' + selectedOrder.id}</span>
            <span class="label">Số sản phẩm</span><span class="value">${selectedEntries.length}</span>
            <span class="label">Hình thức</span><span class="value">${requestType === 'EXCHANGE' ? '🔄 Đổi sản phẩm' : '💰 Hoàn tiền'}</span>
            <span class="label">Lý do</span><span class="value">${REASON_LABELS[reason] || reason}</span>
            <span class="label">Trạng thái</span><span class="value" style="color:#fbbf24;">⏳ Chờ xử lý</span>
        `;

        goToStep(3);
        showToast('Gửi yêu cầu thành công!', 'success');

    } catch (err) {
        console.error(err);
        showToast('Lỗi: ' + err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = `
            <svg viewBox="0 0 24 24" width="18" height="18"><path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
            Gửi yêu cầu đổi/trả
        `;
    }
}

// ============ Load My Requests ============
async function loadMyRequests() {
    const container = document.getElementById('myRequestsList');
    try {
        const userId = sessionStorage.getItem('userId');
        const url = userId ? `${API}/orders/return-requests/by-employee/${userId}` : `${API}/orders/return-requests`;
        const res = await fetch(url, {
            headers: getAuthHeaders()
        });

        if (!res.ok) {
            container.innerHTML = '<div class="rr-empty-state"><p>Không thể tải danh sách yêu cầu</p></div>';
            return;
        }

        const requests = await res.json();
        if (!requests || requests.length === 0) {
            container.innerHTML = '<div class="rr-empty-state"><p>Chưa có yêu cầu đổi/trả nào</p></div>';
            return;
        }

        container.innerHTML = requests.slice(0, 20).map(req => {
            const statusClass = (req.status || '').toLowerCase();
            const statusLabel = {
                'PENDING': '⏳ Chờ xử lý',
                'APPROVED': '✅ Đã duyệt',
                'REJECTED': '❌ Từ chối',
                'COMPLETED': '🎉 Hoàn tất'
            }[req.status] || req.status;

            return `
                <div class="rr-request-card">
                    <div class="rr-request-card-header">
                        <span class="rr-request-card-id">#${req.id} — ${req.invoiceNumber || '—'}</span>
                        <span class="rr-status-badge ${statusClass}">${statusLabel}</span>
                    </div>
                    <div class="rr-request-card-body">
                        <span class="label">Sản phẩm</span><span class="value">${req.productName || '—'}</span>
                        <span class="label">Số lượng</span><span class="value">${req.quantity}</span>
                        <span class="label">Lý do</span><span class="value">${REASON_LABELS[req.reason] || req.reason || '—'}</span>
                        <span class="label">Hình thức</span><span class="value">${req.requestType === 'EXCHANGE' ? '🔄 Đổi SP' : '💰 Hoàn tiền'}</span>
                        <span class="label">Ngày gửi</span><span class="value">${formatDate(req.createdAt)}</span>
                        ${req.adminNote ? `<span class="label">Ghi chú admin</span><span class="value">${req.adminNote}</span>` : ''}
                    </div>
                </div>
            `;
        }).join('');

    } catch (err) {
        console.error(err);
        container.innerHTML = '<div class="rr-empty-state"><p>Lỗi kết nối</p></div>';
    }
}

// ============ Reset Form ============
function resetForm() {
    selectedOrder = null;
    selectedProducts.clear();
    uploadedImages = [];
    document.getElementById('searchOrderInput').value = '';
    document.getElementById('returnReason').value = '';
    document.getElementById('reasonDetail').value = '';
    document.getElementById('orderResults').innerHTML = `
        <div class="rr-empty-state">
            <svg viewBox="0 0 64 64" width="48" height="48" opacity="0.4"><rect x="8" y="12" width="48" height="40" rx="4" stroke="currentColor" stroke-width="2" fill="none"/><path d="M8 24h48" stroke="currentColor" stroke-width="2" fill="none"/></svg>
            <p>Nhập thông tin để tìm đơn hàng của bạn</p>
        </div>`;
    renderImagePreviews();
    goToStep(1);
    loadMyRequests();
}

// ============ Init ============
document.addEventListener('DOMContentLoaded', () => {
    // User info
    const username = sessionStorage.getItem('username');
    const avatar = document.getElementById('userAvatar');
    const nameEl = document.getElementById('userName');
    if (avatar) avatar.textContent = (username ? username[0] : 'K').toUpperCase();
    if (nameEl) nameEl.textContent = username || 'Khách hàng';

    // Bind events
    document.getElementById('searchOrderBtn').addEventListener('click', searchOrders);
    document.getElementById('searchOrderInput').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') { e.preventDefault(); searchOrders(); }
    });
    document.getElementById('submitRequestBtn').addEventListener('click', submitReturnRequest);

    // Image upload
    setupImageUpload();

    // Load existing requests
    loadMyRequests();
});
