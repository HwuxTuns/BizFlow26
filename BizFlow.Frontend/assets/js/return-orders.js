const API_BASE = resolveApiBase();
let returnOrders = [];
let activeOrderDetail = null;
let selectedReturnItems = new Map();
let rangeState = { start: null, end: null, view: new Date() };

function resolveApiBase() {
    const configured = window.API_BASE_URL || window.API_BASE;
    if (configured) {
        return configured.replace(/\/$/, '');
    }

    if (window.location.protocol === 'file:') {
        return 'http://localhost:8000/api';
    }

    // When developing locally: if served from port 3000 (frontend dev server)
    // call the gateway directly. Otherwise keep using '/api' so a dev proxy
    // (if configured) continues to work.
    if (['localhost', '127.0.0.1'].includes(window.location.hostname)) {
        if (window.location.port === '3000') {
            return 'http://localhost:8000/api';
        }
        if (window.location.port !== '8080') {
            return '/api';
        }
    }

    return `${window.location.origin}/api`;
}

function checkAuth() {
    const token = sessionStorage.getItem('accessToken');
    const role = sessionStorage.getItem('role');
    if (!token || role !== 'EMPLOYEE') {
        window.location.href = '/pages/login.html';
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(price || 0).replace('₫', 'đ');
}

function formatDateTime(value) {
    const date = value ? new Date(value) : null;
    if (!date || Number.isNaN(date.getTime())) return '-';
    return date.toLocaleDateString('vi-VN');
}

async function searchPaidOrders() {
    const keyword = document.getElementById('returnSearchKeyword')?.value.trim() || '';
    const fromDate = document.getElementById('returnSearchFrom')?.value || '';
    const toDate = document.getElementById('returnSearchTo')?.value || '';
    if (!keyword) {
        renderOrderList([]);
        alert('Vui lòng nhập SĐT hoặc số hóa đơn để tìm.');
        return;
    }

    const params = new URLSearchParams();
    if (keyword) params.append('keyword', keyword);
    if (fromDate) params.append('fromDate', fromDate);
    if (toDate) params.append('toDate', toDate);

    try {
        const res = await fetch(`${API_BASE}/orders/returns/search?${params.toString()}`, {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            }
        });
        if (!res.ok) {
            if (res.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            alert('Không thể tải danh sách hóa đơn.');
            return;
        }
        returnOrders = await res.json();
        renderOrderList(returnOrders || []);
    } catch (err) {
        console.error(err);
        alert('Lỗi kết nối khi tải dữ liệu.');
    }
}

function getPresetRange(preset) {
    const now = new Date();
    const start = new Date(now);
    const end = new Date(now);
    switch (preset) {
        case 'today':
            break;
        case 'yesterday':
            start.setDate(start.getDate() - 1);
            end.setDate(end.getDate() - 1);
            break;
        case 'thisWeek': {
            const day = start.getDay() || 7;
            start.setDate(start.getDate() - (day - 1));
            break;
        }
        case 'thisMonth':
            start.setDate(1);
            break;
        default:
            return null;
    }
    return {
        from: start.toISOString().slice(0, 10),
        to: end.toISOString().slice(0, 10)
    };
}

function applyPresetRange() {
    const preset = document.getElementById('returnDatePreset')?.value || 'today';
    const fromInput = document.getElementById('returnSearchFrom');
    const toInput = document.getElementById('returnSearchTo');
    const inline = document.getElementById('returnRangeInline');
    if (!fromInput || !toInput) return;

    if (preset === 'all') {
        fromInput.value = '';
        toInput.value = '';
        if (inline) inline.textContent = 'Toàn bộ';
        closeRangeModal();
        return;
    }

    if (preset === 'custom') {
        openRangeModal();
        return;
    }

    const range = getPresetRange(preset);
    if (range) {
        fromInput.value = range.from;
        toInput.value = range.to;
    }
    if (inline) {
        if (preset === 'today' || preset === 'yesterday') {
            inline.textContent = formatDateLabel(fromInput.value);
        } else {
            inline.textContent = `${formatDateLabel(fromInput.value)} → ${formatDateLabel(toInput.value)}`;
        }
    }
    closeRangeModal();
}

function openRangeModal() {
    const panel = document.getElementById('returnRangePanel');
    if (!panel) return;
    if (!rangeState.start && !rangeState.end) {
        const today = new Date();
        rangeState.start = today;
        rangeState.end = today;
    }
    rangeState.view = new Date((rangeState.start || new Date()).getFullYear(), (rangeState.start || new Date()).getMonth(), 1);
    renderRangeCalendar();
    const trigger = document.getElementById('returnDatePreset');
    if (trigger) {
        const rect = trigger.getBoundingClientRect();
        panel.style.left = `${rect.left - trigger.offsetParent.getBoundingClientRect().left}px`;
        panel.style.top = `${trigger.offsetTop + trigger.offsetHeight + 8}px`;
    }
    panel.classList.add('show');
    panel.setAttribute('aria-hidden', 'false');
}

function closeRangeModal() {
    const panel = document.getElementById('returnRangePanel');
    if (!panel) return;
    panel.classList.remove('show');
    panel.setAttribute('aria-hidden', 'true');
}

function renderRangeCalendar() {
    const label = document.getElementById('rangeMonthLabel');
    const grid = document.getElementById('rangeGrid');
    if (!label || !grid) return;
    const view = rangeState.view || new Date();
    label.textContent = view.toLocaleString('vi-VN', { month: 'long', year: 'numeric' });

    const year = view.getFullYear();
    const month = view.getMonth();
    const firstDay = new Date(year, month, 1);
    const offset = (firstDay.getDay() + 6) % 7;
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const cells = [];
    for (let i = 0; i < offset; i++) {
        cells.push('<span class="range-day is-muted"></span>');
    }
    for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month, day);
        const key = formatDateValue(date);
        const isSelected = (rangeState.start && formatDateValue(rangeState.start) === key)
            || (rangeState.end && formatDateValue(rangeState.end) === key);
        const inRange = rangeState.start && rangeState.end && date >= startOfDay(rangeState.start) && date <= startOfDay(rangeState.end);
        cells.push(`<button type="button" class="range-day ${isSelected ? 'is-selected' : ''} ${inRange ? 'is-in-range' : ''}" data-date="${key}">${day}</button>`);
    }
    grid.innerHTML = cells.join('');
}

function startOfDay(date) {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

function formatDateValue(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function formatDateLabel(value) {
    if (!value) return '--/--/----';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '--/--/----';
    const d = String(date.getDate()).padStart(2, '0');
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const y = date.getFullYear();
    return `${d}/${m}/${y}`;
}

function setRangeValue(start, end) {
    const fromInput = document.getElementById('returnSearchFrom');
    const toInput = document.getElementById('returnSearchTo');
    const inline = document.getElementById('returnRangeInline');
    if (!fromInput || !toInput) return;
    fromInput.value = start ? formatDateValue(start) : '';
    toInput.value = end ? formatDateValue(end) : '';
    if (inline) {
        if (fromInput.value && toInput.value && fromInput.value === toInput.value) {
            inline.textContent = formatDateLabel(fromInput.value);
        } else {
            inline.textContent = `${formatDateLabel(fromInput.value)} → ${formatDateLabel(toInput.value)}`;
        }
    }
}



function renderOrderList(list) {
    const container = document.getElementById('returnInvoiceList');
    const empty = document.getElementById('returnInvoiceEmpty');
    if (!container || !empty) return;

    if (!list || list.length === 0) {
        container.innerHTML = '';
        empty.style.display = 'block';
        return;
    }

    empty.style.display = 'none';
    container.innerHTML = list.map(order => `
        <div class="invoice-row">
            <span class="invoice-cell"><button class="invoice-link" data-order-id="${order.id}">${order.invoiceNumber || '-'}</button></span>
            <span>${formatDateTime(order.createdAt)}</span>
            <span>${order.customerName || 'Khách lẻ'}</span>
            <span>${order.customerPhone || '-'}</span>
            <span>${formatPrice(order.totalAmount)}</span>
            <span>${order.cashierName || order.userName || '-'}</span>
        </div>
    `).join('');
}

async function openOrderDetail(orderId) {
    if (!orderId) return;
    try {
        const res = await fetch(`${API_BASE}/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            }
        });
        if (!res.ok) {
            if (res.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            alert('Không thể tải chi tiết hóa đơn.');
            return;
        }
        activeOrderDetail = await res.json();
        selectedReturnItems.clear();
        renderOrderDetail(activeOrderDetail);
        openModal('returnInvoiceModal');
    } catch (err) {
        console.error(err);
        alert('Lỗi kết nối khi tải chi tiết hóa đơn.');
    }
}

function renderOrderDetail(order) {
    if (!order) return;
    document.getElementById('returnDetailInvoiceNumber').textContent = order.invoiceNumber || '-';
    document.getElementById('returnDetailEmployee').textContent = order.userName || '-';
    document.getElementById('returnDetailCashier').textContent = order.cashierName || order.userName || '-';
    document.getElementById('returnDetailCustomer').textContent = order.customerName || 'Khách lẻ';

    const itemsContainer = document.getElementById('returnDetailItems');
    if (!itemsContainer) return;
    itemsContainer.innerHTML = (order.items || []).map(item => {
        const maxQty = item.quantity || 0;
        return `
            <div class="detail-row item-row" data-item-id="${item.id}" data-max-qty="${maxQty}">
                <span><input type="checkbox" class="return-item-check"></span>
                <span>${item.productName || '-'}</span>
                <span>${item.unit || '-'}</span>
                <span>${maxQty}</span>
                <span><input type="number" class="qty-input" min="1" max="${maxQty}" value="${maxQty}" disabled></span>
                <span>${formatPrice(item.price)}</span>
                <span>${formatPrice((item.price || 0) * maxQty)}</span>
            </div>
        `;
    }).join('');
}

function collectSelectedItems() {
    const rows = Array.from(document.querySelectorAll('#returnDetailItems .detail-row.item-row'));
    const selected = [];
    rows.forEach(row => {
        const checkbox = row.querySelector('.return-item-check');
        const qtyInput = row.querySelector('.qty-input');
        if (!checkbox?.checked) return;
        const itemId = Number(row.dataset.itemId);
        const maxQty = Number(row.dataset.maxQty);
        let qty = Number(qtyInput?.value || 0);
        if (!Number.isFinite(qty) || qty <= 0) return;
        if (qty > maxQty) qty = maxQty;
        const item = (activeOrderDetail?.items || []).find(i => i.id === itemId);
        if (!item) return;
        selected.push({
            ...item,
            selectedQty: qty
        });
    });
    return selected;
}

function openReturnConfirm(selectedItems) {
    if (!selectedItems.length) {
        alert('Vui lòng chọn sản phẩm cần trả.');
        return;
    }
    const list = document.getElementById('returnConfirmList');
    const totalEl = document.getElementById('returnRefundTotal');
    if (!list || !totalEl) return;

    let total = 0;
    list.innerHTML = selectedItems.map(item => {
        const line = (item.price || 0) * item.selectedQty;
        total += line;
        return `
            <div class="confirm-item">
                <span>${item.productName || '-'}</span>
                <span>${item.selectedQty} x ${formatPrice(item.price)}</span>
            </div>
        `;
    }).join('');
    totalEl.textContent = formatPrice(total);
    openModal('returnConfirmModal');
}

async function confirmReturn() {
    if (!activeOrderDetail) return;
    const selectedItems = collectSelectedItems();
    if (!selectedItems.length) {
        alert('Vui lòng chọn sản phẩm cần trả.');
        return;
    }

    const payload = {
        userId: parseInt(sessionStorage.getItem('userId'), 10) || null,
        customerId: activeOrderDetail.customerId || null,
        paid: true,
        paymentMethod: document.getElementById('returnRefundMethod')?.value || 'CASH',
        orderType: 'RETURN',
        originalOrderId: activeOrderDetail.id,
        refundMethod: document.getElementById('returnRefundMethod')?.value || 'CASH',
        returnReason: document.getElementById('returnReason')?.value || '',
        returnNote: document.getElementById('returnNote')?.value || '',
        items: selectedItems.map(item => ({
            productId: item.productId,
            quantity: item.selectedQty
        }))
    };

    try {
        const res = await fetch(`${API_BASE}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            },
            body: JSON.stringify(payload)
        });
        if (!res.ok) {
            const message = await res.text();
            alert(message || 'Không thể tạo hóa đơn trả hàng.');
            return;
        }
        const data = await res.json();
        alert(`Tạo hóa đơn trả hàng thành công: ${data.invoiceNumber || ''}`);
        closeModal('returnConfirmModal');
        closeModal('returnInvoiceModal');
    } catch (err) {
        console.error(err);
        alert('Lỗi kết nối khi tạo hóa đơn trả hàng.');
    }
}

function startExchange() {
    const selectedItems = collectSelectedItems();
    if (!selectedItems.length) {
        alert('Vui lòng chọn sản phẩm cần đổi.');
        return;
    }
    const draft = {
        originalOrderId: activeOrderDetail.id,
        customerId: activeOrderDetail.customerId || null,
        customerName: activeOrderDetail.customerName || '',
        customerPhone: activeOrderDetail.customerPhone || '',
        items: selectedItems.map(item => ({
            productId: item.productId,
            productName: item.productName,
            productCode: item.productCode,
            unit: item.unit,
            price: item.price,
            quantity: item.selectedQty
        }))
    };
    sessionStorage.setItem('exchangeDraft', JSON.stringify(draft));
    window.location.href = '/pages/employee-dashboard.html';
}

function openModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function bindEvents() {
    document.getElementById('returnSearchBtn')?.addEventListener('click', searchPaidOrders);
    const datePreset = document.getElementById('returnDatePreset');
    datePreset?.addEventListener('change', applyPresetRange);
    datePreset?.addEventListener('click', () => {
        if ((datePreset.value || '') === 'custom') {
            openRangeModal();
        }
    });
    document.getElementById('rangePrevMonth')?.addEventListener('click', () => {
        rangeState.view = new Date(rangeState.view.getFullYear(), rangeState.view.getMonth() - 1, 1);
        renderRangeCalendar();
    });
    document.getElementById('rangeNextMonth')?.addEventListener('click', () => {
        rangeState.view = new Date(rangeState.view.getFullYear(), rangeState.view.getMonth() + 1, 1);
        renderRangeCalendar();
    });
    document.getElementById('rangeClear')?.addEventListener('click', () => {
        rangeState.start = null;
        rangeState.end = null;
        setRangeValue(null, null);
        renderRangeCalendar();
    });
    document.getElementById('rangeToday')?.addEventListener('click', () => {
        const today = new Date();
        rangeState.start = today;
        rangeState.end = today;
        setRangeValue(today, today);
        renderRangeCalendar();
    });
    document.getElementById('rangeApply')?.addEventListener('click', () => {
        if (rangeState.start && rangeState.end && rangeState.end < rangeState.start) {
            const tmp = rangeState.start;
            rangeState.start = rangeState.end;
            rangeState.end = tmp;
        }
        setRangeValue(rangeState.start, rangeState.end);
        closeRangeModal();
        searchPaidOrders();
    });
    document.getElementById('rangeGrid')?.addEventListener('click', (e) => {
        const day = e.target.closest('.range-day');
        if (!day || !day.dataset.date) return;
        const picked = new Date(day.dataset.date);
        if (!rangeState.start || (rangeState.start && rangeState.end)) {
            rangeState.start = picked;
            rangeState.end = null;
        } else {
            rangeState.end = picked;
        }
        renderRangeCalendar();
    });
    ['returnSearchKeyword', 'returnSearchFrom', 'returnSearchTo'].forEach(id => {
        document.getElementById(id)?.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                searchPaidOrders();
            }
        });
    });

    document.getElementById('returnInvoiceList')?.addEventListener('click', (e) => {
        const btn = e.target.closest('.invoice-link');
        if (!btn) return;
        const orderId = Number(btn.dataset.orderId);
        if (!Number.isFinite(orderId)) return;
        openOrderDetail(orderId);
    });

    document.getElementById('returnDetailItems')?.addEventListener('change', (e) => {
        const row = e.target.closest('.detail-row.item-row');
        if (!row) return;
        const checkbox = row.querySelector('.return-item-check');
        const qtyInput = row.querySelector('.qty-input');
        if (!checkbox || !qtyInput) return;
        if (e.target.classList.contains('return-item-check')) {
            qtyInput.disabled = !checkbox.checked;
            if (checkbox.checked && !qtyInput.value) {
                qtyInput.value = row.dataset.maxQty || 1;
            }
        }
        if (e.target.classList.contains('qty-input')) {
            const max = Number(row.dataset.maxQty || 0);
            let value = Number(qtyInput.value || 0);
            if (value > max) value = max;
            if (value < 1) value = 1;
            qtyInput.value = value;
        }
    });

    document.getElementById('selectAllReturnItems')?.addEventListener('click', () => {
        const rows = Array.from(document.querySelectorAll('#returnDetailItems .detail-row.item-row'));
        const shouldSelect = rows.some(row => !row.querySelector('.return-item-check')?.checked);
        rows.forEach(row => {
            const checkbox = row.querySelector('.return-item-check');
            const qtyInput = row.querySelector('.qty-input');
            if (!checkbox || !qtyInput) return;
            checkbox.checked = shouldSelect;
            qtyInput.disabled = !shouldSelect;
            if (shouldSelect) {
                qtyInput.value = row.dataset.maxQty || 1;
            }
        });
    });

    document.getElementById('exchangeBtn')?.addEventListener('click', startExchange);
    document.getElementById('returnOnlyBtn')?.addEventListener('click', () => {
        openReturnConfirm(collectSelectedItems());
    });

    document.getElementById('confirmReturnBtn')?.addEventListener('click', confirmReturn);
    document.getElementById('cancelReturnConfirm')?.addEventListener('click', () => closeModal('returnConfirmModal'));
    document.getElementById('closeReturnConfirm')?.addEventListener('click', () => closeModal('returnConfirmModal'));

    document.getElementById('closeReturnInvoiceModal')?.addEventListener('click', () => closeModal('returnInvoiceModal'));

    document.querySelectorAll('#returnInvoiceModal, #returnConfirmModal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal(modal.id);
            }
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    bindEvents();
    applyPresetRange();
});


