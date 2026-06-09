const API_BASE = resolveApiBase();
let invoices = [];
let promotionIndex = null;
let currentInvoiceDetail = null;

window.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadUserInfo();
    ensureHeaderActions();
    setupActions();
    setupAppMenuModal();
    setupLogout();
    setupInvoiceDetailModal();
    handleInvoicePreset();
    initResizableColumns();
    loadInvoices();
});

function resolveApiBase() {
    const configured = window.API_BASE_URL || window.API_BASE;
    if (configured) {
        return configured.replace(/\/$/, '');
    }

    if (window.location.protocol === 'file:') {
        return 'http://localhost:8000/api';
    }

    if (['localhost', '127.0.0.1'].includes(window.location.hostname) && window.location.port !== '8080') {
        return '/api';
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

function loadUserInfo() {
    const username = sessionStorage.getItem('username');
    const userInitial = (username ? username[0] : 'E').toUpperCase();

    const initialEl = document.getElementById('userInitial');
    if (initialEl) initialEl.textContent = userInitial;
    const nameEl = document.getElementById('userName');
    if (nameEl) nameEl.textContent = username || 'Nhân viên';
}

function setupActions() {
    document.getElementById('backToPos')?.addEventListener('click', () => {
        window.location.href = '/pages/employee-dashboard.html';
    });

    const logo = document.getElementById('goPosFromLogo') || document.querySelector('.logo-mark');
    logo?.addEventListener('click', () => {
        window.location.href = '/pages/employee-dashboard.html';
    });

    document.getElementById('datePreset')?.addEventListener('change', applyFilters);
    document.getElementById('invoicePreset')?.addEventListener('change', handleInvoicePreset);
    document.getElementById('invoiceDateFrom')?.addEventListener('change', applyFilters);
    document.getElementById('invoiceDateTo')?.addEventListener('change', applyFilters);
    const filterIds = [
        'filterInvoiceNo',
        'filterInvoiceDate',
        'filterCreatedDate',
        'filterOrderCode',
        'filterStatus',
        'filterCustomerCode',
        'filterCustomerName',
        'filterCustomerPhone',
        'filterTotal',
        'filterNote',
        'filterSales',
        'filterCashier'
    ];
    filterIds.forEach(id => {
        document.getElementById(id)?.addEventListener('input', applyFilters);
        document.getElementById(id)?.addEventListener('change', applyFilters);
    });

    document.getElementById('invoiceList')?.addEventListener('click', (e) => {
        const link = e.target.closest('.invoice-link');
        if (!link) return;
        const orderId = link.getAttribute('data-order-id');
        if (orderId) {
            openInvoiceDetail(orderId);
        }
    });
}

function initResizableColumns() {
    const table = document.querySelector('.invoice-table');
    const headerRow = document.querySelector('.invoice-row.header');
    if (!table || !headerRow) return;

    const headers = Array.from(headerRow.children);
    if (headers.length === 0) return;

    const minWidth = 90;
    const getWidths = () => headers.map(header => Math.max(minWidth, Math.round(header.getBoundingClientRect().width)));
    let widths = getWidths();

    const applyWidths = () => {
        const cols = widths.map(width => `${width}px`).join(' ');
        table.style.setProperty('--invoice-cols', cols);
    };

    applyWidths();

    headers.forEach((header, index) => {
        if (index === headers.length - 1) return;
        const resizer = document.createElement('div');
        resizer.className = 'col-resizer';
        header.appendChild(resizer);

        resizer.addEventListener('pointerdown', (event) => {
            event.preventDefault();
            event.stopPropagation();
            resizer.setPointerCapture(event.pointerId);

            widths = getWidths();
            const startX = event.clientX;
            const startWidth = widths[index];
            const originalUserSelect = document.body.style.userSelect;
            document.body.style.userSelect = 'none';

            const onMove = (moveEvent) => {
                const delta = moveEvent.clientX - startX;
                widths[index] = Math.max(minWidth, Math.round(startWidth + delta));
                applyWidths();
            };

            const onUp = () => {
                resizer.releasePointerCapture(event.pointerId);
                document.body.style.userSelect = originalUserSelect;
                window.removeEventListener('pointermove', onMove);
                window.removeEventListener('pointerup', onUp);
            };

            window.addEventListener('pointermove', onMove);
            window.addEventListener('pointerup', onUp);
        });
    });
}

function ensureHeaderActions() {
    const headerActions = document.querySelector('.header-actions');
    if (!headerActions) return;

    if (!document.getElementById('logoutBtn')) {
        const logoutBtn = document.createElement('button');
        logoutBtn.id = 'logoutBtn';
        logoutBtn.className = 'icon-btn';
        logoutBtn.title = 'Đăng xuất';
        logoutBtn.setAttribute('aria-label', 'Đăng xuất');
        logoutBtn.innerHTML = `
            <svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
                <path d="M10 6H5v12h5" />
                <path d="M14 16l4-4-4-4" />
                <path d="M18 12H9" />
            </svg>
        `;
        headerActions.appendChild(logoutBtn);
    }

    if (!document.getElementById('appMenuBtn')) {
        const appMenuBtn = document.createElement('button');
        appMenuBtn.id = 'appMenuBtn';
        appMenuBtn.className = 'icon-btn';
        appMenuBtn.title = 'Ứng dụng';
        appMenuBtn.setAttribute('aria-label', 'Ứng dụng');
        appMenuBtn.innerHTML = `
            <svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
                <circle cx="6" cy="6" r="1.5" />
                <circle cx="12" cy="6" r="1.5" />
                <circle cx="18" cy="6" r="1.5" />
                <circle cx="6" cy="12" r="1.5" />
                <circle cx="12" cy="12" r="1.5" />
                <circle cx="18" cy="12" r="1.5" />
            </svg>
        `;
        headerActions.appendChild(appMenuBtn);
    }
}

function setupAppMenuModal() {
    ensureAppMenuModal();
    const modal = document.getElementById('appMenuModal');
    const openBtn = document.getElementById('appMenuBtn');
    const closeBtn = document.getElementById('closeAppMenu');
    if (!modal || !openBtn || !closeBtn) return;

    openBtn.addEventListener('click', () => {
        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
    });

    closeBtn.addEventListener('click', () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
    });

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.classList.remove('show');
            modal.setAttribute('aria-hidden', 'true');
        }
    });

    const grid = modal.querySelector('.app-menu-grid');
    grid?.addEventListener('click', (e) => {
        const tile = e.target.closest('.app-tile[data-app]');
        if (!tile) return;
        const target = tile.dataset.app;
        const route = resolveAppRoute(target);
        if (route) {
            window.location.href = route;
        }
    });
}

function ensureAppMenuModal() {
    if (document.getElementById('appMenuModal')) return;
    const modal = document.createElement('div');
    modal.className = 'modal side';
    modal.id = 'appMenuModal';
    modal.setAttribute('aria-hidden', 'true');
    modal.innerHTML = `
        <div class="modal-content app-menu">
            <div class="modal-header">
                <h3>Ứng dụng</h3>
                <button id="closeAppMenu" class="icon-btn small" type="button" aria-label="Đóng">×</button>
            </div>
            <div class="app-menu-grid">
                <button class="app-tile" data-app="pos">
                    <span class="app-icon app-blue">BH</span>
                    <span>Bán hàng</span>
                </button>
                <button class="app-tile" data-app="invoices">
                    <span class="app-icon app-pink">HD</span>
                    <span>DS hóa đơn</span>
                </button>
                <button class="app-tile" data-app="return-request" onclick="window.location.href='/pages/return-request.html'">
                    <span class="app-icon app-red">ĐT</span>
                    <span>Đổi trả hàng</span>
                </button>
                <button class="app-tile" data-app="print">
                    <span class="app-icon app-orange">IN</span>
                    <span>Máy in - Mẫu in</span>
                </button>
                <button class="app-tile" data-app="daily-report">
                    <span class="app-icon app-teal">BC</span>
                    <span>Báo cáo theo ngày</span>
                </button>
                <button class="app-tile" data-app="access-log">
                    <span class="app-icon app-gray">NK</span>
                    <span>Nhật ký truy cập</span>
                </button>
                <button class="app-tile" data-app="management">
                    <span class="app-icon app-indigo">QL</span>
                    <span>Trang quản lý</span>
                </button>
                <button class="app-tile" data-app="guide">
                    <span class="app-icon app-purple">HD</span>
                    <span>Hướng dẫn</span>
                </button>
                <button class="app-tile" data-app="intro">
                    <span class="app-icon app-orange">GT</span>
                    <span>Giới thiệu</span>
                </button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
}

function resolveAppRoute(target) {
    switch (target) {
        case 'pos':
            return '/pages/employee-dashboard.html';
        case 'orders':
            return '/pages/order-list.html';
        case 'invoices':
            return '/pages/invoice-list.html';
        case 'online-orders':
            return '/pages/online-orders.html';
        case 'returns':
            return '/pages/return-orders.html';
        case 'transfers':
            return '/pages/transfer-requests.html';
        case 'topup':
            return '/pages/topup-wallet.html';
        case 'cashflow':
            return '/pages/cashflow.html';
        case 'secondary':
            return '/pages/secondary-screen.html';
        case 'print':
            return '/pages/print-templates.html';
        case 'daily-report':
            return '/pages/daily-report.html';
        case 'access-log':
            return '/pages/access-log.html';
        case 'management':
            return '/pages/management.html';
        case 'guide':
            return '/pages/guide.html';
        case 'intro':
            return '/pages/introduction.html';
        default:
            return '';
    }
}

function setupLogout() {
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        if (confirm('Đăng xuất?')) {
            sessionStorage.clear();
            window.location.href = '/pages/login.html';
        }
    });
}

async function loadInvoices() {
    const listEl = document.getElementById('invoiceList');
    const emptyEl = document.getElementById('invoiceEmpty');
    if (listEl) {
        listEl.innerHTML = '<div class="invoice-row"><span>Đang tải...</span></div>';
    }

    try {
        const response = await fetch(`${API_BASE}/orders/summary`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
        });

        if (!response.ok) {
            if (response.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            throw new Error('Không tải được danh sách hóa đơn');
        }

        invoices = await response.json();
        renderInvoices(invoices || []);
    } catch (err) {
        if (listEl) listEl.innerHTML = '';
        if (emptyEl) {
            emptyEl.textContent = 'Không thể tải dữ liệu hóa đơn';
            emptyEl.style.display = 'block';
        }
    }
}

function applyFilters() {
    const preset = document.getElementById('datePreset')?.value || '';
    const dateRange = resolveDateRange(preset, '');
    const invoicePreset = document.getElementById('invoicePreset')?.value || 'today';
    const invoiceRange = resolveInvoiceRange(invoicePreset);

    const filterInvoiceNo = normalizeText(document.getElementById('filterInvoiceNo')?.value);
    const filterInvoiceDate = document.getElementById('filterInvoiceDate')?.value || '';
    const filterCreatedDate = document.getElementById('filterCreatedDate')?.value || '';
    const filterOrderCode = normalizeText(document.getElementById('filterOrderCode')?.value);
    const filterStatus = (document.getElementById('filterStatus')?.value || '').trim();
    const filterCustomerCode = normalizeText(document.getElementById('filterCustomerCode')?.value);
    const filterCustomerName = normalizeText(document.getElementById('filterCustomerName')?.value);
    const filterCustomerPhone = normalizeText(document.getElementById('filterCustomerPhone')?.value);
    const filterTotal = (document.getElementById('filterTotal')?.value || '').trim();
    const filterNote = normalizeText(document.getElementById('filterNote')?.value);
    const filterSales = normalizeText(document.getElementById('filterSales')?.value);
    const filterCashier = normalizeText(document.getElementById('filterCashier')?.value);
    const totalFilterRule = parseNumberFilter(filterTotal);

    const filtered = (invoices || []).filter(inv => {
        if (dateRange) {
            const createdAt = new Date(inv.createdAt);
            if (!Number.isNaN(createdAt.getTime())) {
                if (createdAt < dateRange.start || createdAt > dateRange.end) {
                    return false;
                }
            }
        }

        if (invoiceRange) {
            const createdAt = new Date(inv.createdAt);
            if (!Number.isNaN(createdAt.getTime())) {
                if (createdAt < invoiceRange.start || createdAt > invoiceRange.end) {
                    return false;
                }
            }
        }

        if (filterInvoiceDate && !matchesDate(inv.createdAt, filterInvoiceDate)) return false;
        if (filterCreatedDate && !matchesDate(inv.createdAt, filterCreatedDate)) return false;

        const invoiceNumber = normalizeText(inv.invoiceNumber || inv.orderCode || inv.code || (inv.id ? `HD-${inv.id}` : ''));
        const orderCode = normalizeText(inv.orderCode || inv.code || '');
        const customerCode = normalizeText(inv.customerCode || (inv.customerId ? `KH${String(inv.customerId).padStart(6, '0')}` : ''));
        const customerName = normalizeText(inv.customerName || '');
        const customerPhone = normalizeText(inv.customerPhone || '');
        const salesName = normalizeText(inv.salesName || inv.userName || '');
        const cashierName = normalizeText(inv.cashierName || inv.userName || '');
        const note = normalizeText(inv.note || '');
        const totalAmount = Number(inv.totalAmount) || 0;

        if (filterStatus && inv.status !== filterStatus) return false;
        if (filterInvoiceNo && !invoiceNumber.includes(filterInvoiceNo)) return false;
        if (filterOrderCode && !orderCode.includes(filterOrderCode)) return false;
        if (filterCustomerCode && !customerCode.includes(filterCustomerCode)) return false;
        if (filterCustomerName && !customerName.includes(filterCustomerName)) return false;
        if (filterCustomerPhone && !customerPhone.includes(filterCustomerPhone)) return false;
        if (filterNote && !note.includes(filterNote)) return false;
        if (filterSales && !salesName.includes(filterSales)) return false;
        if (filterCashier && !cashierName.includes(filterCashier)) return false;
        if (totalFilterRule && !totalFilterRule(totalAmount)) return false;

        return true;
    });

    renderInvoices(filtered);
}

function renderInvoices(list) {
    const listEl = document.getElementById('invoiceList');
    const emptyEl = document.getElementById('invoiceEmpty');
    const countEl = document.getElementById('invoiceCount');
    const totalEl = document.getElementById('invoiceTotal');

    if (!listEl || !emptyEl) return;

    if (!list || list.length === 0) {
        listEl.innerHTML = '';
        emptyEl.style.display = 'block';
        if (countEl) countEl.textContent = '0 kết quả';
        if (totalEl) totalEl.textContent = formatPrice(0);
        return;
    }

    emptyEl.style.display = 'none';

    const total = list.reduce((sum, inv) => sum + (Number(inv.totalAmount) || 0), 0);
    if (countEl) countEl.textContent = `${list.length} kết quả`;
    if (totalEl) totalEl.textContent = formatPrice(total);

    listEl.innerHTML = list.map(inv => {
        const createdAt = formatDate(inv.createdAt);
        const invoiceDate = createdAt;
        const customerName = inv.customerName || 'Khách lẻ';
        const customerPhone = inv.customerPhone || '-';
        const statusInfo = mapStatus(inv.status);
        const customerCode = inv.customerCode || (inv.customerId ? `KH${String(inv.customerId).padStart(6, '0')}` : '-');
        const salesName = inv.salesName || inv.userName || '-';
        const cashierName = inv.cashierName || inv.userName || '-';
        const note = inv.note || '-';

        const invoiceCode = inv.invoiceNumber || inv.orderCode || inv.code || (inv.id ? `HD-${inv.id}` : '-');
        return `
            <div class="invoice-row">
                <span class="invoice-link" data-order-id="${inv.id || ''}">${escapeHtml(invoiceCode)}</span>
                <span>${invoiceDate}</span>
                <span>${createdAt}</span>
                <span>-</span>
                <span><span class="status-badge ${statusInfo.className}">${statusInfo.label}</span></span>
                <span>${escapeHtml(customerCode)}</span>
                <span>${escapeHtml(customerName)}</span>
                <span>${escapeHtml(customerPhone)}</span>
                <span>${formatPrice(inv.totalAmount || 0)}</span>
                <span>${escapeHtml(note)}</span>
                <span>${escapeHtml(salesName)}</span>
                <span>${escapeHtml(cashierName)}</span>
            </div>
        `;
    }).join('');
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(price).replace('₫', 'đ');
}

function formatDate(value) {
    if (!value) return '-';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function mapStatus(status) {
    const s = String(status || '').toUpperCase();
    switch (s) {
        case 'PAID':
            return { label: 'Đã xác nhận', className: 'status-paid' };
        case 'CONFIRMED':
            return { label: 'Đã xác nhận', className: 'status-paid' };
        case 'DELIVERED':
        case 'RECEIVED':
            return { label: 'Giao hàng thành công', className: 'status-paid' };
        case 'SHIPPING':
        case 'SHIPPED':
            return { label: 'Đang giao hàng', className: 'status-unpaid' };
        case 'PACKING':
        case 'PROCESSING':
            return { label: 'Đang đóng gói', className: 'status-unpaid' };
        case 'RETURNED':
            return { label: 'Đổi trả', className: 'status-returned' };
        case 'UNPAID':
        case 'PENDING':
            return { label: 'Đơn mới', className: 'status-unpaid' };
        case 'CANCELLED':
            return { label: 'Đã hủy', className: 'status-returned' };
        default:
            return { label: status || 'Không rõ', className: 'status-unpaid' };
    }
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function resolveDateRange(preset, pickedDate) {
    const now = new Date();
    if (pickedDate) {
        const selected = new Date(pickedDate);
        if (Number.isNaN(selected.getTime())) return null;
        const start = new Date(selected.setHours(0, 0, 0, 0));
        const end = new Date(selected.setHours(23, 59, 59, 999));
        return { start, end };
    }
    if (preset === 'all') {
        return null;
    }
    const end = new Date(now);
    const start = new Date(now);
    if (preset === 'week') {
        start.setDate(start.getDate() - 6);
    } else if (preset === 'month') {
        start.setDate(start.getDate() - 29);
    } else if (!preset) {
        return null;
    } else {
        start.setHours(0, 0, 0, 0);
    }
    end.setHours(23, 59, 59, 999);
    return { start, end };
}

function normalizeText(value) {
    return String(value || '').trim().toLowerCase();
}

function matchesDate(value, pickedDate) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return false;
    const target = new Date(pickedDate);
    if (Number.isNaN(target.getTime())) return false;
    return date.toISOString().slice(0, 10) === target.toISOString().slice(0, 10);
}

function parseNumberFilter(raw) {
    if (!raw) return null;
    const cleaned = raw.replace(/\s+/g, '').replace(/,/g, '').replace(/\./g, '');
    const match = cleaned.match(/^(<=|>=|=)?(\d+)$/);
    if (!match) return null;
    const op = match[1] || '';
    const value = Number(match[2]);
    if (!Number.isFinite(value)) return null;
    if (op === '<=') return (n) => n <= value;
    if (op === '>=') return (n) => n >= value;
    if (op === '=') return (n) => n === value;
    return (n) => String(Math.round(n)).includes(String(value));
}

function handleInvoicePreset() {
    const preset = document.getElementById('invoicePreset')?.value || 'today';
    const rangeGroup = document.getElementById('invoiceRangeGroup');
    if (rangeGroup) {
        rangeGroup.classList.toggle('show', preset === 'custom');
    }
    applyFilters();
}

function resolveInvoiceRange(preset) {
    const now = new Date();
    const start = new Date(now);
    const end = new Date(now);
    if (preset === 'custom') {
        const from = document.getElementById('invoiceDateFrom')?.value || '';
        const to = document.getElementById('invoiceDateTo')?.value || '';
        if (!from || !to) return null;
        const fromDate = new Date(from);
        const toDate = new Date(to);
        if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) return null;
        fromDate.setHours(0, 0, 0, 0);
        toDate.setHours(23, 59, 59, 999);
        return { start: fromDate, end: toDate };
    }
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    if (preset === 'today') {
        return { start, end };
    }
    if (preset === 'yesterday') {
        start.setDate(start.getDate() - 1);
        end.setDate(end.getDate() - 1);
        return { start, end };
    }
    if (preset === 'thisWeek') {
        const day = start.getDay() || 7;
        start.setDate(start.getDate() - day + 1);
        return { start, end };
    }
    if (preset === 'lastWeek') {
        const day = start.getDay() || 7;
        end.setDate(end.getDate() - day);
        start.setDate(end.getDate() - 6);
        return { start, end };
    }
    if (preset === 'thisMonth') {
        start.setDate(1);
        return { start, end };
    }
    if (preset === 'lastMonth') {
        start.setMonth(start.getMonth() - 1, 1);
        end.setDate(0);
        return { start, end };
    }
    if (preset === 'last3Months') {
        start.setMonth(start.getMonth() - 2, 1);
        return { start, end };
    }
    if (preset === 'last6Months') {
        start.setMonth(start.getMonth() - 5, 1);
        return { start, end };
    }
    return null;
}

function setupInvoiceDetailModal() {
    const modal = document.getElementById('invoiceDetailModal');
    const closeBtn = document.getElementById('closeInvoiceDetail');
    if (!modal) return;

    const close = () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
    };
    closeBtn?.addEventListener('click', close);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            close();
        }
    });

    document.getElementById('printInvoiceDetailBtn')?.addEventListener('click', () => {
        if (currentInvoiceDetail) {
            printInvoiceDetail(currentInvoiceDetail);
        }
    });
}

async function openInvoiceDetail(orderId) {
    const modal = document.getElementById('invoiceDetailModal');
    const itemsEl = document.getElementById('invoiceDetailItems');
    if (!modal || !itemsEl) return;

    itemsEl.innerHTML = '<div class="detail-row"><span>Đang tải...</span></div>';
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');

    try {
        const headers = { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` };
        const promoPromise = loadPromotionIndex();
        const response = await fetch(`${API_BASE}/orders/${orderId}`, { headers });

        if (!response.ok) {
            if (response.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            throw new Error('Không tải được hóa đơn');
        }

        const data = await response.json();
        const promoMap = await promoPromise;
        renderInvoiceDetail(data, promoMap);
    } catch (err) {
        itemsEl.innerHTML = '<div class="detail-row"><span>Không thể tải dữ liệu</span></div>';
    }
}

function renderInvoiceDetail(data, promoMap) {
    if (!data) return;
    currentInvoiceDetail = data;
    setText('detailInvoiceNumber', data.invoiceNumber || '-');
    setText('detailEmployee', data.salesName || data.userName || '-');
    setText('detailCashier', data.cashierName || data.userName || '-');
    const customerName = data.customerName || 'Khách lẻ';
    const customerPhone = data.customerPhone || '';
    setText('detailCustomer', customerPhone ? `${customerName} (${customerPhone})` : customerName);

    const itemsEl = document.getElementById('invoiceDetailItems');
    const items = Array.isArray(data.items) ? data.items : [];
    if (!itemsEl) return;

    if (items.length === 0) {
        itemsEl.innerHTML = '<div class="detail-row"><span>Không có sản phẩm</span></div>';
        return;
    }

    itemsEl.innerHTML = items.map(item => {
        const quantity = Number(item.quantity) || 0;
        const unitPrice = Number(item.price) || 0;
        const baseTotal = Number(item.lineTotal) || unitPrice * quantity;
        const discountPercent = Number(item.discountPercent) || 0;
        const discountAmount = Number(item.discountAmount) || 0;
        const discountValue = discountPercent > 0
            ? baseTotal * (discountPercent / 100)
            : discountAmount;
        const lineTotal = Math.max(0, baseTotal - discountValue);
        const taxRate = Number(item.taxRate) || 0;
        const promoInfo = promoMap?.get(item.productId);
        const promoLine = promoInfo ? `<small class="detail-promo">KM: ${escapeHtml(promoInfo.label)}</small>` : '';
        return `
            <div class="detail-row">
                <span class="detail-item-name">
                    ${escapeHtml(item.productName || '-')}
                    ${promoLine}
                    ${item.productCode ? `<small>${escapeHtml(item.productCode)}</small>` : ''}
                </span>
                <span>${escapeHtml(item.unit || '-')}</span>
                <span>${quantity}</span>
                <span>${formatPrice(unitPrice)}</span>
                <span>${formatPrice(lineTotal)}</span>
                <span class="detail-tax">${taxRate ? `${taxRate}%` : '0%'}</span>
            </div>
        `;
    }).join('');
}

function printInvoiceDetail(data) {
    const printSize = localStorage.getItem('bizflow_print_size') || 'K80';
    let pageWidth = '80mm';
    let pageHeight = '200mm';
    if (printSize === 'K58') {
        pageWidth = '58mm';
        pageHeight = '160mm';
    } else if (printSize === 'A6') {
        pageWidth = '100mm';
        pageHeight = '150mm';
    }

    const items = Array.isArray(data.items) ? data.items : [];
    const itemRows = items.map((item) => {
        const quantity = Number(item.quantity) || 0;
        const unitPrice = Number(item.price) || 0;
        const baseTotal = Number(item.lineTotal) || unitPrice * quantity;
        const discountPercent = Number(item.discountPercent) || 0;
        const discountAmount = Number(item.discountAmount) || 0;
        const discountValue = discountPercent > 0
            ? baseTotal * (discountPercent / 100)
            : discountAmount;
        const lineTotal = Math.max(0, baseTotal - discountValue);
        return `
            <div class="receipt-item">
                <span>${escapeHtml(item.productName || '-')}</span>
                <span>${quantity}</span>
                <span>${formatPriceCompact(unitPrice)}</span>
                <span>${formatPriceCompact(lineTotal)}</span>
            </div>
        `;
    }).join('');

    const invoiceCode = data.invoiceNumber || '-';
    const invoiceDate = formatDate(data.createdAt);
    const cashier = data.cashierName || data.userName || '-';
    const customerName = data.customerName || 'Khách lẻ';
    const customerPhone = data.customerPhone || '-';
    const totalAmount = formatPriceCompact(Number(data.totalAmount) || 0);
    const note = data.note ? escapeHtml(data.note) : '-';

    const content = `
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8" />
    <title>In hoa don</title>
    <style>
        @page { size: ${pageWidth} ${pageHeight}; margin: 0; }
        html, body { width: ${pageWidth}; margin: 0; }
        body { font-family: Arial, sans-serif; }
        * { box-sizing: border-box; }
        .print-sheet { width: ${pageWidth}; margin: 0; display: flex; justify-content: center; padding-top: 2mm; box-sizing: border-box; }
        .invoice-receipt { width: 74mm; font-size: 10.5px; line-height: 1.15; color: #2f3644; display: grid; gap: 4px; }
        .receipt-header { text-align: center; display: grid; gap: 1px; }
        .receipt-brand { font-weight: 800; letter-spacing: 0.4px; }
        .receipt-meta { font-size: 9px; color: #5b6274; display: grid; gap: 0; }
        .receipt-section { display: grid; gap: 1px; }
        .receipt-divider { border-top: 1px dashed #c9ceda; margin: 1px 0; }
        .receipt-items { display: grid; gap: 2px; }
        .receipt-item { display: grid; grid-template-columns: 1.4fr 0.5fr 0.8fr 1fr; gap: 3px; }
        .receipt-item.header { font-weight: 700; font-size: 9.5px; color: #2f3644; }
        .receipt-item span:nth-child(2),
        .receipt-item span:nth-child(3),
        .receipt-item span:nth-child(4) { text-align: right; }
        .receipt-totals { display: grid; gap: 1px; }
        .receipt-totals div { display: flex; justify-content: space-between; }
        .receipt-note { font-size: 9px; color: #4b5366; display: grid; gap: 0; }
    </style>
</head>
<body>
<div class="print-sheet">
    <div class="invoice-receipt">
        <div class="receipt-header">
            <div class="receipt-brand">BizFlow POS</div>
            <div class="receipt-meta">
                <div>Mã hóa đơn: <strong>${escapeHtml(invoiceCode)}</strong></div>
                <div>Thời gian: <span>${escapeHtml(invoiceDate)}</span></div>
            </div>
        </div>
        <div class="receipt-section">
            <div>Thu ngân: <span>${escapeHtml(cashier)}</span></div>
            <div>Khách hàng: <span>${escapeHtml(customerName)}</span></div>
            <div>SĐT: <span>${escapeHtml(customerPhone)}</span></div>
        </div>
        <div class="receipt-divider"></div>
        <div class="receipt-items">
            <div class="receipt-item header">
                <span>SP</span>
                <span>SL</span>
                <span>Đơn giá</span>
                <span>Thành tiền</span>
            </div>
            ${itemRows}
        </div>
        <div class="receipt-divider"></div>
        <div class="receipt-totals">
            <div><span>Tổng cộng</span><span>${totalAmount}đ</span></div>
        </div>
        <div class="receipt-note">
            <span>Ghi chú:</span>
            <span>${note}</span>
        </div>
    </div>
</div>
<script>
    window.onload = () => {
        window.focus();
        window.print();
        setTimeout(() => window.close(), 200);
    };
</script>
</body>
</html>`;

    const printWindow = window.open('', '_blank', 'width=480,height=700');
    if (!printWindow) {
        window.print();
        return;
    }
    printWindow.document.open();
    printWindow.document.write(content);
    printWindow.document.close();
}

function formatPriceCompact(price) {
    return new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0
    }).format(price);
}

async function loadPromotionIndex() {
    if (promotionIndex) {
        return promotionIndex;
    }

    const headers = { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` };

    try {
        const promoPromise = fetch(`${API_BASE}/v1/promotions`, { headers })
            .then(res => (res.ok ? res.json() : []));
        const productPromise = fetch(`${API_BASE}/products`, { headers })
            .then(res => (res.ok ? res.json() : []));

        const [promoList, productList] = await Promise.all([promoPromise, productPromise]);
        const activePromos = (promoList || []).filter(isPromotionActive);
        promotionIndex = buildPromotionIndex(activePromos, productList || []);
        return promotionIndex;
    } catch (err) {
        promotionIndex = new Map();
        return promotionIndex;
    }
}

function buildPromotionIndex(promos, productList) {
    const productMap = new Map((productList || []).map((p) => [p.id, p]));
    const targetMap = new Map();

    (promos || []).forEach((promo) => {
        const targetIds = new Set();
        const targets = promo.targets || [];
        const bundles = promo.bundleItems || [];

        targets.forEach((target) => {
            if (!target || target.targetId == null) return;
            if (target.targetType === 'PRODUCT') {
                targetIds.add(target.targetId);
            }
            if (target.targetType === 'CATEGORY') {
                (productList || []).forEach((product) => {
                    if (product.categoryId === target.targetId) {
                        targetIds.add(product.id);
                    }
                });
            }
        });

        bundles.forEach((item) => {
            if (item?.productId != null) {
                targetIds.add(item.productId);
            }
        });

        targetIds.forEach((id) => {
            if (!targetMap.has(id)) {
                targetMap.set(id, []);
            }
            targetMap.get(id).push(promo);
        });
    });

    const index = new Map();
    targetMap.forEach((promoList, productId) => {
        const product = productMap.get(productId);
        if (!product) return;
        const best = selectBestPromotion(product, promoList);
        if (best?.promo) {
            index.set(productId, {
                promo: best.promo,
                label: formatPromotionLabel(best.promo)
            });
        }
    });

    return index;
}

function selectBestPromotion(product, promos) {
    const basePrice = Number(product?.price);
    const candidates = (promos || []).map((promo) => {
        const price = getPromoPrice(basePrice, promo);
        return { promo, price };
    });

    const priced = candidates.filter((c) => Number.isFinite(c.price));
    if (priced.length > 0) {
        priced.sort((a, b) => a.price - b.price);
        return { promo: priced[0].promo, price: priced[0].price };
    }

    return { promo: candidates[0]?.promo || null, price: NaN };
}

function getPromoPrice(basePrice, promo) {
    if (!Number.isFinite(basePrice) || !promo) return NaN;
    const value = Number(promo.discountValue);
    switch (normalizeDiscountType(promo.discountType)) {
        case 'PERCENT':
            if (!Number.isFinite(value)) return NaN;
            return Math.max(0, basePrice * (1 - value / 100));
        case 'FIXED':
            if (!Number.isFinite(value)) return NaN;
            return Math.max(0, basePrice - value);
        case 'BUNDLE':
            if (!Number.isFinite(value)) return NaN;
            return Math.max(0, value);
        case 'FREE_GIFT':
            return basePrice;
        default:
            return NaN;
    }
}

function normalizeDiscountType(value) {
    if (!value) return value;
    if (value === 'FIXED_AMOUNT') return 'FIXED';
    if (value === 'FREE_GIFT') return 'BUNDLE';
    return value;
}

function formatPromotionLabel(promo) {
    if (!promo) return 'Khuyen mai';
    const value = Number(promo.discountValue);
    const type = normalizeDiscountType(promo.discountType);
    if (type === 'PERCENT' && Number.isFinite(value)) {
        return `Giam ${value}%`;
    }
    if (type === 'FIXED' && Number.isFinite(value)) {
        return `Giam ${formatPrice(value)}`;
    }
    if (type === 'BUNDLE') {
        return 'Combo';
    }
    if (type === 'FREE_GIFT') {
        return 'Tang kem';
    }
    return promo.discountType || 'Khuyen mai';
}

function isPromotionActive(promo) {
    if (!promo) return false;
    if (promo.active === false) return false;
    const now = new Date();
    const start = parsePromotionDate(promo.startDate);
    const end = parsePromotionDate(promo.endDate);
    if (start && now < start) return false;
    if (end && now > end) return false;
    return true;
}

function parsePromotionDate(value) {
    if (!value) return null;
    if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0, second = 0] = value;
        const date = new Date(year, (month || 1) - 1, day || 1, hour, minute, second);
        return Number.isNaN(date.getTime()) ? null : date;
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = value;
    }
}

