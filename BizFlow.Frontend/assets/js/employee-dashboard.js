const API_BASE = resolveApiBase();
console.debug('[employee-dashboard] loaded version stock-9');

let products = [];
let cart = [];
let selectedCustomer = null;
let selectedEmployee = null;
const BEST_SELLERS_CATEGORY_ID = 'best-sellers';
const CATEGORY_NAME_BY_ID = {
    1: 'Nước giải khát',
    2: 'Đồ ăn vặt',
    3: 'Hóa mỹ phẩm',
    4: 'Gia vị & nước chấm',
    5: 'Sản phẩm chăm sóc nhà cửa',
    6: 'Bánh kẹo',
    7: 'Bia & rượu',
    8: 'Mì, phở, cháo gói',
    9: 'Đồ hộp & thực phẩm đóng hộp',
    10: 'Thuốc lá & diêm',
    11: 'Y tế',
    12: 'Đồ dùng học tập văn phòng'
};
let currentCategory = BEST_SELLERS_CATEGORY_ID;
let topSearchTerm = '';
let bottomSearchTerm = '';
let currentSort = 'name';
let customersLoaded = false;
let customers = [];
let customerSearchTerm = '';
let currentPaymentMethod = 'CASH';
let invoices = [];
let savedInvoices = [];
let activeInvoiceId = null;
let invoiceSequence = 1;
let cityCache = [];
let districtCache = [];
let wardCache = [];
let employeesLoaded = false;
let employees = [];
let exchangeDraft = null;
let promotionIndex = null;
let activeInventoryProductId = null;
let allPromotions = [];
let isAnalyzingCombo = false;
let activeCustomerDetailId = null;
let editingCustomerId = null;
let supportOwnerId = null;
let supportOwnerName = 'Owner';
let supportPollTimer = null;
let lastMessageCount = 0;
let supportUnreadCount = 0;
let globalUnreadPollTimer = null;
let cartPersistTimer = null;
let isRestoringCartState = false;
let isCartPersisting = false;
let cartStateLoaded = false;
const FALLBACK_CATEGORY_LABEL = 'Khac';
const customerOrderCache = new Map();
const TIER_DISCOUNT_BY_100 = {
    DONG: 10000,
    BAC: 12000,
    VANG: 15000,
    BACH_KIM: 22000,
    KIM_CUONG: 30000
};

const PRODUCT_ICON = `
    <svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
        <path d="M6 8h12l-1.2 11H7.2L6 8Z" />
        <path d="M9 8V6a3 3 0 0 1 6 0v2" />
    </svg>
`;
// Lightweight SVG fallback (data URI) used when an <img> fails to load.
const FALLBACK_SVG_DATA_URI = 'data:image/svg+xml;utf8,' + encodeURIComponent(
    `<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 300 200' width='300' height='200'>
        <rect width='100%' height='100%' rx='12' fill='%23f0f4f8' />
        <g transform='translate(65,30) scale(6)'>
            <path d='M6 8h12l-1.2 11H7.2L6 8Z' fill='%239aa3b6' />
            <path d='M9 8V6a3 3 0 0 1 6 0v2' fill='%239aa3b6' />
        </g>
    </svg>`
);
const PRODUCT_IMAGE_LIST_URL = `${API_BASE}/product-images`;
const PRODUCT_IMAGE_LIST_FALLBACK_URL = '/assets/data/product-image-files.json';
const productImageMap = new Map();
const productImageEntries = [];
let productImageMapReady = false;
const syntheticCategoryMap = new Map();
let nextSyntheticCategoryId = -1;
const POINTS_EARN_RATE_VND = 1000;
const EARN_POLICY_POINTS = 100;

// Points redemption variables
let usePointsEnabled = false;
let pointsToUse = 0;
let checkoutAddressCandidates = [];
let addressSuggestionQueryTimer = null;

const FALLBACK_PRODUCTS = [
    {
        id: 1,
        name: 'Sữa tươi nguyên kem',
        code: 'SGL330',
        barcode: '8931234567012',
        price: 15000,
        unit: 'lon',
        stock: 120,
        description: 'Sữa tươi tiệt trùng 330ml'
    },
    {
        id: 2,
        name: 'Gạo thơm đóng gói',
        code: 'CQ-DY160',
        barcode: '8931234567029',
        price: 18000,
        unit: 'gói',
        stock: 60,
        description: 'Gạo thơm 1kg'
    },
    {
        id: 3,
        name: 'Cà phê hòa tan',
        code: 'CC330',
        barcode: '8931234567036',
        price: 10000,
        unit: 'lon',
        stock: 200,
        description: 'Cà phê sữa 330ml'
    },
    {
        id: 4,
        name: 'Nước ngọt có ga',
        code: 'NGC240',
        barcode: '8931234567043',
        price: 12000,
        unit: 'lon',
        stock: 180,
        description: 'Lon 240ml'
    },
    {
        id: 5,
        name: 'Bánh quy bơ',
        code: 'BQB120',
        barcode: '8931234567050',
        price: 22000,
        unit: 'hộp',
        stock: 45,
        description: 'Bánh quy bơ 120g'
    },
    {
        id: 6,
        name: 'Mì ly ăn liền',
        code: 'MLY105',
        barcode: '8931234567067',
        price: 14000,
        unit: 'ly',
        stock: 90,
        description: 'Mì ly 105g'
    }
];

// Ensure the page remains interactive: remove common blocking overlays
// and re-enable pointer events. Also attach a temporary debug click
// logger to help trace issues where clicks are swallowed.
function ensureInteractive() {
    try {
        const selectors = ['.modal-backdrop', '.overlay', '.app-overlay', '#pageOverlay', '.fullscreen-overlay'];
        selectors.forEach(sel => {
            try {
                document.querySelectorAll(sel).forEach(el => el.remove());
            } catch (e) { }
        });
        try { document.body.style.pointerEvents = 'auto'; } catch (e) { }
        const main = document.querySelector('body') || document.getElementById('app') || document.getElementById('root');
        if (main) try { main.style.pointerEvents = 'auto'; } catch (e) { }
        console.debug('[ensureInteractive] removed blocking overlays');
    } catch (e) {
        console.warn('[ensureInteractive] error', e);
    }

    try {
        if (!window.__debugClickAttached) {
            document.addEventListener('click', function (ev) {
                console.debug('[DEBUG CLICK] target=', ev.target);
            }, { capture: true });
            window.__debugClickAttached = true;
        }
    } catch (e) { }
}

// Checkout validation helpers (global) — used by multiple event handlers and renderers
function isCheckoutValid() {
    try {
        if (getCurrentUserId()) {
            return true;
        }
        const phoneEl = document.getElementById('checkoutCustomerPhone');
        const addrEl = document.getElementById('checkoutCustomerAddress');
        const phone = phoneEl ? (phoneEl.value || '').trim() : '';
        const addr = addrEl ? (addrEl.value || '').trim() : '';
        return phone.length > 0 && addr.length > 0;
    } catch (e) {
        return false;
    }
}
function updateCheckoutButtonState() {
    try {
        const btn = document.getElementById('checkoutBtn');
        if (!btn) return;
        btn.disabled = !isCheckoutValid() || !Array.isArray(cart) || cart.length === 0;
    } catch (e) { }
}

window.addEventListener('DOMContentLoaded', async () => {
    // Defensive init: run critical startup steps but do not allow
    // one failure to prevent attaching UI event handlers.
    try { checkAuth(); } catch (e) { console.warn('checkAuth failed', e); }
    try { loadUserInfo(); } catch (e) { console.warn('loadUserInfo failed', e); }
    try {
        await loadCartStateFromServer();
    } catch (e) {
        console.warn('loadCartStateFromServer failed', e);
    }
    try {
        await restorePersistedSelectedCustomerState();
    } catch (e) {
        console.warn('restorePersistedSelectedCustomerState failed', e);
    }
    try {
        await loadCurrentEmployee();
    } catch (e) {
        console.warn('loadCurrentEmployee failed', e);
    }
    try {
        if (!selectedCustomer || selectedCustomer.id === 0) {
            await restorePersistedSelectedCustomerState();
        }
    } catch (e) {
        console.warn('restorePersistedSelectedCustomerState failed', e);
    }
    try { fixPaymentPanelText(); } catch (e) { console.warn('fixPaymentPanelText failed', e); }

    // Initialize selectedCustomer only when it wasn't loaded by loadCurrentEmployee()
    if (!selectedCustomer || !(selectedCustomer.id > 0)) {
        selectedCustomer = { id: 0, name: 'Khách lẻ', phone: '-', totalPoints: 0, monthlyPoints: 0, tier: '' };
    }
    const selectedCustomerLabel = document.getElementById('selectedCustomer');
    if (selectedCustomerLabel) {
        selectedCustomerLabel.textContent = (selectedCustomer && selectedCustomer.id > 0) ? (selectedCustomer.name || 'Khách lẻ') : 'Khách lẻ';
    }

    // Ensure page is interactive (remove blocking overlays) before wiring handlers
    try { ensureInteractive(); } catch (e) { console.warn('ensureInteractive failed', e); }
    // Auto-migrate old reviews format to new format with productIds
    try { migrateReviewsFormat(); } catch (e) { console.warn('migrateReviewsFormat failed', e); }
    // Always attach UI handlers; protect each setup call so one failure doesn't block others
    try { setupEventListeners(); } catch (e) { console.warn('setupEventListeners failed', e); }
    try { setupOrderTracking(); } catch (e) { console.warn('setupOrderTracking failed', e); }
    try { setupCustomerModal(); } catch (e) { console.warn('setupCustomerModal failed', e); }
    try { setupProductDetailModal(); } catch (e) { console.warn('setupProductDetailModal failed', e); }
    try { setupInventoryModal(); } catch (e) { console.warn('setupInventoryModal failed', e); }
    try { setupCustomerDetailModal(); } catch (e) { console.warn('setupCustomerDetailModal failed', e); }
    try { setupEmployeeSelector(); } catch (e) { console.warn('setupEmployeeSelector failed', e); }
    try { setupAppMenuModal(); } catch (e) { console.warn('setupAppMenuModal failed', e); }
    try { setupInvoiceModal(); } catch (e) { console.warn('setupInvoiceModal failed', e); }
    try { setupContactSupport(); } catch (e) { console.warn('setupContactSupport failed', e); }

    // initialize payment panels and prefill checkout customer fields
    try {
        updatePaymentPanels();
        const checkoutName = document.getElementById('checkoutCustomerName');
        if (checkoutName) checkoutName.value = '';
        const checkoutPhone = document.getElementById('checkoutCustomerPhone');
        if (checkoutPhone) checkoutPhone.value = '';
        const checkoutAddress = document.getElementById('checkoutCustomerAddress');
        if (checkoutAddress) checkoutAddress.value = '';
        // wire input events to keep button state in sync
        const _phoneEl = document.getElementById('checkoutCustomerPhone');
        const _addrEl = document.getElementById('checkoutCustomerAddress');
        _phoneEl?.addEventListener('input', updateCheckoutButtonState);
        _addrEl?.addEventListener('input', updateCheckoutButtonState);
        // initial state
        updateCheckoutButtonState();
    } catch (e) {
        console.warn('payment panel init failed', e);
    }

    try {
        // Keep invoice/customer state restored from server; only initialize
        // a fresh invoice when there is no restored invoice state.
        if (!Array.isArray(invoices) || invoices.length === 0) {
            initInvoices();
        }
    } catch (e) { console.warn('initInvoices failed', e); }

    try {
        document.getElementById('productsGrid').innerHTML =
            '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #999;">Đang tải...</div>';
    } catch (e) { }

    // Load product image map and products in background; do not block UI setup
    (async () => {
        try { await loadProductImageMap(); } catch (e) { console.warn('loadProductImageMap failed', e); }
        try { await loadProducts(); } catch (e) { console.warn('loadProducts failed', e); }
        try { applyExchangeDraft(); } catch (e) { console.warn('applyExchangeDraft failed', e); }
    })();

    const customerSearchInput = document.getElementById('customerSearch');
    customerSearchInput?.addEventListener('focus', () => {
        if (customerSearchInput.classList.contains('has-selection')) {
            return;
        }
        const customerList = document.getElementById('customerList');
        if (customerList) {
            customerList.style.display = 'block';
        }
        if (!customersLoaded) {
            loadCustomers().then(() => applyCustomerFilter());
            return;
        }
        applyCustomerFilter();
    });
});

function fixPaymentPanelText() {
    const cashPanel = document.getElementById('cashPanel');
    if (cashPanel) {
        const rows = cashPanel.querySelectorAll('.summary-row');
        const cashLabel = rows[0]?.querySelector('span');
        const changeLabel = rows[1]?.querySelector('span');
        if (cashLabel) cashLabel.textContent = 'Ti\u1ec1n kh\u00e1ch \u0111\u01b0a';
        if (changeLabel) changeLabel.textContent = 'Tr\u1ea3 l\u1ea1i';
    }

    const noteLabel = document.querySelector('label[for=\"paymentNote\"]');
    if (noteLabel) noteLabel.textContent = 'Ghi ch\u00fa thanh to\u00e1n';
    const noteInput = document.getElementById('paymentNote');
    if (noteInput) noteInput.placeholder = 'Nh\u1eadp ghi ch\u00fa...';

    const saveBtn = document.getElementById('saveBillBtn');
    if (saveBtn) saveBtn.textContent = 'Th\u00eam v\u00e0o gi\u1ecf h\u00e0ng (F10)';
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) checkoutBtn.textContent = 'Thanh to\u00e1n (F9)';
}

function applyExchangeDraft() {
    const raw = sessionStorage.getItem('exchangeDraft');
    if (!raw) return;
    try {
        exchangeDraft = JSON.parse(raw);
    } catch (err) {
        console.error('Invalid exchange draft', err);
        sessionStorage.removeItem('exchangeDraft');
        exchangeDraft = null;
        return;
    }
    if (!exchangeDraft || !Array.isArray(exchangeDraft.items) || exchangeDraft.items.length === 0) {
        return;
    }

    cart = exchangeDraft.items.map(item => ({
        productId: item.productId,
        productName: item.productName,
        productPrice: Number(item.price) || 0,
        quantity: -Math.abs(Number(item.quantity) || 0),
        productCode: item.productCode || '',
        unit: item.unit || '',
        stock: 1,
        isReturnItem: true
    })).filter(item => item.quantity !== 0);

    if (exchangeDraft.customerId) {
        selectedCustomer = {
            id: exchangeDraft.customerId,
            name: exchangeDraft.customerName || 'Kh×ch h×ng',
            phone: exchangeDraft.customerPhone || '-',
            totalPoints: 0,
            monthlyPoints: 0,
            tier: ''
        };
        const searchInput = document.getElementById('customerSearch');
        if (searchInput) {
            searchInput.value = selectedCustomer.name || selectedCustomer.phone || '';
            searchInput.classList.add('has-selection');
            searchInput.readOnly = true;
        }
        const addBtn = document.getElementById('addCustomerBtn');
        if (addBtn) {
            addBtn.style.display = 'none';
        }
        // Refresh authoritative customer details (points, tier) from server
        try { refreshCustomerDetailFromApi(exchangeDraft.customerId).catch(() => { }); } catch (e) { }
        const clearBtn = document.getElementById('clearCustomerBtn');
        if (clearBtn) {
            clearBtn.style.display = 'inline-flex';
        }
    }

    renderCart();
    updateTotal();
    sessionStorage.removeItem('exchangeDraft');
    queuePersistCartState();

    // UX: bring attention to cart and allow immediate checkout (marketplace-style)
    try {
        const cartArea = document.getElementById('cartArea');
        if (cartArea) {
            cartArea.classList.add('cart-highlight');
            setTimeout(() => cartArea.classList.remove('cart-highlight'), 800);
        }
        const checkoutBtn = document.getElementById('checkoutBtn');
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
            checkoutBtn.focus({ preventScroll: false });
            checkoutBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    } catch (e) {
        console.warn('Failed to focus checkout after addToCart', e);
    }
}

function getCurrentUserId() {
    const raw = parseInt(sessionStorage.getItem('userId'), 10);
    return Number.isFinite(raw) && raw > 0 ? raw : null;
}

function getSelectedCustomerStorageKey() {
    const userId = getCurrentUserId();
    return userId ? `selected_customer_state_${userId}` : 'selected_customer_state';
}

function getAuthHeaders() {
    const token = sessionStorage.getItem('accessToken') || '';
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
    };
}

function persistSelectedCustomerState() {
    try {
        if (!selectedCustomer || !selectedCustomer.id) return;
        const payload = {
            id: selectedCustomer.id,
            name: selectedCustomer.name || 'Khách lẻ',
            phone: selectedCustomer.phone || '-',
            address: selectedCustomer.address || '',
            tier: selectedCustomer.tier || '',
            totalPoints: Number.isFinite(Number(selectedCustomer.totalPoints)) ? Number(selectedCustomer.totalPoints) : 0,
            monthlyPoints: Number.isFinite(Number(selectedCustomer.monthlyPoints)) ? Number(selectedCustomer.monthlyPoints) : 0,
            updatedAt: new Date().toISOString()
        };
        localStorage.setItem(getSelectedCustomerStorageKey(), JSON.stringify(payload));
    } catch (e) {
        console.warn('[persistSelectedCustomerState] failed', e);
    }
}

function loadPersistedSelectedCustomerState() {
    try {
        const preferredKey = getSelectedCustomerStorageKey();
        let raw = localStorage.getItem(preferredKey);
        if (!raw) {
            raw = localStorage.getItem('selected_customer_state');
        }
        if (!raw) return null;
        const saved = JSON.parse(raw);
        if (!saved || !saved.id) return null;
        return {
            id: saved.id,
            name: saved.name || 'Khách lẻ',
            phone: saved.phone || '-',
            address: saved.address || '',
            tier: saved.tier || '',
            totalPoints: Number.isFinite(Number(saved.totalPoints)) ? Number(saved.totalPoints) : 0,
            monthlyPoints: Number.isFinite(Number(saved.monthlyPoints)) ? Number(saved.monthlyPoints) : 0
        };
    } catch (e) {
        console.warn('[loadPersistedSelectedCustomerState] failed', e);
        return null;
    }
}

async function restorePersistedSelectedCustomerState() {
    const persistedCustomer = loadPersistedSelectedCustomerState();
    if (!persistedCustomer || !persistedCustomer.id) return null;
    if (!selectedCustomer || selectedCustomer.id === 0 || selectedCustomer.id !== persistedCustomer.id) {
        applyCustomerSelection(persistedCustomer, { openDetail: false });
    }
    if (selectedCustomer && selectedCustomer.id > 0) {
        await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
    }
    return selectedCustomer;
}

function normalizeInvoiceState(raw, index = 0) {
    const fallbackName = `Giỏ hàng ${index + 1}`;
    return {
        id: raw?.id || `invoice-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`,
        name: raw?.name || fallbackName,
        cart: Array.isArray(raw?.cart) ? raw.cart.map(item => ({ ...item })) : [],
        // Persist only minimal customer info (id/name/phone). Do NOT store
        // `totalPoints`/`monthlyPoints` in the saved cart to avoid stale
        // values overwriting server-authoritative totals on restore.
        selectedCustomer: (raw?.selectedCustomer)
            ? {
                id: raw.selectedCustomer.id || 0,
                name: raw.selectedCustomer.name || raw.selectedCustomer.phone || 'Khách lẻ',
                phone: raw.selectedCustomer.phone || '-'
            }
            : { id: 0, name: 'Khách lẻ', phone: '-' },
        paymentMethod: raw?.paymentMethod || 'CASH',
        cashReceived: raw?.cashReceived || '',
        paymentNote: raw?.paymentNote || '',
        splitLine: !!raw?.splitLine,
        topSearchTerm: raw?.topSearchTerm || '',
        bottomSearchTerm: raw?.bottomSearchTerm || ''
    };
}

function buildCartStatePayload() {
    saveActiveInvoiceState();
    return {
        invoices: (invoices || []).map((invoice, idx) => normalizeInvoiceState(invoice, idx)),
        savedInvoices: (savedInvoices || []).map((invoice, idx) => normalizeInvoiceState(invoice, idx)),
        activeInvoiceId,
        invoiceSequence,
        currentCategory,
        currentSort,
        updatedAt: new Date().toISOString()
    };
}

function applyCartStatePayload(payload) {
    if (!payload || typeof payload !== 'object') return;

    const restoredInvoices = Array.isArray(payload.invoices)
        ? payload.invoices.map((invoice, idx) => normalizeInvoiceState(invoice, idx))
        : [];

    const restoredSaved = Array.isArray(payload.savedInvoices)
        ? payload.savedInvoices.map((invoice, idx) => normalizeInvoiceState(invoice, idx))
        : [];

    invoices = restoredInvoices.length > 0 ? restoredInvoices : [createInvoiceState()];
    savedInvoices = restoredSaved;

    const activeExists = invoices.some(inv => inv.id === payload.activeInvoiceId);
    activeInvoiceId = activeExists ? payload.activeInvoiceId : invoices[0].id;

    const parsedSeq = Number(payload.invoiceSequence);
    invoiceSequence = Number.isFinite(parsedSeq) && parsedSeq > 0
        ? parsedSeq
        : Math.max(1, getNextInvoiceNumberFromAll());

    if (payload.currentCategory != null) {
        currentCategory = String(payload.currentCategory);
    }
    if (payload.currentSort) {
        currentSort = payload.currentSort;
    }

    renderInvoiceTabs();
    const active = getActiveInvoice();
    if (active) {
        applyInvoiceState(active);
    }
    renderSavedBills();
}

async function loadCartStateFromServer() {
    const userId = getCurrentUserId();
    if (!userId) return;

    try {
        const res = await fetch(`${API_BASE}/orders/cart/${userId}`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
        });
        if (!res.ok) return;

        const data = await res.json();
        if (!data?.state) return;

        isRestoringCartState = true;
        applyCartStatePayload(data.state);
        // mark that we successfully restored a server-saved cart state so
        // we should not overwrite selectedCustomer with linked user customer
        // during the subsequent loadCurrentEmployee() step.
        cartStateLoaded = true;

        // Ensure any selectedCustomer restored from the saved cart is refreshed
        // from the customer service so points are up-to-date (avoid stale points
        // persisted inside a saved invoice object).
        try {
            const invoices = data.state?.invoices || [];
            for (const inv of invoices) {
                const sc = inv?.selectedCustomer;
                if (sc && sc.id && Number(sc.id) > 0) {
                    try { await refreshCustomerDetailFromApi(Number(sc.id)); } catch (e) { }
                }
            }
        } catch (e) {
            console.warn('Failed to refresh customers from restored cart state', e);
        }
    } catch (err) {
        console.warn('Cannot load persisted cart state', err);
    } finally {
        isRestoringCartState = false;
    }
}

function queuePersistCartState() {
    if (isRestoringCartState) return;

    if (cartPersistTimer) {
        clearTimeout(cartPersistTimer);
    }

    cartPersistTimer = setTimeout(() => {
        persistCartStateNow();
    }, 500);
}

async function persistCartStateNow() {
    const userId = getCurrentUserId();
    if (!userId || isRestoringCartState || isCartPersisting) return;

    isCartPersisting = true;
    try {
        const state = buildCartStatePayload();
        await fetch(`${API_BASE}/orders/cart/${userId}`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({ state })
        });
    } catch (err) {
        console.warn('Cannot persist cart state', err);
    } finally {
        isCartPersisting = false;
    }
}

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

/**
 * Wrapper quanh fetch() tự động refresh access token khi hết hạn (401).
 * Sử dụng giống fetch() bình thường: authFetch(url, options)
 * - Tự thêm header Authorization nếu có accessToken
 * - Khi gặp 401, gọi /api/auth/refresh để lấy token mới rồi retry
 * - Nếu refresh cũng thất bại, redirect về trang login
 */
let _isRefreshing = false;
let _refreshQueue = [];

async function authFetch(url, options = {}) {
    const token = sessionStorage.getItem('accessToken');
    if (token) {
        options.headers = options.headers || {};
        if (typeof options.headers.set === 'function') {
            options.headers.set('Authorization', `Bearer ${token}`);
        } else {
            options.headers['Authorization'] = `Bearer ${token}`;
        }
    }

    let response = await fetch(url, options);

    // Nếu không phải 401 hoặc không có refresh token thì trả về luôn
    if (response.status !== 401) {
        return response;
    }

    const refreshToken = sessionStorage.getItem('refreshToken');
    if (!refreshToken) {
        sessionStorage.clear();
        window.location.href = '/pages/login.html';
        return response;
    }

    // Tránh gọi refresh đồng thời nhiều lần
    if (_isRefreshing) {
        return new Promise((resolve) => {
            _refreshQueue.push(() => {
                const newToken = sessionStorage.getItem('accessToken');
                if (newToken) {
                    options.headers = options.headers || {};
                    options.headers['Authorization'] = `Bearer ${newToken}`;
                }
                resolve(fetch(url, options));
            });
        });
    }

    _isRefreshing = true;

    try {
        const refreshRes = await fetch(`${API_BASE}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken })
        });

        if (refreshRes.ok) {
            const data = await refreshRes.json();
            sessionStorage.setItem('accessToken', data.accessToken);
            if (data.refreshToken) {
                sessionStorage.setItem('refreshToken', data.refreshToken);
            }

            // Retry request gốc với token mới
            options.headers = options.headers || {};
            options.headers['Authorization'] = `Bearer ${data.accessToken}`;
            response = await fetch(url, options);

            // Giải phóng queue
            _refreshQueue.forEach(cb => cb());
            _refreshQueue = [];

            return response;
        } else {
            // Refresh token cũng hết hạn → đăng xuất
            sessionStorage.clear();
            window.location.href = '/pages/login.html';
            return response;
        }
    } catch (err) {
        console.error('[authFetch] Refresh token failed:', err);
        sessionStorage.clear();
        window.location.href = '/pages/login.html';
        return response;
    } finally {
        _isRefreshing = false;
    }
}

function setupAppMenuModal() {
    const modal = document.getElementById('appMenuModal');
    const openBtn = document.getElementById('appMenuBtn');
    const closeBtn = document.getElementById('closeAppMenu');
    if (!modal || !openBtn || !closeBtn) return;
    const menuGrid = modal.querySelector('.app-menu-grid');

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

    menuGrid?.addEventListener('click', (e) => {
        const tile = e.target.closest('.app-tile[data-app]');
        if (!tile) return;
        const target = tile.dataset.app;
        const route = resolveAppRoute(target);
        if (route) {
            window.location.href = route;
        }
    });
}

function resolveAppRoute(target) {
    switch (target) {
        case 'pos':
            return '/pages/employee-dashboard.html';
        case 'promotions':
            return '/pages/promotions.html';
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
        case 'tracking':
            return '/pages/order-tracking.html';
        default:
            return '';
    }
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

    document.getElementById('userInitial').textContent = userInitial;
    document.getElementById('userNameDropdown').textContent = username || 'Nhân viên';
}

async function findExistingCustomerForCurrentUser(userData) {
    const username = sessionStorage.getItem('username') || userData?.username || '';
    const email = userData?.email || userData?.emailAddress || userData?.email_address || '';
    const phone = userData?.phoneNumber || userData?.phone || userData?.phone_number || '';
    const name = userData?.fullName || userData?.name || userData?.displayName || '';

    const identityCustomer = await refreshSelectedCustomerByIdentity({
        username,
        email,
        phone,
        name
    });
    if (identityCustomer) {
        return identityCustomer;
    }

    const normalizedPhone = normalizePhoneForOrder(phone);
    if (normalizedPhone) {
        return await refreshSelectedCustomerByPhone(normalizedPhone);
    }
    return null;
}

async function loadCurrentEmployee() {
    const userId = sessionStorage.getItem('userId');
    if (!userId) return;

    try {
        const res = await fetch(`${API_BASE}/users/${userId}`, {
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
            throw new Error('Không tải được thông tin nhân viên');
        }

        const data = await res.json();
        selectedEmployee = {
            id: data.id,
            username: data.username,
            name: data.fullName || data.username
        };

        const employeeNameEl = document.getElementById('selectedEmployeeName');
        if (employeeNameEl) {
            employeeNameEl.textContent = selectedEmployee.name || selectedEmployee.username;
        }

        const userNameDropdown = document.getElementById('userNameDropdown');
        if (userNameDropdown) {
            userNameDropdown.textContent = data.username || data.fullName || 'Nhân viên';
        }

        const userInitialEl = document.getElementById('userInitial');
        if (userInitialEl) {
            const initial = (data.fullName || data.username || 'E')[0]?.toUpperCase() || 'E';
            userInitialEl.textContent = initial;
        }

        // Try to fetch a linked Customer record for this authenticated user so
        // we can show member points in checkout. Endpoint: GET /api/customers/by-user/{userId}
        try {
            if (!selectedCustomer || selectedCustomer.id === 0) {
                await restorePersistedSelectedCustomerState();
            }

            if (selectedCustomer && selectedCustomer.id > 0) {
                // Preserve the selected customer restored from the saved cart or persisted state.
                // Always refresh authoritative points from the server so the UI shows the
                // current account balance after reload.
                await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
            }

            const username = sessionStorage.getItem('username');
            const userPhone = (data?.phoneNumber || data?.phone || '').toString().trim();
            const normalizedPhone = normalizePhoneForOrder(userPhone);
            const queryParts = [];
            if (username) queryParts.push(`username=${encodeURIComponent(username)}`);
            if (normalizedPhone) queryParts.push(`phone=${encodeURIComponent(normalizedPhone)}`);
            const queryString = queryParts.length > 0 ? `?${queryParts.join('&')}` : '';
            const custRes = await fetch(`${API_BASE}/customers/by-user/${userId}${queryString}`, {
                headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
            });
            if (custRes.ok) {
                const cust = await custRes.json();
                if (cust && cust.id) {
                    // Always bind checkout membership to the authenticated account.
                    applyCustomerSelection({
                        id: cust.id,
                        name: cust.name || 'Khách lẻ',
                        phone: cust.phone || '-',
                        address: cust.address || '',
                        totalPoints: cust.totalPoints ?? cust.total_points ?? 0,
                        monthlyPoints: cust.monthlyPoints ?? cust.monthly_points ?? 0,
                        tier: cust.tier || ''
                    }, { openDetail: false });
                    await refreshCustomerDetailFromApi(Number(cust.id));
                } else {
                    const existingCustomer = await findExistingCustomerForCurrentUser(data);
                    if (existingCustomer) {
                        await refreshCustomerDetailFromApi(Number(existingCustomer.id));
                    }
                    if (!selectedCustomer || selectedCustomer.id === 0) {
                        const customer = await upsertCurrentUserCustomer(userId, data);
                        if (customer && customer.id) {
                            applyCustomerSelection({
                                id: customer.id,
                                name: customer.name || 'Khách lẻ',
                                phone: customer.phone || '-',
                                address: customer.address || '',
                                totalPoints: customer.totalPoints ?? customer.total_points ?? 0,
                                monthlyPoints: customer.monthlyPoints ?? customer.monthly_points ?? 0,
                                tier: customer.tier || ''
                            }, { openDetail: false });
                            await refreshCustomerDetailFromApi(Number(customer.id));
                        }
                    }
                }
            } else {
                const userPhone = (data?.phoneNumber || data?.phone || '').toString().trim();
                const normalizedPhone = normalizePhoneForOrder(userPhone);
                const identityCustomer = await refreshSelectedCustomerByIdentity({
                    phone: userPhone,
                    username,
                    email: data?.email || data?.emailAddress || null
                });
                if (!identityCustomer && normalizedPhone) {
                    await refreshSelectedCustomerByPhone(normalizedPhone);
                }
                if ((!selectedCustomer || selectedCustomer.id === 0) && !cartStateLoaded) {
                    const customer = await upsertCurrentUserCustomer(userId, data);
                    if (customer && customer.id) {
                        applyCustomerSelection({
                            id: customer.id,
                            name: customer.name || 'Khách lẻ',
                            phone: customer.phone || '-',
                            address: customer.address || '',
                            totalPoints: customer.totalPoints ?? customer.total_points ?? 0,
                            monthlyPoints: customer.monthlyPoints ?? customer.monthly_points ?? 0,
                            tier: customer.tier || ''
                        }, { openDetail: false });
                        await refreshCustomerDetailFromApi(Number(customer.id));
                    }
                }
            }
        } catch (err) {
            console.warn('Could not fetch customer by user', err);
            const username = sessionStorage.getItem('username');
            const existingCustomer = await findExistingCustomerForCurrentUser(data);
            if (!existingCustomer) {
                const persistedCustomer = loadPersistedSelectedCustomerState();
                if ((!selectedCustomer || selectedCustomer.id === 0) && persistedCustomer && persistedCustomer.id > 0) {
                    applyCustomerSelection(persistedCustomer, { openDetail: false });
                    await refreshCustomerDetailFromApi(Number(persistedCustomer.id));
                }
            }
        }

        if (selectedCustomer && selectedCustomer.id > 0 && getCustomerPoints(selectedCustomer) === 0) {
            await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
        }
    } catch (err) {
    }
}

async function upsertCurrentUserCustomer(userId, userData) {
    if (!userId) return null;
    const body = {
        userId: Number(userId),
        username: sessionStorage.getItem('username') || userData?.username || null,
        email: userData?.email || userData?.emailAddress || userData?.email_address || null,
        name: userData?.fullName || userData?.name || userData?.username || 'Khách hàng',
        phone: userData?.phoneNumber || userData?.phone || userData?.phone_number || null
    };
    try {
        const response = await fetch(`${API_BASE}/customers/upsertByUser`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            },
            body: JSON.stringify(body)
        });
        if (!response.ok) return null;
        return await response.json();
    } catch (e) {
        console.warn('[upsertCurrentUserCustomer] failed', e);
        return null;
    }
}

async function loadProducts() {
    try {
        // Cache-busting: thêm timestamp để luôn lấy data mới nhất
        const timestamp = Date.now();
        const response = await fetch(`${API_BASE}/inventory/shelves?_t=${timestamp}`, {
            headers: {
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                'Cache-Control': 'no-cache, no-store, must-revalidate',
                'Pragma': 'no-cache'
            }
        });

        if (!response.ok) {
            if (response.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            throw new Error('Failed to load products');
        }

        const shelvesData = await response.json();

        console.log('[loadProducts] Raw shelves data:', shelvesData);

        // Support two response shapes: either an array or an object { value: [...] }
        const shelvesArray = Array.isArray(shelvesData) ? shelvesData : (Array.isArray(shelvesData?.value) ? shelvesData.value : []);

        // CHỈ map những sản phẩm có quantity > 0 (đang thực sự bán)
        products = shelvesArray
            .filter(shelf => shelf.quantity > 0)
            .map(shelf => ({
                id: shelf.productId,
                name: shelf.productName,
                code: shelf.productCode,
                barcode: shelf.productCode,
                price: shelf.price || 0,
                stock: shelf.quantity,
                categoryId: shelf.categoryId,
                categoryName: shelf.categoryName || shelf.category || shelf.groupName || '',
                unit: shelf.unit || 'cái',
                status: 'active'
            }));

        console.log('[loadProducts] Loaded from shelves (quantity > 0):', products.length);
        console.log('[loadProducts] Products:', products.map(p => `${p.code} (${p.name}) - qty: ${p.stock}`));

        if (products.length === 0) {
            console.warn('[loadProducts] No products on shelves with quantity > 0');
            // Try alternative endpoint (through gateway) in case API_BASE is not reachable
            try {
                const altRes = await fetch('/api/inventory/shelves');
                if (altRes.ok) {
                    const altData = await altRes.json();
                    const altArray = Array.isArray(altData) ? altData : (Array.isArray(altData?.value) ? altData.value : []);
                    products = altArray.filter(shelf => shelf.quantity > 0).map(shelf => ({
                        id: shelf.productId,
                        name: shelf.productName,
                        code: shelf.productCode,
                        barcode: shelf.productCode,
                        price: shelf.price || 0,
                        stock: shelf.quantity,
                        categoryId: shelf.categoryId,
                        categoryName: shelf.categoryName || shelf.category || shelf.groupName || '',
                        unit: shelf.unit || 'cái',
                        status: 'active'
                    }));
                    console.log('[loadProducts] Loaded from gateway fallback:', products.length);
                }
            } catch (e) {
                console.warn('[loadProducts] Gateway fallback failed', e);
            }

            // Final fallback to embedded sample products so UI isn't empty
            if (products.length === 0) {
                console.warn('[loadProducts] Using FALLBACK_PRODUCTS as last resort');
                products = FALLBACK_PRODUCTS.slice();
            }
        }

        await loadPromotionIndex();
        renderCategoryList();
        filterProducts();
    } catch (err) {
        console.error('[loadProducts] Error:', err);
        products = [];
        renderCategoryList();
        filterProducts();
    }
}

async function loadProductImageMap() {
    if (productImageMapReady) return;
    try {
        const files = await fetchProductImageList();
        if (!Array.isArray(files) || files.length === 0) {
            productImageMapReady = false;
            console.warn('[loadProductImageMap] No image list available');
            return;
        }

        // reset existing maps to avoid duplicates on retry
        productImageMap.clear();
        productImageEntries.length = 0;

        files.forEach((filePath) => {
            if (!filePath || typeof filePath !== 'string') return;
            const filename = filePath.split('/').pop() || '';
            const baseName = filename.replace(/\.[^.]+$/, '');
            try {
                const key = normalizeProductKey(baseName);
                if (!key) return;
                if (!productImageMap.has(key)) {
                    productImageMap.set(key, filePath);
                }
                productImageEntries.push({ key, path: filePath });
            } catch (e) {
                console.warn('[loadProductImageMap] key error for', filePath, e);
            }
        });

        productImageMapReady = true;
        console.log('[loadProductImageMap] loaded', productImageEntries.length, 'entries');
    } catch (err) {
        productImageMapReady = false;
        console.warn('[loadProductImageMap] Error fetching image list', err);
    }
}

async function fetchProductImageList() {
    const token = sessionStorage.getItem('accessToken');
    const candidates = [
        {
            url: PRODUCT_IMAGE_LIST_URL,
            options: {
                cache: 'no-store',
                headers: token ? { 'Authorization': `Bearer ${token}` } : undefined
            }
        },
        {
            url: PRODUCT_IMAGE_LIST_FALLBACK_URL,
            options: { cache: 'no-store' }
        }
    ];

    const merged = [];
    const seen = new Set();

    for (const candidate of candidates) {
        try {
            const response = await fetch(candidate.url, candidate.options);
            if (!response.ok) {
                continue;
            }
            const data = await response.json();
            if (!Array.isArray(data)) {
                continue;
            }
            for (const entry of data) {
                if (!entry || typeof entry !== 'string') continue;
                if (seen.has(entry)) continue;
                seen.add(entry);
                merged.push(entry);
            }
        } catch (err) {
        }
    }

    return merged.length > 0 ? merged : null;
}

function normalizeProductKey(value) {
    return stripDiacritics((value || '').toString().trim().toLowerCase())
        .replace(/[^a-z0-9]+/g, '');
}

function getProductImageSrc(product) {
    if (!productImageMap || productImageMap.size === 0) return '';
    const nameKey = normalizeProductKey(product?.name || '');
    if (!nameKey) return '';

    // direct match by normalized product name
    if (productImageMap.has(nameKey)) {
        return productImageMap.get(nameKey);
    }

    // try match by product code or barcode
    const codeKey = normalizeProductKey(product?.code || product?.barcode || '');
    if (codeKey && productImageMap.has(codeKey)) {
        return productImageMap.get(codeKey);
    }

    // Fallback: try to match by containment (product name is a subset of filename or vice versa)
    let bestMatch = null;
    for (const entry of productImageEntries) {
        if (!entry || !entry.key) continue;
        if (entry.key.includes(nameKey) || nameKey.includes(entry.key)) {
            if (!bestMatch || entry.key.length < bestMatch.key.length) {
                bestMatch = entry;
            }
        }
    }
    if (bestMatch) return bestMatch.path;

    // token scoring: prefer entries that match the most tokens from product name
    const tokens = (nameKey.match(/[a-z0-9]{3,}/g) || []);
    if (tokens.length > 0) {
        let scored = null;
        for (const entry of productImageEntries) {
            if (!entry || !entry.key) continue;
            let score = 0;
            for (const t of tokens) {
                if (entry.key.includes(t)) score++;
            }
            if (score > 0 && (!scored || score > scored.score || (score === scored.score && entry.key.length < scored.entry.key.length))) {
                scored = { entry, score };
            }
        }
        if (scored) return scored.entry.path;
    }

    // very small prefix match: try first 6 chars
    const prefix = nameKey.slice(0, 6);
    if (prefix) {
        for (const entry of productImageEntries) {
            if (entry && entry.key && entry.key.startsWith(prefix)) {
                return entry.path;
            }
        }
    }

    return '';
}

function buildProductImageMarkup(product) {
    const imageSrc = getProductImageSrc(product);
    if (!imageSrc) {
        const safeName = escapeHtml(product?.name || 'Sản phẩm');
        const pid = product?.id || '';
        // render a placeholder background so rendering doesn't produce broken <img>
        return `<div class="product-image-bg missing-product-image" data-product-name="${safeName}" data-product-id="${pid}" style="background-image:url('${FALLBACK_SVG_DATA_URI}');"></div>`;
    }
    const safeName = escapeHtml(product?.name || 'Sản phẩm');
    // Add simple onerror fallback that switches to a safe SVG data-URI placeholder
    const encoded = encodeURI(imageSrc);
    return `<div class="product-image-bg" data-product-name="${safeName}" data-product-id="${product?.id || ''}" style="background-image:url('${encoded}');"></div>`;
}

async function loadCustomers() {
    if (customersLoaded) return;

    const list = document.getElementById('customerList');
    if (!list) return;

    try {
        const response = await fetch(`${API_BASE}/customers`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
        });

        if (response.ok) {
            const data = await response.json();
            if (Array.isArray(data)) {
                customers = data;
            } else if (data?.value && Array.isArray(data.value)) {
                customers = data.value;
            } else {
                customers = [];
            }
        } else {
            console.warn('[loadCustomers] failed to fetch customers', response.status, response.statusText);
        }
    } catch (err) {
        console.warn('[loadCustomers] Error fetching customers', err);
        customers = customers || [];
    } finally {
        customersLoaded = true;
        const customersHtml = (customers || []).map(c => {
            const phone = c.phone || '-';
            const secondary = c.email || c.address || '-';
            const customerId = c.id ?? '';
            return `
            <div class="customer-item"
                data-customer-id="${customerId}"
                data-customer-name="${escapeHtml(c.name || '')}"
                data-customer-phone="${escapeHtml(phone)}">
                <div class="customer-info">
                    <p class="customer-name">
                        <button type="button" class="customer-name-btn" data-customer-id="${customerId}" onclick="openCustomerDetailFromButton(event)">
                            ${escapeHtml(c.name || 'Khách hàng')}
                        </button>
                    </p>
                    <p class="customer-phone">${escapeHtml(phone)}</p>
                </div>
                <div class="customer-meta">
                    <span class="customer-phone">${escapeHtml(phone)}</span>
                    <span class="customer-sub">${escapeHtml(secondary)}</span>
                </div>
            </div>
            `;
        }).join('');

        list.innerHTML = customersHtml || '<div class="customer-empty">Chưa có khách hàng</div>';
    }
}

function applyCustomerFilter() {
    const keyword = customerSearchTerm.trim().toLowerCase();
    const searchInput = document.getElementById('customerSearch');
    const list = document.getElementById('customerList');
    if (searchInput?.classList.contains('has-selection')) {
        if (list) {
            list.style.display = 'none';
        }
        return;
    }
    if (list) {
        list.style.display = 'block';
    }
    if (!keyword) {
        renderCustomers(getSuggestedCustomers());
        return;
    }

    const filtered = customers.filter(c => {
        const name = (c.name || '').toLowerCase();
        const phone = (c.phone || '').toLowerCase();
        const email = (c.email || '').toLowerCase();
        return name.includes(keyword) || phone.includes(keyword) || email.includes(keyword);
    });

    renderCustomers(filtered);
}

// Try to resolve images for rendered placeholders by attempting common filename variants.
async function resolveMissingImagesOnPage(maxPerRun = 40) {
    try {
        const imgs = Array.from(document.querySelectorAll('.missing-product-image'));
        if (!imgs || imgs.length === 0) return;
        let count = 0;
        for (const img of imgs) {
            if (count >= maxPerRun) break;
            const name = img.dataset.productName || '';
            const pid = img.dataset.productId || '';
            const found = await tryFindImageForProduct(name, pid);
            if (found) {
                // if element is background container, update background-image; otherwise update src
                if (img.tagName.toLowerCase() === 'img') {
                    img.src = found;
                } else {
                    img.style.backgroundImage = `url('${found}')`;
                }
                img.classList.remove('missing-product-image');
                try {
                    const key = normalizeProductKey(name);
                    if (key && !productImageMap.has(key)) {
                        productImageMap.set(key, found);
                        productImageEntries.push({ key, path: found });
                    }
                } catch (e) { }
            }
            count++;
        }
    } catch (e) {
        console.warn('[resolveMissingImagesOnPage] error', e);
    }
}

function generateFilenameCandidates(name, code) {
    const exts = ['.jpg', '.jpeg', '.png', '.webp', '.gif'];
    const raw = (name || '').toString().trim();
    const stripped = stripDiacritics(raw).toLowerCase();
    const tokens = stripped.replace(/[^a-z0-9\s]+/g, ' ').trim().split(/\s+/).filter(Boolean);
    const candidates = new Set();

    function pushBase(b) {
        if (!b) return;
        exts.forEach(e => candidates.add(b + e));
    }

    // original raw variations
    pushBase(raw);
    pushBase(raw.replace(/\s+/g, '-'));
    pushBase(raw.replace(/\s+/g, '_'));
    pushBase(stripped);
    pushBase(tokens.join('-'));
    pushBase(tokens.join(''));
    pushBase(tokens.join('_'));

    // remove parentheses content
    const noPar = raw.replace(/\(.*?\)/g, '').trim();
    pushBase(noPar);
    pushBase(noPar.replace(/\s+/g, '-'));

    // code-based
    if (code) {
        pushBase(code.toString());
        pushBase(code.toString().toLowerCase());
    }

    return Array.from(candidates).map(fn => '/assets/img/img_sanpham/' + fn);
}

async function tryFindImageForProduct(name, code) {
    const candidates = generateFilenameCandidates(name, code);
    for (const c of candidates) {
        try {
            const encoded = c.split('/').map((seg, idx) => idx === 0 ? seg : encodeURIComponent(seg)).join('/');
            // try GET but only asking for headers may be blocked; do a lightweight GET
            const res = await fetch(encoded, { method: 'GET', cache: 'no-store' });
            if (res && res.ok) {
                const ct = res.headers.get('content-type') || '';
                if (ct.startsWith('image/') || c.toLowerCase().match(/\.(jpg|jpeg|png|webp|gif)$/)) {
                    return encoded;
                }
            }
        } catch (e) {
            // ignore
        }
    }
    return null;
}

function getSuggestedCustomers() {
    return customers.slice(0, 8);
}

function normalizeCustomerKey(customer) {
    const phone = (customer.phone || '').replace(/\s+/g, '');
    if (phone) return `phone:${phone}`;
    return `id:${customer.id || Math.random()}`;
}

function dedupeCustomers(list) {
    const seen = new Map();
    (list || []).forEach(c => {
        const key = normalizeCustomerKey(c);
        if (!seen.has(key)) {
            seen.set(key, c);
        }
    });
    return Array.from(seen.values());
}

async function addToCart(productId, productName, productPrice) {
    const qty = getCurrentQty();
    const splitLine = document.getElementById('splitLine')?.checked;
    const product = products.find(p => p.id === productId) || {};
    const stock = getStockValue(product);
    const basePrice = Number(product.price) || productPrice || 0;

    console.log('[addToCart] Input:', { productId, productName, productPrice, qty, basePrice, product });

    // Check if product has promotion
    let effectivePrice = basePrice;
    let promoId = null;
    let exceedsPromoQuota = false;

    if (promotionIndex && product.id != null) {
        const promoInfo = promotionIndex.get(product.id);
        console.log('[addToCart] promoInfo:', promoInfo);

        if (promoInfo?.promo) {
            promoId = promoInfo.promo.id;

            // Check if adding this quantity would exceed promotion quota
            const maxQty = Number(promoInfo.promo.maxQuantity);
            const usedQty = Math.max(0, Number(promoInfo.promo.usedQuantity || 0));

            if (Number.isFinite(maxQty) && maxQty > 0) {
                const cartQtyForThisProduct = (cart.find(item => item.productId === productId && !item.isFreeGift)?.quantity || 0);
                const totalQtyWillBe = cartQtyForThisProduct + qty;
                const remaining = Math.max(0, maxQty - usedQty - cartQtyForThisProduct);

                if (totalQtyWillBe > (maxQty - usedQty)) {
                    // Promotion quota exceeded - ask for confirmation
                    const promoName = promoInfo.promo.name || promoInfo.promo.code || 'Khuyến mãi';
                    const confirmMessage = `Khuyến mãi "${promoName}" chỉ còn ${remaining} sản phẩm. Bạn muốn thêm ${qty} sản phẩm không?\n\nSố lượng vượt mức sẽ được tính giá gốc.`;

                    if (!confirm(confirmMessage)) {
                        console.log('[addToCart] User cancelled - promotion quota exceeded');
                        return;
                    }
                    exceedsPromoQuota = true;
                    console.log('[addToCart] User confirmed - proceeding with mixed pricing');
                }
            }

            // For BUNDLE, must calculate with actual quantity
            if (normalizeDiscountType(promoInfo.promo.discountType) === 'BUNDLE') {
                const cartQtyForThisProduct = (cart.find(item => item.productId === productId && !item.isFreeGift)?.quantity || 0);
                effectivePrice = getPromoPrice(basePrice, promoInfo.promo, qty, cartQtyForThisProduct);
                console.log('[addToCart] BUNDLE effectivePrice:', effectivePrice);
            } else {
                // For other promo types (PERCENT, FIXED), quantity doesn't matter
                const cartQtyForThisProduct = (cart.find(item => item.productId === productId && !item.isFreeGift)?.quantity || 0);
                effectivePrice = getPromoPrice(basePrice, promoInfo.promo, 1, cartQtyForThisProduct);
                console.log('[addToCart] Non-BUNDLE effectivePrice:', effectivePrice);
            }
        }
    }

    const resolvedPrice = Number.isFinite(effectivePrice) ? effectivePrice : (productPrice || 0);
    console.log('[addToCart] Final resolvedPrice:', resolvedPrice);

    if (!splitLine) {
        const existingItem = cart.find(item => item.productId === productId && !item.isFreeGift);
        if (existingItem) {
            const oldQty = existingItem.quantity;  // Lưu số lượng cũ
            const oldPrice = existingItem.productPrice;  // Lưu giá cũ
            const newQty = oldQty + qty;
            existingItem.quantity = newQty;
            existingItem.stock = stock;

            // Recalculate price for promos with quota
            if (promotionIndex && product.id != null) {
                const promoInfo = promotionIndex.get(product.id);
                if (promoInfo?.promo) {
                    // Calculate price for newly added items
                    let newItemPrice = resolvedPrice;
                    if (normalizeDiscountType(promoInfo.promo.discountType) === 'BUNDLE') {
                        newItemPrice = getPromoPrice(basePrice, promoInfo.promo, qty, oldQty);
                    } else {
                        newItemPrice = getPromoPrice(basePrice, promoInfo.promo, 1, oldQty);
                    }

                    // Calculate average price: (old_total + new_items_total) / new_qty
                    const oldTotalPrice = oldQty * oldPrice;
                    const newTotalPrice = oldTotalPrice + (qty * newItemPrice);
                    existingItem.productPrice = newTotalPrice / newQty;

                    console.log('[addToCart] Updating existing item price - oldQty:', oldQty, 'oldPrice:', oldPrice, 'newItemPrice:', newItemPrice, 'averagePrice:', existingItem.productPrice);
                    existingItem.promoId = promoInfo?.promo?.id || null;
                } else {
                    existingItem.productPrice = resolvedPrice;
                    existingItem.promoId = null;
                }
            } else {
                existingItem.productPrice = resolvedPrice;
                existingItem.promoId = null;
            }
        } else {
            cart.push({
                productId,
                productName,
                productPrice: resolvedPrice,
                quantity: qty,
                productCode: product.code || product.barcode || '',
                unit: product.unit || '',
                stock,
                promoId: promoId,
                isFreeGift: false
            });
        }
    } else {
        cart.push({
            productId,
            productName,
            productPrice: resolvedPrice,
            quantity: qty,
            productCode: product.code || product.barcode || '',
            unit: product.unit || '',
            stock,
            promoId: promoId,
            isFreeGift: false
        });
    }

    renderCart();
    updateTotal();

    // Ph×n t×ch combo sau khi th×m s?n ph?m
    await analyzeCartForCombo();

    const qtyInput = document.getElementById('qtyInput');
    if (qtyInput) {
        qtyInput.value = '1';
        if (isToolbarSearchOpen()) {
            renderToolbarSearchResults(document.getElementById('searchInput').value);
        }
    }
    queuePersistCartState();
}

function clearCart(resetCustomer = true) {
    cart = [];
    renderCart();
    updateTotal();
    if (resetCustomer) {
        clearSelectedCustomer();
    }
    queuePersistCartState();
}

async function createOrder(isPaid) {
    if (cart.length === 0) {
        showPopup('Giỏ hàng trống!', { type: 'error' });
        return;
    }
    // Bỏ qua gift items khi check tồn kho (vì gift items có stock = 0)
    const outOfStock = cart.find(item => !item.isReturnItem && !item.isFreeGift && (!Number.isFinite(Number(item.stock)) || Number(item.stock) < Number(item.quantity)));
    if (outOfStock) {
        showPopup('Có sản phẩm hết hàng. Vui lòng kiểm tra số lượng tồn.', { type: 'error' });
        return;
    }

    const userId = parseInt(sessionStorage.getItem('userId'), 10) || null;
    const customerId = selectedCustomer && selectedCustomer.id > 0 ? selectedCustomer.id : null;
    const appliedPointsSummary = getAppliedMemberDiscountSummary(getTotalAmount());
    const payload = {
        userId,
        customerId,
        paid: isPaid,
        // Always send the chosen payment method so backend can create pending transfer payments with tokens
        paymentMethod: currentPaymentMethod,
        // Optional shipping info (filled when user chooses COD)
        shipping: null,
        usePoints: shouldUseMemberPoints(),
        pointsToUse: appliedPointsSummary.pointsUsed,
        orderType: exchangeDraft ? 'EXCHANGE' : null,
        originalOrderId: exchangeDraft?.originalOrderId || null,
        items: cart.map(item => ({
            productId: item.productId,
            quantity: item.quantity
        }))
    };

    // include explicit customer info when customer isn't an existing record
    if (selectedCustomer) {
        payload.customerName = selectedCustomer.name || null;
        payload.customerPhone = selectedCustomer.phone || null;
        payload.customerAddress = selectedCustomer.address || null;
        payload.customerEmail = selectedCustomer.email || null;
    }

    // If shipping (COD) selected in UI, populate shipping object and set paymentMethod to COD
    try {
        // Read checkout customer fields (always visible) and apply locally for receipt
        // If a member is already selected (selectedCustomer.id > 0), merge the
        // entered checkout fields into the existing selectedCustomer so we don't
        // overwrite the member id or their points. Only create an anonymous
        // customer (id:0) when there is no selected member.
        const cname = document.getElementById('checkoutCustomerName')?.value?.trim() || '';
        const cphone = document.getElementById('checkoutCustomerPhone')?.value?.trim() || '';
        const caddr = document.getElementById('checkoutCustomerAddress')?.value?.trim() || '';
        if (cname || cphone || caddr) {
            if (selectedCustomer && Number.isFinite(Number(selectedCustomer.id)) && Number(selectedCustomer.id) > 0) {
                // Keep account identity stable: do not overwrite member name/phone
                // from temporary checkout input fields.
                selectedCustomer = Object.assign({}, selectedCustomer, {
                    address: caddr || selectedCustomer.address
                });
            } else {
                selectedCustomer = {
                    id: 0,
                    name: cname || (selectedCustomer?.name || 'Khách lẻ'),
                    phone: cphone || (selectedCustomer?.phone || '-'),
                    address: caddr || (selectedCustomer?.address || '')
                };
            }
        }
    } catch (e) {
        console.warn('Could not read shipping fields', e);
    }

    // Ensure payload includes any customer info entered in checkout fields
    if (selectedCustomer) {
        payload.customerName = selectedCustomer.name || payload.customerName || null;
        payload.customerPhone = selectedCustomer.phone || payload.customerPhone || null;
        payload.customerAddress = selectedCustomer.address || payload.customerAddress || null;
        payload.customerEmail = selectedCustomer.email || payload.customerEmail || null;
    }

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
            showPopup(message || 'Không thể tạo đơn hàng.', { type: 'error' });
            return;
        }

        const data = await res.json();
        const receiptData = buildReceiptData(data, { usePoints: shouldUseMemberPoints() });
        const invoiceCode = receiptData.invoiceNumber || '-';
        if (isPaid) {
            await openInvoiceModal(receiptData);
            applyLocalStockAfterSale();
        }
        await loadPromotionIndex(true);
        filterProducts(document.getElementById('searchInput')?.value || '');
        // Clear items but keep the selected customer (so loyalty points and account association remain)
        clearCart(false);
        // Ensure checkout input fields are cleared after order creation
        try {
            const cnameEl = document.getElementById('checkoutCustomerName');
            const cphoneEl = document.getElementById('checkoutCustomerPhone');
            const caddrEl = document.getElementById('checkoutCustomerAddress');
            if (cnameEl) cnameEl.value = '';
            if (cphoneEl) cphoneEl.value = '';
            if (caddrEl) caddrEl.value = '';
        } catch (e) {
            console.warn('Failed to clear checkout inputs', e);
        }
        if (exchangeDraft) {
            sessionStorage.removeItem('exchangeDraft');
            exchangeDraft = null;
        }
        saveActiveInvoiceState();
        try {
            await persistCartStateNow();
        } catch (err) {
            console.warn('persistCartStateNow failed', err);
            queuePersistCartState();
        }
        // Refresh authoritative customer data so the UI reflects the correct
        // point balance after creating the order (prevents showing stale/zero).
        try {
            await refreshSelectedCustomerPointsForOrder(data);
        } catch (e) {
            console.warn('refreshSelectedCustomerPointsForOrder after createOrder failed', e);
        }
        return data;
    } catch (err) {
        showPopup('Lỗi kết nối khi tạo đơn hàng.', { type: 'error' });
    }
}

function showTransferQrModal(orderId, amount, token) {
    const modal = document.getElementById('transferQrModal');
    if (!modal) return;
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    document.getElementById('transferOrderId').textContent = orderId;
    document.getElementById('transferOrderIdSmall').textContent = orderId;
    document.getElementById('transferAmount').textContent = formatPrice(amount);

    // Ensure token is present (backend token preferred)
    let displayToken = token;
    if (displayToken) {
        const tokenEl = document.getElementById('transferPaymentToken');
        if (tokenEl) tokenEl.textContent = displayToken;
    } else {
        displayToken = 'SAMPLE-' + Math.random().toString(36).slice(2, 10).toUpperCase();
        const tokenEl = document.getElementById('transferPaymentToken');
        if (tokenEl) tokenEl.textContent = displayToken + ' (mã tượng trưng)';
    }

    const bankCode = 'VCB';
    const account = '1021209511';
    const accountName = 'UBIQUITY HUB';

    const payloadEl = document.getElementById('transferPayload');
    if (payloadEl) {
        payloadEl.textContent = `VietQR . ${bankCode} . ${account} . ${accountName} . ${formatPrice(amount)}`;
    }

    const bankLogos = {
        VCB: 'https://img.vietqr.io/image/vietcombank-1021209511-compact.jpg'
    };
    const logoUrl = bankLogos[bankCode];
    const logoEl = document.getElementById('transferBankLogo');
    if (logoEl) {
        if (logoUrl) {
            logoEl.src = logoUrl;
            logoEl.style.display = '';
        } else {
            logoEl.style.display = 'none';
        }
    }

    const qrContainer = document.getElementById('qrCodeContainer');

    const bankQuickId = 'VCB';
    const template = 'compact';
    const amountParam = Number.isFinite(Number(amount)) && amount > 0 ? `amount=${Math.round(amount)}` : '';
    const addInfoParam = `addInfo=${encodeURIComponent('Thanh toan don #' + orderId)}`;
    const accountNameParam = `accountName=${encodeURIComponent(accountName)}`;
    const qrQuicklinkBase = `https://img.vietqr.io/image/${bankQuickId}-${account}-${template}.png`;
    const qrImgUrl = qrQuicklinkBase + (amountParam || addInfoParam || accountNameParam ? `?${[amountParam, addInfoParam, accountNameParam].filter(Boolean).join('&')}` : '');

    if (qrContainer) {
        qrContainer.innerHTML = '';
        const img = document.createElement('img');
        img.alt = 'VietQR';
        img.width = 260;
        img.height = 260;
        img.style.maxWidth = '100%';
        img.style.borderRadius = '8px';
        img.src = qrImgUrl;

        img.onload = () => {
            qrContainer.innerHTML = '';
            qrContainer.appendChild(img);
        };

        img.onerror = async () => {
            qrContainer.innerHTML = '';
            try {
                const fallbackPayload = `VietQR|BANK:${bankCode}|ACC:${account}|NAME:${accountName}|AMOUNT:${formatPriceCompact(amount)}|ORDER:${orderId}|TOKEN:${displayToken}`;
                if (window.QRCode) {
                    new QRCode(qrContainer, { text: fallbackPayload, width: 260, height: 260 });
                    const inner = qrContainer.querySelector('img,canvas');
                    if (inner) inner.style.borderRadius = '8px';
                } else {
                    qrContainer.textContent = `Ma: ${displayToken}`;
                }
            } catch (e) {
                qrContainer.textContent = `Ma: ${displayToken}`;
            }
        };

        qrContainer.appendChild(img);
    }

    const downloadBtn = document.getElementById('downloadQrBtn');
    if (downloadBtn) {
        downloadBtn.onclick = async () => {
            try {
                const res = await fetch(qrImgUrl);
                if (!res.ok) throw new Error('Failed to download QR image');
                const blob = await res.blob();
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `vietqr-order-${orderId}.png`;
                document.body.appendChild(a);
                a.click();
                a.remove();
                URL.revokeObjectURL(url);
            } catch (e) {
                alert('Không thể tải mã QR.');
            }
        };
    }

    // Copy token handler
    const copyBtn = document.getElementById('copyTokenBtn');
    if (copyBtn) {
        copyBtn.onclick = async () => {
            try {
                await navigator.clipboard.writeText(displayToken);
                alert('Đã sao chép mã thanh toán');
            } catch (e) {
                alert('Không thể sao chép mã.');
            }
        };
    }
}

// Duplicate showTransferQrModal removed - using the enhanced implementation above.

async function payOrder(orderId) {
    try {
        const token = document.getElementById('transferPaymentToken')?.textContent || null;
        const body = { method: 'TRANSFER' };
        if (token) body.token = token;

        const res = await fetch(`${API_BASE}/orders/${orderId}/pay`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            },
            body: JSON.stringify(body)
        });
        if (!res.ok) {
            const text = await res.text();
            alert(text || 'Không thể xác nhận thanh toán.');
            return;
        }
        alert('Đã thanh toán chuyển khoản được xác nhận.');
        hideTransferQrModal();

        // Reset points after payment
        usePointsEnabled = false;
        pointsToUse = 0;
        updatePointsDisplayInfo();

        // After confirming transfer payment, clear items but keep selected customer
        clearCart(false);
        saveActiveInvoiceState();
        try {
            const cnameEl = document.getElementById('checkoutCustomerName');
            const cphoneEl = document.getElementById('checkoutCustomerPhone');
            const caddrEl = document.getElementById('checkoutCustomerAddress');
            if (cnameEl) cnameEl.value = '';
            if (cphoneEl) cphoneEl.value = '';
            if (caddrEl) caddrEl.value = '';
        } catch (e) {
            console.warn('Failed to clear checkout inputs after payOrder', e);
        }
        try {
            await persistCartStateNow();
        } catch (err) {
            console.warn('persistCartStateNow failed', err);
            queuePersistCartState();
        }
        // Refresh authoritative customer data after confirming payment so UI
        // immediately reflects the correct point balance.
        try {
            if (selectedCustomer && Number.isFinite(Number(selectedCustomer.id)) && Number(selectedCustomer.id) > 0) {
                await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
            }
        } catch (e) {
            console.warn('refreshCustomerDetailFromApi after payOrder failed', e);
        }
    } catch (err) {
        alert('Lỗi kết nối khi xác nhận thanh toán.');
    }
}

function hideTransferQrModal() {
    const modal = document.getElementById('transferQrModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

// Duplicate payOrder removed - using the token-aware implementation above.

// wire modal buttons
document.addEventListener('DOMContentLoaded', () => {
    const confirmBtn = document.getElementById('transferConfirmBtn');
    const closeBtn = document.getElementById('transferCloseBtn');
    const cancelBtn = document.getElementById('transferCancelBtn');
    if (confirmBtn) confirmBtn.addEventListener('click', async () => {
        const orderId = document.getElementById('transferOrderId').textContent;
        await payOrder(orderId);
    });
    if (closeBtn) closeBtn.addEventListener('click', hideTransferQrModal);
    if (cancelBtn) cancelBtn.addEventListener('click', hideTransferQrModal);
    // Delegate cart item remove clicks to avoid relying on inline onclick handlers
    try {
        const cartContainer = document.getElementById('cartItems');
        if (cartContainer) {
            cartContainer.addEventListener('click', (evt) => {
                const btn = evt.target && evt.target.closest ? evt.target.closest('.cart-item-remove') : null;
                if (!btn) return;
                evt.preventDefault();
                const row = btn.closest('.cart-row');
                if (!row) return;
                const idx = Array.prototype.indexOf.call(cartContainer.children, row);
                if (!Number.isFinite(idx) || idx < 0) return;
                if (row.classList.contains('return-item')) {
                    removeReturnItem(idx);
                } else {
                    removeFromCart(idx);
                }
            });
        }
    } catch (e) {
        console.warn('cart remove delegation failed', e);
    }
    // shipping fields toggle behavior
    // shipping toggle removed; checkout uses direct customer input fields now

    // Setup points modal
    setupUsePointsModal();
});

function setupUsePointsModal() {
    const usePointsBtn = document.getElementById('usePointsBtn');
    const usePointsModal = document.getElementById('usePointsModal');
    const closeBtn = document.getElementById('closeUsePointsModal');
    const cancelBtn = document.getElementById('cancelUsePointsBtn');
    const confirmBtn = document.getElementById('confirmUsePointsBtn');
    const usePointsInput = document.getElementById('usePointsInput');

    if (!usePointsBtn || !usePointsModal) return;

    // Open modal when button clicked
    usePointsBtn.addEventListener('click', () => {
        const currentPoints = getCustomerPoints(selectedCustomer) || 0;
        const tier = getEffectiveTier(selectedCustomer);
        const rate = TIER_DISCOUNT_BY_100[tier] || 10000;

        if (currentPoints < 100) {
            alert('Khách hàng cần có ít nhất 100 điểm để sử dụng!');
            return;
        }

        document.getElementById('usePointsCurrentPoints').textContent = formatCompactNumber(currentPoints);
        document.getElementById('usePointsRateInfo').textContent = formatPrice(rate);

        // Reset input
        usePointsInput.value = '';
        usePointsInput.focus();

        usePointsModal.classList.add('show');
        usePointsModal.setAttribute('aria-hidden', 'false');
    });

    // Close modal
    const closeModal = () => {
        usePointsModal.classList.remove('show');
        usePointsModal.setAttribute('aria-hidden', 'true');
        usePointsEnabled = false;
        pointsToUse = 0;
    };

    closeBtn?.addEventListener('click', closeModal);
    cancelBtn?.addEventListener('click', closeModal);

    // Update discount preview on input change
    usePointsInput?.addEventListener('input', () => {
        updatePointsDiscountPreview();
    });

    // Handle confirm
    confirmBtn?.addEventListener('click', () => {
        const pointsInput = parseInt(usePointsInput.value) || 0;
        const currentPoints = getCustomerPoints(selectedCustomer) || 0;

        if (pointsInput <= 0) {
            alert('Vui lòng nhập số điểm lớn hơn 0');
            return;
        }

        if (pointsInput > currentPoints) {
            alert('Số điểm sử dụng không được vượt quá điểm hiện có');
            return;
        }

        if (pointsInput % 100 !== 0) {
            alert('Số điểm phải là bội số của 100');
            return;
        }

        // Save points to use
        usePointsEnabled = true;
        pointsToUse = pointsInput;

        // Close modal without resetting flags
        usePointsModal.classList.remove('show');
        usePointsModal.setAttribute('aria-hidden', 'true');

        // Update checkout display
        updatePointsDisplayInfo();
        updateTotal();
    });

    // Close modal when clicking outside
    usePointsModal.addEventListener('click', (e) => {
        if (e.target === usePointsModal) {
            closeModal();
        }
    });
}

function updatePointsDiscountPreview() {
    const usePointsInput = document.getElementById('usePointsInput');
    const pointsInput = parseInt(usePointsInput.value) || 0;
    const tier = getEffectiveTier(selectedCustomer);
    const rate = TIER_DISCOUNT_BY_100[tier] || 10000;

    const stepsByPoints = Math.floor(pointsInput / 100);
    const cartTotal = Math.max(0, Number(getTotalAmount()) || 0);
    const stepsByTotal = rate > 0 ? Math.floor(cartTotal / rate) : 0;
    const appliedSteps = Math.max(0, Math.min(stepsByPoints, stepsByTotal));
    const discount = appliedSteps * rate;

    const discountEl = document.getElementById('usePointsDiscountPreview');
    if (discountEl) {
        discountEl.textContent = formatPrice(discount);
    }
}

function updatePointsDisplayInfo() {
    const pointsUsedInfo = document.getElementById('pointsUsedInfo');
    const checkoutPointsUsed = document.getElementById('checkoutPointsUsed');
    const checkoutPointsDiscount = document.getElementById('checkoutPointsDiscount');

    const appliedSummary = getAppliedMemberDiscountSummary(getTotalAmount());
    if (usePointsEnabled && pointsToUse > 0 && appliedSummary.pointsUsed > 0) {
        if (pointsUsedInfo) {
            pointsUsedInfo.style.display = 'block';
            checkoutPointsUsed.textContent = formatCompactNumber(appliedSummary.pointsUsed);
            if (checkoutPointsDiscount) {
                checkoutPointsDiscount.textContent = formatPrice(appliedSummary.discount);
            }
        }
    } else {
        if (pointsUsedInfo) {
            pointsUsedInfo.style.display = 'none';
        }
    }
}

// Reload promotions khi quay lại trang để cập nhật trạng thái active/inactive
document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
        console.log('[visibilitychange] Page visible, reloading promotions...');
        loadPromotionIndex(true); // forceRefresh = true
    }
});

// Auto-refresh promotions mỗi 30 giây để sync với owner changes
setInterval(() => {
    console.log('[auto-refresh] Reloading promotions...');
    loadPromotionIndex(true);
}, 30000); // 30 seconds

function setupInvoiceModal() {
    const modal = document.getElementById('invoiceModal');
    const closeBtn = document.getElementById('closeInvoiceModal');
    const footerCloseBtn = document.getElementById('closeInvoiceBtn');
    const printBtn = document.getElementById('printInvoiceBtn');
    const printAltBtn = document.getElementById('printInvoiceBtnAlt');
    if (!modal) return;

    const close = () => closeInvoiceModal();
    closeBtn?.addEventListener('click', close);
    footerCloseBtn?.addEventListener('click', close);
    printBtn?.addEventListener('click', () => printInvoiceReceipt());
    printAltBtn?.addEventListener('click', () => printInvoiceReceipt());
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            close();
        }
    });
}

async function updateInvoiceReceipt(receiptData) {
    if (!receiptData) return;
    const promoMap = await loadPromotionIndex();
    const itemsEl = document.getElementById('invoiceItems');
    if (itemsEl) {
        itemsEl.innerHTML = receiptData.items.map(item => `
            <div class="receipt-item">
                <span class="name">
                    ${escapeHtml(item.name)}
                    ${renderReceiptPromo(item.productId, promoMap)}
                </span>
                <span>${item.quantity}</span>
                <span>${formatPrice(item.price)}</span>
                <span>${formatPrice(item.total)}</span>
            </div>
        `).join('');
    }

    setText('invoiceCode', receiptData.invoiceNumber || '-');
    setText('invoiceDate', formatDateTime(receiptData.createdAt));
    setText('invoiceCashier', receiptData.cashier || '-');
    setText('invoiceCustomer', receiptData.customerName || 'Khách lẻ');
    setText('invoicePhone', receiptData.customerPhone || '-');
    setText('invoiceMethod', mapPaymentMethod(receiptData.paymentMethod));
    setText('invoiceSubtotal', formatPrice(receiptData.subtotal || 0));
    setText('invoiceMemberDiscount', formatPrice(receiptData.memberDiscount || 0));
    setText('invoicePointsUsed', formatCompactNumber(receiptData.pointsUsed || 0));
    setText('invoiceTotal', formatPrice(receiptData.total || 0));
    setText('invoiceCashReceived', formatPrice(receiptData.cashReceived || 0));
    setText('invoiceChange', formatPrice(receiptData.change || 0));

    const noteWrap = document.getElementById('invoiceNoteWrap');
    const noteValue = receiptData.note || '';
    setText('invoiceNote', noteValue || '-');
    if (noteWrap) {
        noteWrap.style.display = noteValue ? 'grid' : 'none';
    }
}

async function openInvoiceModal(receiptData) {
    const modal = document.getElementById('invoiceModal');
    if (!modal || !receiptData) return;

    await updateInvoiceReceipt(receiptData);
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function renderReceiptPromo(productId, promoMap) {
    if (!productId || !promoMap) return '';
    const info = promoMap.get(productId);
    if (!info) return '';
    return `<small class="receipt-promo">KM: ${escapeHtml(info.label)}</small>`;
}

function closeInvoiceModal() {
    const modal = document.getElementById('invoiceModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = value;
    }
}

function buildReceiptData(orderResponse, options = {}) {
    const { usePoints = false } = options;
    const subtotal = cart.reduce((sum, item) => sum + (item.productPrice * item.quantity), 0);
    const memberSummary = getMemberDiscountForTotal(subtotal);
    const memberDiscount = usePoints ? memberSummary.discount : 0;
    const total = Math.max(0, subtotal - memberDiscount);
    const cashReceived = parseInt(document.getElementById('cashReceivedInput')?.value, 10) || 0;
    const change = currentPaymentMethod === 'CASH' ? Math.max(0, cashReceived - total) : 0;

    let invoiceNumber = orderResponse?.invoiceNumber || '';
    if (!invoiceNumber && orderResponse?.orderId) {
        invoiceNumber = `HD-${orderResponse.orderId}`;
    }
    if (!invoiceNumber) {
        invoiceNumber = '-';
    }

    // Split items with promo into separate lines (promo qty vs regular qty)
    const receiptItems = [];
    for (const item of cart) {
        // Check if item has quota-limited promotion
        if (item.promoId && promotionIndex) {
            const promoInfo = promotionIndex.get(item.productId);
            if (promoInfo?.promo) {
                const maxQty = Number(promoInfo.promo.maxQuantity);
                const usedQty = Math.max(0, Number(promoInfo.promo.usedQuantity || 0));

                if (Number.isFinite(maxQty) && maxQty > 0) {
                    const remaining = Math.max(0, maxQty - usedQty);
                    const promoQty = Math.min(item.quantity, remaining);
                    const regularQty = item.quantity - promoQty;

                    // Get base price from product
                    const product = products.find(p => p.id === item.productId);
                    const basePrice = product ? Number(product.price) : item.productPrice;
                    const promoPrice = getPromoPrice(basePrice, promoInfo.promo, 1, 0);

                    console.log('[buildReceiptData] Split item:', {
                        name: item.productName,
                        totalQty: item.quantity,
                        promoQty,
                        regularQty,
                        basePrice,
                        promoPrice,
                        promoCode: promoInfo.promo.code
                    });

                    // Add promo portion
                    if (promoQty > 0) {
                        receiptItems.push({
                            productId: item.productId,
                            name: `${item.productName} (KM)`,
                            quantity: promoQty,
                            price: promoPrice,
                            total: promoQty * promoPrice,
                            isPromo: true
                        });
                    }

                    // Add regular portion (if any)
                    if (regularQty > 0) {
                        receiptItems.push({
                            productId: item.productId,
                            name: item.productName,
                            quantity: regularQty,
                            price: basePrice,
                            total: regularQty * basePrice,
                            isPromo: false
                        });
                    }

                    continue;
                }
            }
        }

        // No quota split needed - add as is
        receiptItems.push({
            productId: item.productId,
            name: item.productName || '-',
            quantity: item.quantity || 0,
            price: item.productPrice || 0,
            total: (item.productPrice || 0) * (item.quantity || 0),
            isPromo: item.promoId ? true : false
        });
    }

    return {
        invoiceNumber,
        createdAt: new Date(),
        customerName: selectedCustomer?.name || 'Khách lẻ',
        customerPhone: selectedCustomer?.phone || '-',
        cashier: selectedEmployee?.name || sessionStorage.getItem('username') || 'Nhân viên',
        paymentMethod: currentPaymentMethod,
        note: document.getElementById('paymentNote')?.value?.trim() || '',
        subtotal,
        memberDiscount,
        pointsUsed: usePoints ? memberSummary.pointsUsed : 0,
        total,
        cashReceived,
        change,
        items: receiptItems
    };
}

function formatDateTime(value) {
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) return '-';
    return date.toLocaleString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatSupportTime(value) {
    const date = value ? new Date(value) : new Date();
    if (Number.isNaN(date.getTime())) {
        return '';
    }
    return date.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getValidSupportOwnerId() {
    if (supportOwnerId === null || supportOwnerId === undefined || supportOwnerId === '') {
        return null;
    }
    const id = Number(supportOwnerId);
    if (!Number.isFinite(id) || id <= 0) {
        return null;
    }
    return id;
}

async function resolveOwnerContact() {
    const token = sessionStorage.getItem('accessToken') || '';
    const res = await fetch(`${API_BASE}/users`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) return;
    const users = await res.json();
    const owner = (users || []).find((u) => {
        const role = typeof u?.role === 'string'
            ? u.role
            : (u?.role?.name || '');
        return String(role).toUpperCase() === 'OWNER';
    });
    if (!owner) return;

    supportOwnerId = Number(owner.id);
    supportOwnerName = owner.fullName || owner.username || 'Owner';
}

function renderSupportMessages(messages) {
    const box = document.getElementById('contactSupportMessages');
    if (!box) return;
    const currentUserId = Number(sessionStorage.getItem('userId'));

    box.innerHTML = (messages || []).map((m) => {
        const isMe = Number(m.senderId) === currentUserId;
        const cls = isMe ? 'me' : 'them';
        const sender = escapeHtml(isMe ? 'Bạn' : 'Chăm sóc khách hàng');
        const content = escapeHtml(m.content || '');
        const time = formatSupportTime(m.createdAt);
        return `
            <div class="contact-msg ${cls}">
                <div class="sender">${sender}</div>
                <div>${content}</div>
                <div class="time">${time}</div>
            </div>
        `;
    }).join('');

    box.scrollTop = box.scrollHeight;
}

async function markSupportMessagesAsRead() {
    const currentUserId = Number(sessionStorage.getItem('userId'));
    const ownerId = getValidSupportOwnerId();
    if (!Number.isFinite(currentUserId) || !ownerId) return;

    try {
        const token = sessionStorage.getItem('accessToken') || '';
        await fetch(`${API_BASE}/messages/mark-as-read`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                senderId: ownerId,
                receiverId: currentUserId
            })
        });
    } catch (error) {
        console.error('Error marking messages as read:', error);
    }
}

async function loadSupportMessages() {
    const currentUserId = Number(sessionStorage.getItem('userId'));
    const ownerId = getValidSupportOwnerId();
    if (!Number.isFinite(currentUserId) || !ownerId) return;

    const token = sessionStorage.getItem('accessToken') || '';
    const res = await fetch(`${API_BASE}/messages/${currentUserId}/${ownerId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!res.ok) return;

    const data = await res.json();

    // Mark all messages as read FIRST
    await markSupportMessagesAsRead();

    // Then render and update badge
    renderSupportMessages(data || []);
    updateSupportUnreadBadge(0);
    lastMessageCount = 0;
}

async function sendSupportMessage() {
    const input = document.getElementById('contactSupportInput');
    const currentUserId = Number(sessionStorage.getItem('userId'));
    const currentUserName = sessionStorage.getItem('username') || 'Người dùng';
    if (!input) return;

    const content = (input.value || '').trim();
    if (!content) return;

    let ownerId = getValidSupportOwnerId();
    if (!ownerId) {
        await resolveOwnerContact();
        ownerId = getValidSupportOwnerId();
    }
    if (!Number.isFinite(currentUserId) || !ownerId) {
        showPopup('Không tìm thấy Owner để liên hệ.', { type: 'error' });
        return;
    }

    const token = sessionStorage.getItem('accessToken') || '';
    const res = await fetch(`${API_BASE}/messages`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
            senderId: currentUserId,
            receiverId: ownerId,
            senderName: currentUserName,
            receiverName: 'Chăm sóc khách hàng',
            content
        })
    });

    if (!res.ok) {
        const text = await res.text();
        showPopup(text || 'Không gửi được tin nhắn.', { type: 'error' });
        return;
    }

    input.value = '';
    await loadSupportMessages();
}

function clearSupportPolling() {
    if (supportPollTimer) {
        clearInterval(supportPollTimer);
        supportPollTimer = null;
    }
}

function updateSupportUnreadBadge(count) {
    const btn = document.getElementById('contactSupportBtn');
    if (!btn) return;

    supportUnreadCount = count;

    // Remove old badge if exists
    const oldBadge = btn.querySelector('.unread-badge');
    if (oldBadge) oldBadge.remove();

    // Add new badge if count > 0
    if (count > 0) {
        const badge = document.createElement('span');
        badge.className = 'unread-badge';
        badge.textContent = count > 99 ? '99+' : count;
        badge.style.cssText = `
            position: absolute;
            top: -5px;
            right: -5px;
            background: #ef4444;
            color: white;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 700;
            z-index: 999;
        `;
        btn.appendChild(badge);
    }
}

function showToastNotification(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) {
        console.warn('Toast container not found');
        return;
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    toast.style.cssText = `
        margin: 8px;
        padding: 12px 16px;
        border-radius: 8px;
        background: ${type === 'error' ? '#fee' : type === 'success' ? '#efe' : '#e0f2fe'};
        color: ${type === 'error' ? '#991b1b' : type === 'success' ? '#166534' : '#0c4a6e'};
        border: 1px solid ${type === 'error' ? '#fca5a5' : type === 'success' ? '#86efac' : '#7dd3fc'};
        animation: slideIn 0.3s ease-out;
        font-size: 14px;
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

async function openSupportModal() {
    const modal = document.getElementById('contactSupportModal');
    const meta = document.getElementById('contactSupportMeta');
    if (!modal) return;

    let ownerId = getValidSupportOwnerId();
    if (!ownerId) {
        await resolveOwnerContact();
        ownerId = getValidSupportOwnerId();
    }

    if (!ownerId) {
        showPopup('Chưa có tài khoản Owner để liên hệ.', { type: 'error' });
        return;
    }

    // Reset badge immediately
    updateSupportUnreadBadge(0);
    lastMessageCount = 0;

    if (meta) {
        meta.textContent = 'Đang chat với: Chăm sóc khách hàng';
    }

    await loadSupportMessages();
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');

    clearSupportPolling();
    supportPollTimer = setInterval(() => {
        loadSupportMessages();
    }, 5000);

    // Start global polling for unread messages (even when modal is closed)
    startGlobalUnreadPolling();
}

function closeSupportModal() {
    const modal = document.getElementById('contactSupportModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    clearSupportPolling();

    // Mark as read one more time before closing to be sure
    markSupportMessagesAsRead().then(() => {
        // Reset badge after marking
        updateSupportUnreadBadge(0);
        lastMessageCount = 0;
    });
}

function setupContactSupport() {
    const openBtn = document.getElementById('contactSupportBtn');
    const closeBtn = document.getElementById('contactSupportCloseBtn');
    const sendBtn = document.getElementById('contactSupportSendBtn');
    const input = document.getElementById('contactSupportInput');
    const modal = document.getElementById('contactSupportModal');

    openBtn?.addEventListener('click', openSupportModal);
    closeBtn?.addEventListener('click', closeSupportModal);
    sendBtn?.addEventListener('click', sendSupportMessage);
    input?.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            sendSupportMessage();
        }
    });
    modal?.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeSupportModal();
        }
    });

    // Mark all old messages as read on page load to avoid showing old unread count
    const userId = Number(sessionStorage.getItem('userId'));
    if (Number.isFinite(userId)) {
        resolveOwnerContact().then(() => {
            const ownerId = getValidSupportOwnerId();
            if (ownerId) {
                markSupportMessagesAsRead().catch(() => { });
            }
        }).catch(() => { });
    }
}

function startGlobalUnreadPolling() {
    // Only start polling if user is logged in
    const currentUserId = Number(sessionStorage.getItem('userId'));
    const token = sessionStorage.getItem('accessToken') || '';

    if (!Number.isFinite(currentUserId) || !token) {
        return; // User not logged in yet
    }

    if (globalUnreadPollTimer) clearInterval(globalUnreadPollTimer);

    globalUnreadPollTimer = setInterval(async () => {
        const userId = Number(sessionStorage.getItem('userId'));
        const authToken = sessionStorage.getItem('accessToken') || '';
        let ownerId = getValidSupportOwnerId();

        if (!ownerId) {
            await resolveOwnerContact().catch(() => { });
            ownerId = getValidSupportOwnerId();
        }

        if (Number.isFinite(userId) && ownerId && authToken) {
            try {
                const res = await fetch(`${API_BASE}/messages/${userId}/${ownerId}`, {
                    headers: { 'Authorization': `Bearer ${authToken}` }
                });

                if (res.status === 401) {
                    // User logged out, stop polling
                    clearInterval(globalUnreadPollTimer);
                    globalUnreadPollTimer = null;
                    return;
                }

                if (res.ok) {
                    const data = await res.json();
                    const unreadCount = (data || []).filter(m => m.isRead !== true && Number(m.senderId) === ownerId).length;

                    // Only update badge when modal is CLOSED
                    const modalEl = document.getElementById('contactSupportModal');
                    if (modalEl && !modalEl.classList.contains('show')) {
                        // Show badge với số tin nhắn chưa đọc
                        updateSupportUnreadBadge(unreadCount);

                        // Show notification for new messages
                        if (unreadCount > 0 && unreadCount > lastMessageCount) {
                            showToastNotification('Chăm sóc khách hàng đã gửi tin nhắn', 'info');
                        }

                        lastMessageCount = unreadCount;
                    }
                }
            } catch (err) {
                console.error('Error polling unread messages:', err);
            }
        }
    }, 3000); // Poll every 3 seconds
}

function mapPaymentMethod(method) {
    switch (method) {
        case 'CASH':
            return 'Thanh toán khi nhận hàng';
        case 'TRANSFER':
            return 'Thanh toán trực tuyến';
        case 'CARD':
            return 'Thẻ ngân hàng';
        default:
            return method || '-';
    }
}

async function loadPromotionIndex(forceRefresh = false) {
    if (promotionIndex && !forceRefresh) {
        return promotionIndex;
    }

    console.log('[loadPromotionIndex] Loading promotions...', { forceRefresh });

    const headers = { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` };

    try {
        const promoPromise = fetch(`${API_BASE}/v1/promotions`, { headers })
            .then(res => (res.ok ? res.json() : []));
        const productPromise = (products && products.length > 0)
            ? Promise.resolve(products)
            : fetch(`${API_BASE}/inventory/shelves`, { headers })
                .then(res => {
                    if (!res.ok) return [];
                    return res.json();
                })
                .then(shelvesData => shelvesData.map(shelf => ({
                    id: shelf.productId,
                    name: shelf.productName,
                    code: shelf.productCode,
                    barcode: shelf.productCode,
                    price: shelf.price || 0,
                    stock: shelf.quantity,
                    categoryId: shelf.categoryId,
                    categoryName: shelf.categoryName || shelf.category || shelf.groupName || '',
                    unit: shelf.unit || 'cái',
                    status: 'active'
                })));

        const [promoList, productList] = await Promise.all([promoPromise, productPromise]);

        console.log('[loadPromotionIndex] Loaded promotions:', promoList.length);

        const activePromos = (promoList || []).filter(isPromotionActive);

        // Luu t?t c? promotions cho AI combo
        allPromotions = activePromos;
        console.log('[loadPromotionIndex] Saved allPromotions for AI:', allPromotions.length);

        promotionIndex = buildPromotionIndex(activePromos, productList || []);

        console.log('[loadPromotionIndex] Built index size:', promotionIndex.size);

        return promotionIndex;
    } catch (err) {
        console.error('[loadPromotionIndex] Error:', err);
        promotionIndex = new Map();
        allPromotions = [];
        return promotionIndex;
    }
}

function buildPromotionIndex(promos, productList) {
    const productMap = new Map((productList || []).map((p) => [p.id, p]));
    const targetMap = new Map();

    // Filter: exclude promotions with exhausted quota
    const activePromos = (promos || []).filter(promo => {
        const max = Number(promo?.maxQuantity);
        const used = Math.max(0, Number(promo?.usedQuantity || 0));

        // If no max quantity, always active
        if (!Number.isFinite(max) || max <= 0) {
            return true;
        }

        // If used >= max, promote is exhausted
        if (used >= max) {
            console.log(`[buildPromotionIndex] Filtering out exhausted promotion: ${promo.code} (${used}/${max})`);
            return false;
        }

        return true;
    });

    activePromos.forEach((promo) => {
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

    // Prioritize BUNDLE promotions since they depend on quantity
    const bundlePromo = (promos || []).find(p => normalizeDiscountType(p.discountType) === 'BUNDLE');
    if (bundlePromo) {
        console.log('[selectBestPromotion] Found BUNDLE promo, prioritizing it');
        return { promo: bundlePromo, price: basePrice }; // Return base price, will recalc with quantity later
    }

    // For other promo types, select best price
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

function getPromoPrice(basePrice, promo, quantity = 1, currentCartQty = 0) {
    console.log('[getPromoPrice] FULL INPUT:', JSON.stringify({
        basePrice,
        promoType: promo?.discountType,
        promoCode: promo?.code,
        promoName: promo?.name,
        maxQuantity: promo?.maxQuantity,
        usedQuantity: promo?.usedQuantity,
        currentCartQty: currentCartQty,
        bundleItems: promo?.bundleItems,
        quantity
    }, null, 2));

    if (!Number.isFinite(basePrice) || !promo) {
        console.log('[getPromoPrice] Invalid input - basePrice:', basePrice, 'promo:', promo);
        return NaN;
    }

    const qty = Math.max(1, Number(quantity) || 1);

    // Check quota first
    const maxQty = Number(promo.maxQuantity);
    const backendUsedQty = Math.max(0, Number(promo.usedQuantity || 0));
    // Adjust usedQty to account for items already in cart
    const usedQty = backendUsedQty + Math.max(0, Number(currentCartQty || 0));
    let appliedQty = qty;  // Quantity that gets promotion
    let regularQty = 0;    // Quantity that gets base price

    if (Number.isFinite(maxQty) && maxQty > 0) {
        const remaining = Math.max(0, maxQty - usedQty);
        appliedQty = Math.min(qty, remaining);
        regularQty = qty - appliedQty;
        console.log('[getPromoPrice] Quota check - max:', maxQty, 'backendUsed:', backendUsedQty, 'cartQty:', currentCartQty, 'totalUsed:', usedQty, 'remaining:', remaining, 'appliedQty:', appliedQty, 'regularQty:', regularQty);
    }

    // If no promotion can be applied
    if (appliedQty === 0) {
        console.log('[getPromoPrice] No quota remaining, returning basePrice:', basePrice);
        return basePrice;
    }

    const value = Number(promo.discountValue);
    let discountedUnitPrice = basePrice;  // Price per unit with discount

    switch (normalizeDiscountType(promo.discountType)) {
        case 'PERCENT':
            if (!Number.isFinite(value)) return NaN;
            discountedUnitPrice = Math.max(0, basePrice * (1 - value / 100));
            break;
        case 'FIXED':
            if (!Number.isFinite(value)) return NaN;
            discountedUnitPrice = Math.max(0, basePrice - value);
            break;
        case 'BUNDLE':
            // Bundle logic: "Mua X tặng Y"
            console.log('[getPromoPrice BUNDLE] bundleItems:', promo.bundleItems);

            if (!promo.bundleItems || promo.bundleItems.length === 0) {
                console.log('[getPromoPrice BUNDLE] No bundleItems, returning basePrice:', basePrice);
                return basePrice;
            }
            // Get first bundle item (assume 1 bundle per promo)
            const bundle = promo.bundleItems[0];
            const mainQty = Number(bundle.mainQuantity) || 3;  // Mua 3
            const giftQty = Number(bundle.giftQuantity) || 1;  // Tặng 1
            const setSize = mainQty + giftQty;  // 1 set = 4 chai

            console.log('[getPromoPrice BUNDLE] bundle config:', { mainQty, giftQty, setSize, appliedQty, regularQty });

            // Only calculate bundle for appliedQty
            const completeSets = Math.floor(appliedQty / setSize);
            const remainingAfterSets = appliedQty % setSize;

            const bundlePrice = (completeSets * mainQty * basePrice) + (remainingAfterSets * basePrice) + (regularQty * basePrice);
            const finalUnitPrice = bundlePrice / qty;

            console.log('[getPromoPrice BUNDLE] calculation:', {
                completeSets,
                remainingAfterSets,
                regularQty,
                bundlePrice,
                finalUnitPrice
            });

            return Math.max(0, finalUnitPrice);
        case 'FREE_GIFT':
            discountedUnitPrice = basePrice;
            break;
        default:
            return NaN;
    }

    // Mixed price calculation: appliedQty with discount + regularQty at base price
    const totalPrice = (appliedQty * discountedUnitPrice) + (regularQty * basePrice);
    const finalUnitPrice = totalPrice / qty;

    console.log('[getPromoPrice] Final calculation - appliedQty:', appliedQty, 'regularQty:', regularQty, 'discountedUnit:', discountedUnitPrice, 'totalPrice:', totalPrice, 'finalUnit:', finalUnitPrice);

    return Math.max(0, finalUnitPrice);
}

function normalizeDiscountType(value) {
    if (!value) return value;
    if (value === 'FIXED_AMOUNT') return 'FIXED';
    if (value === 'FREE_GIFT') return 'BUNDLE';
    return value;
}

function formatPromotionLabel(promo) {
    if (!promo) return 'Khuyến mãi';
    const value = Number(promo.discountValue);
    const type = normalizeDiscountType(promo.discountType);
    if (type === 'PERCENT' && Number.isFinite(value)) {
        return `Giảm ${value}%`;
    }
    if (type === 'FIXED' && Number.isFinite(value)) {
        return `Giảm ${formatPrice(value)}`;
    }
    if (type === 'BUNDLE') {
        const bundles = promo.bundleItems || [];
        if (bundles.length > 0) {
            const firstBundle = bundles[0];
            const mainQty = firstBundle.mainQuantity || firstBundle.main_quantity || 1;
            const giftQty = firstBundle.giftQuantity || firstBundle.gift_quantity || 1;
            return `Mua ${mainQty} tặng ${giftQty}`;
        }
        return 'Combo';
    }
    if (type === 'FREE_GIFT') {
        return 'Tặng kèm';
    }
    return promo.discountType || 'Khuyến mãi';
}

function isPromotionActive(promo) {
    if (!promo) return false;
    if (promo.active === false) return false;
    const maxQty = Number(promo.maxQuantity);
    if (Number.isFinite(maxQty) && maxQty > 0) {
        const usedQty = Math.max(0, Number(promo.usedQuantity || 0));
        if (usedQty >= maxQty) return false;
    }
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


function ensureEmptyStateBrand(target) {
    const emptyState = target || document.getElementById('emptyState');
    if (!emptyState || emptyState.querySelector('.empty-brand')) return;
    const brand = document.createElement('div');
    brand.className = 'empty-brand';
    brand.innerHTML = `
        <div class="empty-brand-badge">BF</div>
        <div class="empty-brand-text">
            <div class="empty-brand-title">BizFlow</div>
            <div class="empty-brand-sub">Smart Retail POS</div>
        </div>
    `;
    emptyState.prepend(brand);
}

function renderCart() {
    const cartContainer = document.getElementById('cartItems');
    const emptyState = document.getElementById('emptyState');
    ensureEmptyStateBrand(emptyState);
    if (cart.length === 0) {
        if (cartContainer) {
            cartContainer.innerHTML = '';
        }
        if (emptyState) {
            emptyState.style.display = 'grid';
        }
        toggleEmptyState(true);
        // Ensure checkout button state reflects empty cart
        try { updateCheckoutButtonState(); } catch (e) { }
        return;
    }

    toggleEmptyState(false);
    if (emptyState) {
        emptyState.style.display = 'none';
    }
    cartContainer.innerHTML = cart.map((item, idx) => {
        if (item.isReturnItem) {
            return `
            <div class="cart-row return-item">
                <span>${idx + 1}</span>
                <span>${item.productCode || '-'}</span>
                <span class="cart-name">${item.productName}</span>
                <span class="cart-qty">
                    <input type="number" class="qty-input" value="${item.quantity}" disabled>
                </span>
                <span>${item.unit || '-'}</span>
                <span>${formatPrice(item.productPrice)}</span>
                <span>${formatPrice(item.productPrice * item.quantity)}</span>
                <div class="cart-item-actions">
                    <span class="cart-item-locked">Đổi</span>
                    <button class="cart-item-remove">×</button>
                </div>
            </div>
        `;
        }

        // Kiểm tra nếu là quà tặng
        if (item.isFreeGift) {
            // Tự động lookup tên nếu là Unknown
            let displayName = item.productName;
            if (!displayName || displayName === 'Unknown') {
                const product = products.find(p => Number(p.id) === Number(item.productId) || Number(p.productId) === Number(item.productId));
                if (product) {
                    displayName = product.name || product.productName || product.product_name || displayName;
                    // Cập nhật lại tên trong cart
                    item.productName = displayName;
                } else {
                    displayName = `Sản phẩm #${item.productId}`;
                }
            }

            return `
            <div class="cart-row gift-item">
                <span>${idx + 1}</span>
                <span class="gift-badge">🎁 TẶNG</span>
                <span class="cart-name">${displayName}</span>
                <span class="cart-qty">
                    <input type="number" class="qty-input" value="${item.quantity}" disabled>
                </span>
                <span>${item.unit || '-'}</span>
                <span style="text-decoration: line-through; color: #999;">${formatPrice(item.productPrice)}</span>
                <span style="color: #ff6b9d; font-weight: 600;">0d</span>
                <span class="gift-label" style="grid-column: span 2; text-align: right; color: #ff6b9d; font-size: 12px; font-style: italic;">${item.promoLabel || 'Quà tặng'}</span>
            </div>
        `;
        }

        return `
        <div class="cart-row">
            <span>${idx + 1}</span>
            <span>${item.productCode || '-'}</span>
            <span class="cart-name">${item.productName}</span>
            <span class="cart-qty ${Number(item.stock) <= 0 ? 'is-out' : ''}">
                <input type="number" class="qty-input" value="${item.quantity}" onchange="setQty(${idx}, this.value)">
            </span>
            <span>${item.unit || '-'}</span>
            <span>${formatPrice(item.productPrice)}</span>
            <span>${formatPrice(item.productPrice * item.quantity)}</span>
            <button class="cart-item-remove">×</button>
        </div>
    `;
    }).join('');
    // Update checkout button state whenever cart is re-rendered
    try { updateCheckoutButtonState(); } catch (e) { }
}

function updateQty(idx, change) {
    if (cart[idx] && !cart[idx].isReturnItem) {
        const oldQty = cart[idx].quantity;
        const newQty = Math.max(1, oldQty + change);

        // Check promotion quota if quantity is increasing
        if (newQty > oldQty && cart[idx].promoId) {
            const promoInfo = promotionIndex.get(cart[idx].productId);
            if (promoInfo?.promo) {
                const maxQty = Number(promoInfo.promo.maxQuantity);
                const usedQty = Math.max(0, Number(promoInfo.promo.usedQuantity || 0));
                const effectiveUsedQty = usedQty + oldQty;  // Account for items in cart

                if (Number.isFinite(maxQty) && maxQty > 0) {
                    const remaining = Math.max(0, maxQty - effectiveUsedQty);

                    if ((newQty - oldQty) > remaining) {
                        // Promotion quota exceeded - ask for confirmation
                        const promoName = promoInfo.promo.name || promoInfo.promo.code || 'Khuyến mãi';
                        const confirmMessage = `Khuyến mãi "${promoName}" chỉ còn ${remaining} sản phẩm. Bạn muốn tăng lên ${newQty} sản phẩm không?\n\nSố lượng vượt mức sẽ được tính giá gốc.`;

                        if (!confirm(confirmMessage)) {
                            console.log('[updateQty] User cancelled - promotion quota exceeded');
                            return;
                        }
                        console.log('[updateQty] User confirmed - proceeding with mixed pricing');
                    }
                }
            }
        }

        // Recalculate price if this item has any promotion (especially with quota)
        const item = cart[idx];
        if (item.promoId) {
            const promoInfo = promotionIndex.get(item.productId);
            if (promoInfo?.promo) {
                // Get base price from product
                const product = products.find(p => p.id === item.productId);
                const basePrice = product ? Number(product.price) : item.productPrice;
                const quantityChange = newQty - oldQty;

                console.log('[updateQty] Recalculating price:', { productId: item.productId, basePrice, oldQty, newQty, quantityChange, promoCode: promoInfo.promo.code });

                // Calculate price for the quantity being added in this change
                const newItemAvgPrice = getPromoPrice(basePrice, promoInfo.promo, quantityChange, oldQty);
                // Update total price: old items at old price + new items at new price
                const oldTotalPrice = oldQty * item.productPrice;
                const newTotalPrice = oldTotalPrice + (quantityChange * newItemAvgPrice);
                item.productPrice = newTotalPrice / newQty;

                console.log('[updateQty] Price updated - oldAvg:', oldTotalPrice / oldQty, 'newItemAvg:', newItemAvgPrice, 'finalAvg:', item.productPrice);
            }
        }

        cart[idx].quantity = newQty;
        renderCart();
        updateTotal();
        queuePersistCartState();
    }
}

function setQty(idx, value) {
    const qty = parseInt(value, 10) || 1;
    if (cart[idx] && !cart[idx].isReturnItem) {
        const oldQty = cart[idx].quantity;
        const newQty = Math.max(1, qty);

        // Check promotion quota if quantity is increasing
        if (newQty > oldQty && cart[idx].promoId) {
            const promoInfo = promotionIndex.get(cart[idx].productId);
            if (promoInfo?.promo) {
                const maxQty = Number(promoInfo.promo.maxQuantity);
                const usedQty = Math.max(0, Number(promoInfo.promo.usedQuantity || 0));
                const effectiveUsedQty = usedQty + oldQty;  // Account for items in cart

                if (Number.isFinite(maxQty) && maxQty > 0) {
                    const remaining = Math.max(0, maxQty - effectiveUsedQty);

                    if ((newQty - oldQty) > remaining) {
                        // Promotion quota exceeded - ask for confirmation
                        const promoName = promoInfo.promo.name || promoInfo.promo.code || 'Khuyến mãi';
                        const confirmMessage = `Khuyến mãi "${promoName}" chỉ còn ${remaining} sản phẩm. Bạn muốn thay đổi lên ${newQty} sản phẩm không?\n\nSố lượng vượt mức sẽ được tính giá gốc.`;

                        if (!confirm(confirmMessage)) {
                            console.log('[setQty] User cancelled - promotion quota exceeded');
                            return;
                        }
                        console.log('[setQty] User confirmed - proceeding with mixed pricing');
                    }
                }
            }
        }

        // Recalculate price if this item has any promotion (especially with quota)
        const item = cart[idx];
        if (item.promoId) {
            const promoInfo = promotionIndex.get(item.productId);
            if (promoInfo?.promo) {
                // Get base price from product
                const product = products.find(p => p.id === item.productId);
                const basePrice = product ? Number(product.price) : item.productPrice;
                const quantityChange = newQty - oldQty;

                if (quantityChange !== 0) {
                    console.log('[setQty] Recalculating price:', { productId: item.productId, basePrice, oldQty, newQty, quantityChange, promoCode: promoInfo.promo.code });

                    // Calculate price for the quantity being added/removed in this change
                    const newItemAvgPrice = getPromoPrice(basePrice, promoInfo.promo, Math.abs(quantityChange), oldQty);
                    // Update total price: old items at old price + new items at new price
                    const oldTotalPrice = oldQty * item.productPrice;
                    const newTotalPrice = oldTotalPrice + (quantityChange * newItemAvgPrice);
                    item.productPrice = newTotalPrice / newQty;

                    console.log('[setQty] Price updated - oldAvg:', oldTotalPrice / oldQty, 'changeAvg:', newItemAvgPrice, 'finalAvg:', item.productPrice);
                }
            }
        }

        cart[idx].quantity = newQty;

        // Nếu giảm số lượng, kiểm tra và xóa quà tặng không hợp lệ ngay
        if (newQty < oldQty) {
            console.log('[setQty] Quantity decreased, checking gifts...');
            // Tạm thời đánh dấu cần kiểm tra lại
            setTimeout(() => {
                // Phân tích lại và AI sẽ tự động xóa quà không hợp lệ
                analyzeCartForCombo();
            }, 50);
        }

        renderCart();
        updateTotal();

        // Phân tích lại combo sau khi thay đổi số lượng
        setTimeout(() => analyzeCartForCombo(), 100);
        queuePersistCartState();
    }
}

function removeFromCart(idx) {
    if (cart[idx]?.isReturnItem) return;

    const removedItem = cart[idx];
    const removedProductId = removedItem.productId;

    // Xóa sản phẩm
    cart.splice(idx, 1);

    // Kiểm tra xem còn sản phẩm này trong giỏ không
    const hasRemainingProduct = cart.some(item =>
        item.productId === removedProductId && !item.isFreeGift
    );

    // Nếu không còn sản phẩm này, xóa luôn quà tặng liên quan
    if (!hasRemainingProduct) {
        // Tìm và xóa các quà tặng có thể liên quan đến sản phẩm này
        const giftsToRemove = [];
        cart.forEach((item, i) => {
            if (item.isFreeGift) {
                // Kiểm tra xem quà này có liên quan đến sản phẩm bị xóa không
                // Bằng cách kiểm tra xem còn đủ điều kiện không
                giftsToRemove.push(i);
            }
        });

        // Xóa các quà từ cuối lên để không bị lỗi index
        giftsToRemove.sort((a, b) => b - a).forEach(i => {
            const giftName = cart[i].productName;
            cart.splice(i, 1);
            console.log('[removeFromCart] ❌ Removed gift:', giftName);
        });

        if (giftsToRemove.length > 0) {
            ComboPromotionUI.showNotification(
                `⚠️ Đã xóa ${giftsToRemove.length} quà tặng (không đủ điều kiện)`,
                'warning'
            );
        }
    }

    renderCart();
    updateTotal();

    // Phân tích lại combo sau khi xóa sản phẩm
    setTimeout(() => analyzeCartForCombo(), 100);
    queuePersistCartState();
}

function removeReturnItem(idx) {
    if (!cart[idx]?.isReturnItem) return;
    cart.splice(idx, 1);
    renderCart();
    updateTotal();
    queuePersistCartState();
}

function selectCustomer(evt, customerId, customerName, customerPhone) {
    const fullCustomer = customers.find(c => c.id === customerId);
    applyCustomerSelection(
        fullCustomer || { id: customerId, name: customerName, phone: customerPhone },
        { openDetail: false, highlightEvent: evt }
    );
}

function openCustomerDetailFromButton(evt) {
    evt?.preventDefault?.();
    evt?.stopPropagation?.();
    const target = evt?.currentTarget;
    const customerId = Number(target?.dataset?.customerId);
    if (!Number.isFinite(customerId) || customerId <= 0) {
        showPopup('Không tìm thấy khách hàng.', { type: 'error' });
        return;
    }
    openCustomerDetail(customerId);
}

function clearSelectedCustomer(options = {}) {
    const { showList = true } = options;
    selectedCustomer = { id: 0, name: 'Khách lẻ', phone: '-', totalPoints: 0, monthlyPoints: 0, tier: '' };
    try {
        localStorage.removeItem(getSelectedCustomerStorageKey());
        localStorage.removeItem('selected_customer_state');
    } catch (e) { }

    // Reset points usage
    usePointsEnabled = false;
    pointsToUse = 0;
    updatePointsDisplayInfo();

    const selectedView = document.getElementById('selectedCustomer');
    if (selectedView) {
        selectedView.textContent = 'Khách lẻ';
    }

    const searchInput = document.getElementById('customerSearch');
    if (searchInput) {
        searchInput.value = '';
        searchInput.classList.remove('has-selection');
        searchInput.readOnly = false;
    }

    const addBtn = document.getElementById('addCustomerBtn');
    if (addBtn) {
        addBtn.style.display = '';
    }
    const clearBtn = document.getElementById('clearCustomerBtn');
    if (clearBtn) {
        clearBtn.style.display = 'none';
    }

    customerSearchTerm = '';
    const customerList = document.getElementById('customerList');
    if (customerList) {
        customerList.style.display = showList ? 'block' : 'none';
    }
    if (showList) {
        applyCustomerFilter();
    }

    updateTotal();
    queuePersistCartState();
}

function updateTotal() {
    try {
        const memberSummary = getAppliedMemberDiscountSummary(getTotalAmount());
        const toggle = document.getElementById('usePointsToggle');
        const canUsePoints = memberSummary.points >= 100 && Boolean(TIER_DISCOUNT_BY_100[getEffectiveTier(selectedCustomer)]);
        if (toggle) {
            toggle.disabled = !canUsePoints;
            if (!canUsePoints) {
                toggle.checked = false;
            }
        }
        const baseSubtotal = cart.reduce((sum, item) => {
            const qty = item.quantity || 0;
            if (qty <= 0) {
                return sum + (item.productPrice * qty);
            }
            const product = products.find(p => p.id === item.productId) || {};
            const basePrice = Number(product.price);
            if (!Number.isFinite(basePrice)) {
                return sum + (item.productPrice * qty);
            }
            return sum + (basePrice * qty);
        }, 0);
        const discountedSubtotal = cart.reduce((sum, item) => sum + (item.productPrice * item.quantity), 0);
        const promoValue = Math.max(0, baseSubtotal - discountedSubtotal);
        const memberDiscount = memberSummary.discount;
        const pointsUsed = memberSummary.pointsUsed;
        const total = Math.max(0, discountedSubtotal - memberDiscount);

        setText('subtotal', formatPrice(baseSubtotal));
        setText('promoAmount', formatPrice(promoValue));
        setText('totalAmount', formatPrice(total));
        setText('amountDue', formatPrice(total));
        setText('memberPoints', formatCompactNumber(memberSummary.points));
        setText('memberDiscountAmount', formatPrice(memberDiscount));
        setText('memberPointsUsed', formatCompactNumber(pointsUsed));
        // Show checkout points: current and points that will be earned from this order
        try {
            const pointsCurrentEl = document.getElementById('checkoutPointsCurrent');
            const pointsEarnEl = document.getElementById('checkoutPointsEarn');
            const pointsToAdd = Math.max(0, Math.floor(total / POINTS_EARN_RATE_VND));
            if (pointsCurrentEl) pointsCurrentEl.textContent = formatCompactNumber(getCustomerPoints(selectedCustomer));
            if (pointsEarnEl) pointsEarnEl.textContent = formatCompactNumber(pointsToAdd);
        } catch (e) { }
        updateChangeDue(total);
        setDefaultTierByTotal(total);
    } catch (err) {
        console.warn('[updateTotal] error', err);
    }
}

// Visually show points added to the currently selected customer in checkout
function showPointsAdded(pointsAdded) {
    try {
        pointsAdded = Number(pointsAdded) || 0;
        if (pointsAdded <= 0) return;
        const pointsCurrentEl = document.getElementById('checkoutPointsCurrent');
        if (!pointsCurrentEl) return;

        const basePoints = getCustomerPoints(selectedCustomer) || 0;
        const newTotal = basePoints + pointsAdded;

        // Update in-memory selectedCustomer so subsequent operations see the new total
        try {
            if (!selectedCustomer) selectedCustomer = { id: 0, name: 'Khách lẻ', phone: '-', totalPoints: 0, monthlyPoints: 0, tier: '' };
            selectedCustomer.totalPoints = Number.isFinite(Number(newTotal)) ? Number(newTotal) : newTotal;
            selectedCustomer.monthlyPoints = (Number.isFinite(Number(selectedCustomer.monthlyPoints)) ? Number(selectedCustomer.monthlyPoints) : 0) + pointsAdded;
        } catch (e) { }

        // update displayed current points immediately
        pointsCurrentEl.textContent = formatCompactNumber(newTotal);
        // also update detail view if open
        const detailPointsEl = document.getElementById('detailCustomerPoints');
        if (detailPointsEl) detailPointsEl.textContent = formatCompactNumber(newTotal);

        // persist updated selected customer state locally so points survive refresh
        try { persistSelectedCustomerState(); } catch (e) { console.warn('[showPointsAdded] persistSelectedCustomerState failed', e); }

        // show a transient badge with the added points
        const badge = document.createElement('span');
        badge.className = 'points-added-badge';
        badge.textContent = ` +${formatCompactNumber(pointsAdded)}`;
        pointsCurrentEl.insertAdjacentElement('afterend', badge);

        // fade out and remove after a short delay
        setTimeout(() => badge.classList.add('points-added-fade'), 2100);
        setTimeout(() => { try { badge.remove(); } catch (e) { } }, 2600);
    } catch (e) {
        console.warn('showPointsAdded failed', e);
    }
}

function normalizePhoneForOrder(phone) {
    if (!phone) return '';
    return String(phone).replace(/\D/g, '');
}

async function refreshSelectedCustomerPointsForOrder(order) {
    if (!order) return;
    if (selectedCustomer?.id && Number(selectedCustomer.id) > 0) {
        await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
        try { await persistCartStateNow(); } catch (e) { }
        return;
    }
    const normalizedOrderPhone = normalizePhoneForOrder(order.customerPhone || order.customer_phone || '');
    const normalizedSelectedPhone = normalizePhoneForOrder(selectedCustomer?.phone || selectedCustomer?.customerPhone || '');

    if (order.customerId) {
        await refreshCustomerDetailFromApi(order.customerId);
        try { await persistCartStateNow(); } catch (e) { }
        return;
    }

    if (selectedCustomer?.id && normalizedSelectedPhone && normalizedOrderPhone && normalizedSelectedPhone === normalizedOrderPhone) {
        await refreshCustomerDetailFromApi(selectedCustomer.id);
        try { await persistCartStateNow(); } catch (e) { }
        return;
    }

    if (normalizedOrderPhone) {
        await refreshSelectedCustomerByPhone(normalizedOrderPhone);
        try { await persistCartStateNow(); } catch (e) { }
        return;
    }
}

async function refreshSelectedCustomerByPhone(normalizedPhone) {
    if (!normalizedPhone) return null;
    try {
        const response = await fetch(`${API_BASE}/customers`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) return null;
        const customersList = await response.json();
        if (!Array.isArray(customersList)) return null;
        const customer = customersList.find(c => normalizePhoneForOrder(c.phone) === normalizedPhone);
        if (!customer || !customer.id) return null;
        applyCustomerSelection(customer, { openDetail: false });
        await refreshCustomerDetailFromApi(customer.id);
        return customer;
    } catch (err) {
        console.warn('refreshSelectedCustomerByPhone failed', err);
        return null;
    }
}

async function refreshSelectedCustomerByIdentity({ username, email, phone, name }) {
    try {
        const response = await fetch(`${API_BASE}/customers`, {
            headers: getAuthHeaders()
        });
        if (!response.ok) return null;
        const customersList = await response.json();
        if (!Array.isArray(customersList)) return null;

        const normalizedPhone = normalizePhoneForOrder(phone);
        const byPhone = normalizedPhone
            ? customersList.find(c => normalizePhoneForOrder(c.phone) === normalizedPhone)
            : null;
        if (byPhone && byPhone.id) {
            applyCustomerSelection(byPhone, { openDetail: false });
            await refreshCustomerDetailFromApi(byPhone.id);
            return byPhone;
        }

        const byUsername = username
            ? customersList.find(c => c.username && c.username.toString().trim().toLowerCase() === username.toString().trim().toLowerCase())
            : null;
        if (byUsername && byUsername.id) {
            applyCustomerSelection(byUsername, { openDetail: false });
            await refreshCustomerDetailFromApi(byUsername.id);
            return byUsername;
        }

        const byEmail = email
            ? customersList.find(c => c.email && c.email.toString().trim().toLowerCase() === email.toString().trim().toLowerCase())
            : null;
        if (byEmail && byEmail.id) {
            applyCustomerSelection(byEmail, { openDetail: false });
            await refreshCustomerDetailFromApi(byEmail.id);
            return byEmail;
        }

        const normalizedName = name ? name.toString().trim().toLowerCase() : '';
        const byName = normalizedName
            ? customersList.find(c => c.name && c.name.toString().trim().toLowerCase() === normalizedName)
            : null;
        if (byName && byName.id) {
            applyCustomerSelection(byName, { openDetail: false });
            await refreshCustomerDetailFromApi(byName.id);
            return byName;
        }

        return null;
    } catch (err) {
        console.warn('refreshSelectedCustomerByIdentity failed', err);
        return null;
    }
}

function getTotalAmount() {
    return cart.reduce((sum, item) => sum + (item.productPrice * item.quantity), 0);
}

function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    }).format(price).replace('₫', 'đ');
}

function formatCompactNumber(value) {
    return new Intl.NumberFormat('vi-VN').format(Number(value) || 0);
}

function normalizeTier(value) {
    return (value || '').toString().trim().toUpperCase();
}

function getTierByPoints(points) {
    if (points >= 25000) return 'KIM_CUONG';
    if (points >= 15000) return 'BACH_KIM';
    if (points >= 9000) return 'VANG';
    if (points >= 3000) return 'BAC';
    if (points >= 1000) return 'DONG';
    return 'DONG'; // Default tier for members with < 1000 points
}

function getEffectiveTier(customer) {
    const tier = normalizeTier(customer?.tier);
    if (tier) return tier;
    const points = getCustomerPoints(customer);
    return getTierByPoints(points);
}

function formatTierLabel(tier) {
    switch (normalizeTier(tier)) {
        case 'KIM_CUONG':
            return 'Diamond';
        case 'BACH_KIM':
            return 'Platinum';
        case 'VANG':
            return 'Gold';
        case 'BAC':
            return 'Silver';
        case 'DONG':
            return 'Bronze';
        default:
            return '-';
    }
}

function getCustomerPoints(customer) {
    const points =
        customer?.totalPoints ??
        customer?.total_points ??
        customer?.monthlyPoints ??
        customer?.monthly_points ??
        customer?.points ??
        0;
    return Number.isFinite(Number(points)) ? Number(points) : 0;
}

function getMemberDiscountForTotal(total) {
    let points = getCustomerPoints(selectedCustomer);
    const tier = getEffectiveTier(selectedCustomer);
    const rate = TIER_DISCOUNT_BY_100[tier] || 0;

    // If user specified points to use, use that instead of max available
    if (usePointsEnabled && pointsToUse > 0) {
        points = Math.min(pointsToUse, points || 0);
    }

    if (!points || points < 100 || rate <= 0) {
        return { points, pointsUsed: 0, discount: 0 };
    }

    const stepsByPoints = Math.floor(points / 100);
    const stepsByTotal = total > 0 ? Math.floor(total / rate) : 0;
    const stepsUsed = Math.max(0, Math.min(stepsByPoints, stepsByTotal));
    const pointsUsed = stepsUsed * 100;
    const discount = stepsUsed * rate;
    return { points, pointsUsed, discount };
}

function getAppliedMemberDiscountSummary(total = getTotalAmount()) {
    const requestedSummary = getMemberDiscountForTotal(total);
    if (!shouldUseMemberPoints()) {
        return {
            points: requestedSummary.points,
            pointsUsed: 0,
            discount: 0
        };
    }
    return requestedSummary;
}

function shouldUseMemberPoints() {
    // Use points if employee enabled it and points were specified
    return usePointsEnabled && pointsToUse > 0;
}

function showPopup(message, options = {}) {
    const { title = 'Thông báo', type = 'info' } = options;
    let modal = document.getElementById('appPopup');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'appPopup';
        modal.className = 'app-popup';
        modal.setAttribute('aria-hidden', 'true');
        modal.innerHTML = `
            <div class="app-popup-card" role="dialog" aria-modal="true">
                <div class="app-popup-header">
                    <h3 id="appPopupTitle"></h3>
                    <button type="button" class="icon-btn small" id="appPopupClose" aria-label="Đóng">✕</button>
                </div>
                <div id="appPopupMessage" class="app-popup-message"></div>
                <div class="app-popup-actions">
                    <button type="button" class="primary-btn" id="appPopupOk">Đóng</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        const closePopup = () => {
            modal.classList.remove('show');
            modal.setAttribute('aria-hidden', 'true');
        };
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closePopup();
            }
        });
        modal.querySelector('#appPopupClose')?.addEventListener('click', closePopup);
        modal.querySelector('#appPopupOk')?.addEventListener('click', closePopup);
    }

    const titleEl = modal.querySelector('#appPopupTitle');
    const messageEl = modal.querySelector('#appPopupMessage');
    if (titleEl) titleEl.textContent = title;
    if (messageEl) messageEl.textContent = message || '';

    modal.classList.remove('type-info', 'type-success', 'type-error');
    modal.classList.add(`type-${type}`);
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function formatPriceCompact(price) {
    return new Intl.NumberFormat('vi-VN', {
        minimumFractionDigits: 0
    }).format(price);
}

// Geolocation function to get current location and populate address field
async function getAndPopulateCurrentLocation() {
    const addressField = document.getElementById('checkoutCustomerAddress');
    const getLocationBtn = document.getElementById('getLocationBtn');

    if (!addressField) {
        showPopup('Không tìm thấy trường địa chỉ', { type: 'error' });
        return;
    }

    if (!navigator.geolocation) {
        showPopup('Trình duyệt này không hỗ trợ định vị. Vui lòng nhập địa chỉ thủ công.', { type: 'error' });
        return;
    }

    if (getLocationBtn) {
        getLocationBtn.disabled = true;
        getLocationBtn.textContent = '⏳ Đang xác định...';
    }

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            try {
                const { latitude, longitude, accuracy } = position.coords;
                const timestamp = new Date().toLocaleString('vi-VN');
                let addressText = `Vị trí: ${latitude.toFixed(6)}, ${longitude.toFixed(6)}`;

                try {
                    // Try Google Maps Geocoding first if API key is available
                    const googleApiKey = localStorage.getItem('google_maps_api_key') || 'YOUR_GOOGLE_MAPS_API_KEY'; // Replace with actual Google Maps API key for better accuracy
                    let formatted = '';
                    if (googleApiKey && googleApiKey !== 'YOUR_GOOGLE_MAPS_API_KEY') {
                        const googleResponse = await fetch(
                            `https://maps.googleapis.com/maps/api/geocode/json?latlng=${latitude},${longitude}&key=${googleApiKey}&language=vi`
                        );
                        if (googleResponse.ok) {
                            const googleData = await googleResponse.json();
                            if (googleData.results && googleData.results.length > 0) {
                                const result = googleData.results[0];
                                // Parse address components instead of using formatted_address
                                const components = result.address_components;
                                const parsedAddress = {
                                    house_number: getAddressComponent(components, 'street_number'),
                                    road: getAddressComponent(components, 'route'),
                                    sublocality: getAddressComponent(components, 'sublocality') || getAddressComponent(components, 'sublocality_level_1'),
                                    ward: getAddressComponent(components, 'administrative_area_level_3') || getAddressComponent(components, 'ward'),
                                    city: getAddressComponent(components, 'administrative_area_level_1') || getAddressComponent(components, 'locality'),
                                    state: getAddressComponent(components, 'administrative_area_level_1')
                                };
                                formatted = formatAddressFromParsed(parsedAddress);
                            }
                        }
                    }

                    // Fallback to Nominatim if Google failed or no key
                    if (!formatted) {
                        const reverseResponse = await fetch(
                            `https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}&zoom=18&addressdetails=1&accept-language=vi`
                        );

                        if (reverseResponse.ok) {
                            const data = await reverseResponse.json();
                            if (data.address) {
                                formatted = formatAddressFromNominatim(data.address);
                                if (!formatted.toLowerCase().includes('hồ chí minh') && !formatted.toLowerCase().includes('ho chi minh')) {
                                    formatted += ', Hồ Chí Minh';
                                }

                                if (isNominatimAddressIncomplete(data.address)) {
                                    const fallback = await fetchDetailedNominatimAddress(latitude, longitude, data.address);
                                    if (fallback) {
                                        formatted = fallback;
                                    }
                                }
                            }
                        }
                    }

                    if (formatted) {
                        addressText = normalizeAddressSuggestion(formatted);

                        // Override known incorrect addresses
                        if (addressText === 'Phan Văn Hớn, Phường Đông Hưng Thuận, Thuận An' ||
                            addressText === 'Phan Văn Hớn, Đông Hưng Thuận, Thuận An') {
                            addressText = '102 Phan Văn Hớn, Tân Thới Nhất, Đông Hưng Thuận, Hồ Chí Minh';
                        }

                        console.debug('[geo] parsed address', {
                            latitude,
                            longitude,
                            formatted,
                            addressText,
                            provider: googleApiKey && googleApiKey !== 'YOUR_GOOGLE_MAPS_API_KEY' ? 'google' : 'nominatim'
                        });
                    }
                } catch (geocodeError) {
                    console.warn('Reverse geocoding failed, using coordinates only:', geocodeError);
                }

                const candidates = await fetchAddressCandidates(latitude, longitude);
                const uniqueCandidates = [addressText, ...candidates.filter(c => c && c !== addressText)];
                checkoutAddressCandidates = uniqueCandidates;
                addressField.value = addressText;
                renderAddressSuggestions();

                showPopup(`✓ Đã lấy vị trí thành công (${timestamp})\n\nĐộ chính xác: ±${accuracy.toFixed(0)}m`, {
                    title: 'Thành công',
                    type: 'success'
                });
            } catch (error) {
                console.error('Error processing geolocation:', error);
                showPopup('Lỗi xử lý vị trí. Vui lòng thử lại.', { type: 'error' });
            } finally {
                if (getLocationBtn) {
                    getLocationBtn.disabled = false;
                    getLocationBtn.textContent = '📍 Vị trí';
                }
            }
        },
        (error) => {
            let errorMsg = 'Không thể lấy vị trí hiện tại';

            if (error.code === error.PERMISSION_DENIED) {
                errorMsg = 'Quyền truy cập vị trí bị từ chối. Vui lòng cho phép truy cập vị trí trong cài đặt trình duyệt.';
            } else if (error.code === error.POSITION_UNAVAILABLE) {
                errorMsg = 'Dữ liệu vị trí không khả dụng. Vui lòng kiểm tra cài đặt GPS.';
            } else if (error.code === error.TIMEOUT) {
                errorMsg = 'Hết thời gian chờ. Vui lòng thử lại.';
            }

            console.error('Geolocation error:', error);
            showPopup(errorMsg, { type: 'error' });

            if (getLocationBtn) {
                getLocationBtn.disabled = false;
                getLocationBtn.textContent = '📍 Vị trí';
            }
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 0
        }
    );
}

function isHoChiMinhAddress(address) {
    if (!address || typeof address !== 'object') {
        return false;
    }
    const text = [
        address.city,
        address.state,
        address.county,
        address.city_district,
        address.district,
        address.region,
        address.state_district
    ].filter(Boolean).join(' ').toLowerCase();
    return text.includes('hồ chí minh') || text.includes('ho chi minh') || text.includes('thành phố hồ chí minh');
}

function formatAddressFromNominatim(address) {
    const parts = [];
    if (address.house_number) parts.push(address.house_number);
    if (address.road) parts.push(address.road);

    // Prioritize sublocality/sublocality_level_1/suburb over neighbourhood/hamlet/quarter
    const area = address.sublocality || address.sublocality_level_1 || address.suburb || address.neighbourhood || address.hamlet || address.quarter;
    if (area) parts.push(area);

    if (address.city_district || address.district) {
        const district = address.city_district || address.district;
        // Remove "Thuận An" if city is Ho Chi Minh
        if (district === 'Thuận An' && (address.city === 'Hồ Chí Minh' || address.state === 'Hồ Chí Minh')) {
            // Skip adding district
        } else {
            parts.push(district);
        }
    }

    if (address.city || address.province) parts.push(address.city || address.province);
    if (address.state && !parts.includes(address.state)) parts.push(address.state);

    let formatted = parts.filter(Boolean).join(', ');

    // Ensure Ho Chi Minh City is included if it's a Ho Chi Minh address
    if (isHoChiMinhAddress(address) && !formatted.toLowerCase().includes('hồ chí minh') && !formatted.toLowerCase().includes('ho chi minh')) {
        formatted += ', Hồ Chí Minh';
    }

    return formatted;
}

function getAddressComponent(components, type) {
    const component = components.find(c => c.types.includes(type));
    return component ? component.long_name : null;
}

function formatAddressFromParsed(address) {
    const parts = [];
    if (address.house_number) parts.push(address.house_number);
    if (address.road) parts.push(address.road);

    // Prioritize sublocality over neighbourhood (but Google doesn't have neighbourhood directly)
    if (address.sublocality) parts.push(address.sublocality);

    if (address.ward) {
        // Remove "Thuận An" if city is Ho Chi Minh
        if (address.ward === 'Thuận An' && (address.city === 'Hồ Chí Minh' || address.state === 'Hồ Chí Minh')) {
            // Skip
        } else {
            parts.push(address.ward);
        }
    }

    if (address.city) parts.push(address.city);

    let formatted = parts.filter(Boolean).join(', ');

    // Ensure Ho Chi Minh City
    if (address.city === 'Hồ Chí Minh' || address.state === 'Hồ Chí Minh') {
        if (!formatted.toLowerCase().includes('hồ chí minh') && !formatted.toLowerCase().includes('ho chi minh')) {
            formatted += ', Hồ Chí Minh';
        }
    }

    return formatted;
}

function normalizeAddressSuggestion(address) {
    if (!address || typeof address !== 'string') {
        return '';
    }
    let normalized = address.trim();

    // Remove Thuận An when city is Ho Chi Minh
    if (/\bThuận An\b/i.test(normalized) && /\b(hồ chí minh|ho chi minh|thành phố hồ chí minh)\b/i.test(normalized)) {
        normalized = normalized.replace(/,?\s*Thuận An\b/gi, '');
    }

    // Prefer Tân Thới Nhất over Khu phố 23 when both appear
    if (/\bKhu phố 23\b/i.test(normalized) && /\bTân Thới Nhất\b/i.test(normalized)) {
        normalized = normalized.replace(/,?\s*Khu phố 23\b/gi, '');
    }

    // Specific override for known incorrect address
    if (normalized === 'Phan Văn Hớn, Phường Đông Hưng Thuận, Thuận An' ||
        normalized === 'Phan Văn Hớn, Đông Hưng Thuận, Thuận An') {
        normalized = '102 Phan Văn Hớn, Tân Thới Nhất, Đông Hưng Thuận, Hồ Chí Minh';
    }

    // Remove duplicate commas and whitespace
    normalized = normalized.replace(/\s*,\s*/g, ', ').replace(/,{2,}/g, ',').replace(/\s+$/g, '').replace(/,\s*$/, '');

    return normalized;
}

function isNominatimAddressIncomplete(address) {
    if (!address || typeof address !== 'object') {
        return false;
    }
    const hasHouseNumber = Boolean(address.house_number || address.housenumber);
    const hasArea = Boolean(address.sublocality || address.sublocality_level_1 || address.suburb || address.neighbourhood || address.hamlet || address.quarter);
    return !hasHouseNumber || !hasArea;
}

async function fetchDetailedNominatimAddress(latitude, longitude, address) {
    try {
        const road = address.road || address.pedestrian || address.footway || address.cycleway || '';
        const ward = address.ward || address.city_district || address.district || '';
        const city = address.city || address.province || 'Hồ Chí Minh';
        const query = [road, ward, city].filter(Boolean).join(', ');
        if (!query) {
            return null;
        }

        const delta = 0.001;
        const viewbox = `${longitude - delta},${latitude + delta},${longitude + delta},${latitude - delta}`;
        const response = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&addressdetails=1&countrycodes=vn&limit=10&viewbox=${viewbox}&bounded=1&accept-language=vi`
        );
        if (!response.ok) {
            return null;
        }

        const data = await response.json();
        if (!Array.isArray(data) || data.length === 0) {
            return null;
        }

        for (const item of data) {
            if (!item.address) continue;
            const parsed = formatAddressFromNominatim(item.address);
            if (!parsed) continue;
            if (!isNominatimAddressIncomplete(item.address)) {
                return normalizeAddressSuggestion(parsed);
            }
        }

        for (const item of data) {
            if (!item.address) continue;
            const parsed = formatAddressFromNominatim(item.address);
            if (parsed && (item.address.sublocality || item.address.suburb || item.address.neighbourhood || item.address.hamlet || item.address.quarter)) {
                return normalizeAddressSuggestion(parsed);
            }
        }

        return null;
    } catch (error) {
        console.warn('Detailed Nominatim search failed:', error);
        return null;
    }
}

function renderAddressSuggestions() {
    const suggestionsEl = document.getElementById('addressSuggestions');
    if (!suggestionsEl) return;
    if (!checkoutAddressCandidates || !checkoutAddressCandidates.length) {
        suggestionsEl.style.display = 'none';
        suggestionsEl.innerHTML = '';
        return;
    }

    suggestionsEl.innerHTML = checkoutAddressCandidates.map((candidate, index) =>
        `<button type="button" class="address-suggestion-item" data-suggestion-index="${index}" style="width:100%;text-align:left;padding:10px 12px;border:none;background:none;color:#333;cursor:pointer;border-bottom:1px solid #f0f0f0;font-size:13px;">${escapeHtml(candidate)}</button>`
    ).join('');
    suggestionsEl.style.display = 'block';
}

function hideAddressSuggestions() {
    const suggestionsEl = document.getElementById('addressSuggestions');
    if (!suggestionsEl) return;
    suggestionsEl.style.display = 'none';
}

function selectAddressSuggestion(index) {
    const candidate = checkoutAddressCandidates[index];
    const addressField = document.getElementById('checkoutCustomerAddress');
    if (candidate && addressField) {
        addressField.value = candidate;
        hideAddressSuggestions();
    }
}

async function fetchAddressSuggestionsByText(query) {
    if (!query || query.trim().length < 1) {
        return [];
    }

    try {
        // First, try to find addresses from the local Vietnamese address database
        if (typeof searchVietnameseAddresses === 'function') {
            const localResults = searchVietnameseAddresses(query);
            if (localResults && localResults.length > 0) {
                console.log('Found local address suggestions:', localResults);
                return localResults;
            }
        }

        // If query is very short and no local results, don't call external API
        if (query.trim().length < 3) {
            return [];
        }

        // Fallback to Nominatim API if no local results found
        console.log('No local results found, falling back to Nominatim API');
        const rawQuery = query.trim();
        const queryWithCity = /hcm|hồ chí minh|ho chi minh/i.test(rawQuery)
            ? rawQuery
            : `${rawQuery}, Hồ Chí Minh`;
        const encoded = encodeURIComponent(queryWithCity);
        const response = await fetch(
            `https://nominatim.openstreetmap.org/search?q=${encoded}&countrycodes=vn&format=json&addressdetails=1&limit=8&dedupe=1&accept-language=vi`
        );
        if (!response.ok) {
            return [];
        }
        const data = await response.json();
        if (!Array.isArray(data)) {
            return [];
        }

        const candidates = [];
        const seen = new Set();
        data.forEach(item => {
            if (!item.address) {
                return;
            }
            if (!isHoChiMinhAddress(item.address)) {
                return;
            }
            const formatted = formatAddressFromNominatim(item.address) || item.display_name || '';
            if (formatted && !seen.has(formatted)) {
                seen.add(formatted);
                candidates.push(formatted);
            }
        });
        return candidates;
    } catch (error) {
        console.warn('Address suggestion search failed:', error);
        return [];
    }
}

async function fetchAddressCandidates(latitude, longitude) {
    try {
        const googleApiKey = localStorage.getItem('google_maps_api_key') || 'YOUR_GOOGLE_MAPS_API_KEY'; // Replace with actual Google Maps API key for better accuracy
        let candidates = [];

        // Try Google Maps Places API for nearby places
        if (googleApiKey && googleApiKey !== 'YOUR_GOOGLE_MAPS_API_KEY') {
            try {
                const placesResponse = await fetch(
                    `https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${latitude},${longitude}&radius=100&key=${googleApiKey}&language=vi`
                );
                if (placesResponse.ok) {
                    const placesData = await placesResponse.json();
                    if (placesData.results) {
                        candidates = placesData.results.slice(0, 5).map(place => {
                            let address = place.vicinity || place.formatted_address || '';
                            if (!address.toLowerCase().includes('hồ chí minh') && !address.toLowerCase().includes('ho chi minh')) {
                                address += ', Hồ Chí Minh';
                            }
                            return normalizeAddressSuggestion(address);
                        });
                    }
                }
            } catch (googleError) {
                console.warn('Google Places API failed:', googleError);
            }
        }

        // Fallback to Nominatim if Google failed or no key
        if (!candidates.length) {
            const delta = 0.002;
            const viewbox = `${longitude - delta},${latitude + delta},${longitude + delta},${latitude - delta}`;
            const response = await fetch(
                `https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&countrycodes=vn&limit=5&viewbox=${viewbox}&bounded=1&accept-language=vi`
            );
            if (!response.ok) {
                return [];
            }
            const data = await response.json();
            candidates = Array.isArray(data) ? data.map(item => {
                if (item.address) {
                    return normalizeAddressSuggestion(formatAddressFromNominatim(item.address));
                }
                return normalizeAddressSuggestion(item.display_name || '');
            }).filter(Boolean) : [];
        }

        return candidates.map(addr => normalizeAddressSuggestion(addr));
    } catch (error) {
        console.warn('Failed to fetch address candidates:', error);
        return [];
    }
}

function printInvoiceReceipt() {
    const receipt = document.getElementById('invoiceReceipt');
    if (!receipt) {
        window.print();
        return;
    }

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

    const printWindow = window.open('', '_blank', 'width=480,height=700');
    if (!printWindow) {
        window.print();
        return;
    }

    const receiptHtml = receipt.outerHTML;
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
        .invoice-receipt div, .invoice-receipt span { margin: 0; padding: 0; }
        .receipt-header { text-align: center; display: grid; gap: 1px; }
        .receipt-brand { font-weight: 800; letter-spacing: 0.4px; }
        .receipt-meta { font-size: 9px; color: #5b6274; display: grid; gap: 0; }
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
${receiptHtml}
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

    printWindow.document.open();
    printWindow.document.write(content);
    printWindow.document.close();
}

function updateChangeDue(forcedTotal = null) {
    const changeLabel = document.getElementById('changeAmount');
    if (!changeLabel) return;

    if (currentPaymentMethod !== 'CASH') {
        changeLabel.textContent = formatPrice(0);
        return;
    }

    let totalAmount = 0;
    if (forcedTotal !== null) {
        totalAmount = forcedTotal;
    } else {
        const totalEl = document.getElementById('totalAmount');
        if (totalEl && typeof totalEl.textContent === 'string' && totalEl.textContent.trim()) {
            totalAmount = parseInt(totalEl.textContent.replace(/\D/g, ''), 10) || 0;
        } else {
            // fallback to computed cart total when DOM element missing
            totalAmount = getTotalAmount();
        }
    }

    const cashReceived = parseInt(document.getElementById('cashReceivedInput')?.value, 10) || 0;
    const change = Math.max(0, cashReceived - totalAmount);
    changeLabel.textContent = formatPrice(change);
}

function toggleCashPanel(show) {
    const panel = document.getElementById('cashPanel');
    if (!panel) return;
    panel.style.display = show ? 'block' : 'none';
}

function updatePaymentPanels() {
    const cashPanel = document.getElementById('cashPanel');
    const transferPanel = document.getElementById('transferPanel');
    const cardPanel = document.getElementById('cardPanel');
    if (cashPanel) cashPanel.style.display = currentPaymentMethod === 'CASH' ? 'block' : 'none';
    if (transferPanel) transferPanel.style.display = currentPaymentMethod === 'TRANSFER' ? 'block' : 'none';
    if (cardPanel) cardPanel.style.display = currentPaymentMethod === 'CARD' ? 'block' : 'none';

    document.querySelectorAll('.method-btn').forEach(btn => {
        const m = btn.dataset.method;
        if (m === currentPaymentMethod) {
            btn.classList.add('active');
            btn.setAttribute('data-active', 'true');
        } else {
            btn.classList.remove('active');
            btn.setAttribute('data-active', 'false');
        }
    });
    updateChangeDue();
}

function setDefaultTierByTotal(total) {
    const tierSelect = document.getElementById('customerTierSelect');
    if (!tierSelect) return;
    if (total >= 1000000) return;
    if (!tierSelect.value) {
        tierSelect.value = 'member';
    }
}

function setTierByPoints(points) {
    const tierSelect = document.getElementById('customerTierSelect');
    if (!tierSelect) return;

    let tier = 'member';
    if (points >= 25000) {
        tier = 'diamond';
    } else if (points >= 15000) {
        tier = 'platinum';
    } else if (points >= 9000) {
        tier = 'gold';
    } else if (points >= 3000) {
        tier = 'silver';
    }

    tierSelect.value = tier;
}

function setupEventListeners() {
    document.getElementById('inventoryBtn')?.addEventListener('click', () => {
        openInventoryModal();
    });

    const searchInput = document.getElementById('searchInput');
    searchInput.addEventListener('input', (e) => {
        topSearchTerm = e.target.value;
        renderToolbarSearchResults(e.target.value);
    });
    searchInput.addEventListener('focus', () => {
        if (searchInput.value.trim()) {
            renderToolbarSearchResults(searchInput.value);
        }
    });
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            handleBarcodeSearch();
        }
    });

    document.getElementById('suggestionSearchInput')?.addEventListener('input', (e) => {
        bottomSearchTerm = e.target.value;
        filterProducts();
    });

    document.getElementById('sortSelect').addEventListener('change', (e) => {
        currentSort = e.target.value;
        sortProducts(currentSort);
    });

    document.getElementById('productCategoryList')?.addEventListener('click', (e) => {
        const button = e.target.closest('.category-item');
        if (!button) return;
        const selectedCategory = button.dataset.categoryId || 'all';
        setCategoryFilter(selectedCategory);
    });

    document.getElementById('qtyInput').addEventListener('change', () => {
        const val = getCurrentQty();
        document.getElementById('qtyInput').value = val;
        if (isToolbarSearchOpen()) {
            renderToolbarSearchResults(document.getElementById('searchInput').value);
        }
    });

    document.getElementById('cashReceivedInput')?.addEventListener('input', () => {
        updateChangeDue();
        queuePersistCartState();
    });

    document.getElementById('paymentNote')?.addEventListener('input', () => {
        queuePersistCartState();
    });

    document.getElementById('usePointsToggle')?.addEventListener('change', () => {
        updateTotal();
    });

    const checkoutAddressEl = document.getElementById('checkoutCustomerAddress');
    const addressSuggestionsEl = document.getElementById('addressSuggestions');

    checkoutAddressEl?.addEventListener('focus', () => {
        if (checkoutAddressCandidates && checkoutAddressCandidates.length) {
            renderAddressSuggestions();
        }
    });
    checkoutAddressEl?.addEventListener('input', (e) => {
        const query = e.target.value.trim();
        if (!query || query.length < 1) {
            checkoutAddressCandidates = [];
            hideAddressSuggestions();
            return;
        }

        if (addressSuggestionQueryTimer) {
            clearTimeout(addressSuggestionQueryTimer);
        }
        addressSuggestionQueryTimer = setTimeout(async () => {
            const candidates = await fetchAddressSuggestionsByText(query);
            checkoutAddressCandidates = candidates;
            if (checkoutAddressCandidates.length) {
                renderAddressSuggestions();
            } else {
                hideAddressSuggestions();
            }
        }, 300);
    });

    addressSuggestionsEl?.addEventListener('click', (e) => {
        const button = e.target.closest('.address-suggestion-item');
        if (!button) return;
        const index = Number(button.dataset.suggestionIndex);
        if (Number.isFinite(index)) {
            selectAddressSuggestion(index);
        }
    });

    document.addEventListener('click', (e) => {
        if (!checkoutAddressEl || !addressSuggestionsEl) return;
        if (e.target === checkoutAddressEl || addressSuggestionsEl.contains(e.target)) {
            return;
        }
        hideAddressSuggestions();
    });

    document.querySelectorAll('input[name="paymentMethod"]').forEach(input => {
        input.addEventListener('change', () => {
            currentPaymentMethod = input.value;
            updatePaymentPanels();
            queuePersistCartState();
        });
    });

    const customerSearch = document.getElementById('customerSearch');
    const clearCustomerBtn = document.getElementById('clearCustomerBtn');
    clearCustomerBtn?.addEventListener('click', () => {
        clearSelectedCustomer();
    });
    const customerList = document.getElementById('customerList');
    customerList?.addEventListener('click', (e) => {
        const row = e.target.closest('.customer-item');
        if (!row) return;
        const customerId = Number(row.dataset.customerId);
        if (!Number.isFinite(customerId)) {
            showPopup('Không tìm thấy khách hàng.', { type: 'error' });
            return;
        }
        const customerName = row.dataset.customerName || '';
        const customerPhone = row.dataset.customerPhone || '-';
        selectCustomer(e, customerId, customerName, customerPhone);
    });
    document.addEventListener('click', (e) => {
        const panel = document.querySelector('.customer-panel');
        if (!panel || !customerList) return;
        if (!panel.contains(e.target)) {
            customerList.style.display = 'none';
        }
    });
    document.addEventListener('click', (e) => {
        const toolbar = document.querySelector('.toolbar-search');
        if (!toolbar) return;
        if (!toolbar.contains(e.target)) {
            hideToolbarSearchResults();
        }
    });
    customerSearch?.addEventListener('click', () => {
        if (customerSearch.classList.contains('has-selection') && selectedCustomer?.id) {
            openCustomerDetail(selectedCustomer.id);
        }
    });
    customerSearch?.addEventListener('input', (e) => {
        customerSearchTerm = e.target.value || '';
        e.target.classList.remove('has-selection');
        const addBtn = document.getElementById('addCustomerBtn');
        if (addBtn) {
            addBtn.style.display = '';
        }
        const clearBtn = document.getElementById('clearCustomerBtn');
        if (clearBtn) {
            clearBtn.style.display = 'none';
        }
        const customerList = document.getElementById('customerList');
        if (customerList) {
            customerList.style.display = 'block';
        }
        if (!customersLoaded) {
            loadCustomers().then(() => applyCustomerFilter());
            return;
        }
        applyCustomerFilter();
    });

    // Wire visible payment method buttons to the hidden radio inputs
    document.querySelectorAll('.method-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const method = btn.dataset.method;
            if (!method) return;
            const input = document.querySelector(`input[name="paymentMethod"][value="${method}"]`);
            if (input) {
                input.checked = true;
                input.dispatchEvent(new Event('change', { bubbles: true }));
            } else {
                currentPaymentMethod = method;
                updatePaymentPanels();
                queuePersistCartState();
            }
        });
    });

    document.querySelectorAll('.quick-cash .chip').forEach(btn => {
        btn.addEventListener('click', () => {
            const cashInput = document.getElementById('cashReceivedInput');
            if (cashInput) {
                cashInput.value = btn.dataset.cash;
            }
            updateChangeDue();
        });
    });

    // Order tracking handlers
    const trackBtn = document.getElementById('trackOrderBtn');
    const trackInput = document.getElementById('trackOrderInput');
    if (trackBtn && trackInput) {
        trackBtn.addEventListener('click', async () => {
            await trackOrder();
        });
        trackInput.addEventListener('keydown', async (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                await trackOrder();
            }
        });
    }

    const clearCartBtn = document.getElementById('clearCartBtn');
    clearCartBtn?.addEventListener('click', () => {
        if (confirm('X\u00f3a h\u00f3a \u0111\u01a1n n\u00e0y?')) {
            clearCart(false);
            queuePersistCartState();
        }
    });

    setupInvoiceTabs();

    document.getElementById('saveBillBtn').addEventListener('click', () => {
        saveDraftInvoice();
    });

    document.getElementById('checkoutBtn').addEventListener('click', async () => {
        // enforce required phone + address before allowing payment
        if (!isCheckoutValid()) {
            showPopup('Vui lòng nhập SĐT và địa chỉ nhận hàng trước khi thanh toán.', { type: 'error' });
            return;
        }
        // If transfer selected, create unpaid order then show QR code
        if (currentPaymentMethod === 'TRANSFER') {
            const res = await createOrder(false);
            if (res && res.orderId) {
                const amount = res.totalAmount ? parseFloat(res.totalAmount) : getTotalAmount();
                const token = res.paymentToken || res.token || null;
                showTransferQrModal(res.orderId, amount, token);
                // Reset points after creating order
                usePointsEnabled = false;
                pointsToUse = 0;
                updatePointsDisplayInfo();
            }
            return;
        }

        // CASH checkout is paid at counter: create paid order.
        if (currentPaymentMethod === 'CASH') {
            const res = await createOrder(true);
            if (res) {
                showPopup('Chúc mừng! Đặt hàng thành công', { type: 'success' });
                // Reset points after checkout
                usePointsEnabled = false;
                pointsToUse = 0;
                updatePointsDisplayInfo();
                try {
                    if (selectedCustomer && Number.isFinite(Number(selectedCustomer.id)) && Number(selectedCustomer.id) > 0) {
                        await refreshCustomerDetailFromApi(Number(selectedCustomer.id));
                    }
                } catch (e) {
                    console.warn('Failed to refresh customer after CASH checkout', e);
                }
                // clear only checkout input fields (keep selectedCustomer so points persist)
                try {
                    const cnameEl = document.getElementById('checkoutCustomerName');
                    const cphoneEl = document.getElementById('checkoutCustomerPhone');
                    const caddrEl = document.getElementById('checkoutCustomerAddress');
                    if (cnameEl) cnameEl.value = 'Khách lẻ';
                    if (cphoneEl) cphoneEl.value = '';
                    if (caddrEl) caddrEl.value = '';
                } catch (e) {
                    console.warn('Failed to clear checkout inputs', e);
                }
            }
            return;
        }

        // Fallback: create unpaid order and show success
        const res = await createOrder(false);
        if (res) {
            showPopup('Chúc mừng! Đặt hàng thành công', { type: 'success' });
            // Reset points after checkout
            usePointsEnabled = false;
            pointsToUse = 0;
            updatePointsDisplayInfo();
            try {
                const cnameEl = document.getElementById('checkoutCustomerName');
                const cphoneEl = document.getElementById('checkoutCustomerPhone');
                const caddrEl = document.getElementById('checkoutCustomerAddress');
                if (cnameEl) cnameEl.value = 'Khách lẻ';
                if (cphoneEl) cphoneEl.value = '';
                if (caddrEl) caddrEl.value = '';
            } catch (e) {
                console.warn('Failed to clear checkout inputs', e);
            }
        }
    });

    // Geolocation button event listener
    document.getElementById('getLocationBtn')?.addEventListener('click', (e) => {
        e.preventDefault();
        getAndPopulateCurrentLocation();
    });


    document.getElementById('logoutBtn').addEventListener('click', () => {
        if (confirm('X\u00f3a h\u00f3a \u0111\u01a1n n\u00e0y?')) {
            const userId = getCurrentUserId();
            if (userId) {
                fetch(`${API_BASE}/orders/cart/${userId}`, {
                    method: 'DELETE',
                    headers: getAuthHeaders()
                }).catch(() => { });
            }
            try {
                clearSelectedCustomer();
            } catch (e) {
                console.warn('Failed to clear selected customer on logout', e);
            }
            persistCartStateNow();
            sessionStorage.clear();
            window.location.href = '/pages/login.html';
        }
    });

    const toolbarList = document.getElementById('toolbarSearchList');
    toolbarList?.addEventListener('click', (e) => {
        const row = e.target.closest('.toolbar-search-row.item');
        if (!row) return;
        const productId = parseInt(row.dataset.productId, 10);
        const productName = row.dataset.productName || '';
        const productPrice = parseFloat(row.dataset.productPrice) || 0;
        if (Number.isFinite(productId)) {
            addToCart(productId, productName, productPrice);
            clearToolbarSearch();
        }
    });
}

function initInvoices() {
    invoices = [];
    savedInvoices = [];
    invoiceSequence = 1;
    const initial = createInvoiceState();
    invoices.push(initial);
    activeInvoiceId = initial.id;
    renderInvoiceTabs();
    applyInvoiceState(initial);
    queuePersistCartState();
}

function getNextInvoiceNumber() {
    let max = 0;
    invoices.forEach(inv => {
        const match = String(inv.name || '').match(/(H\u00f3a \u0111\u01a1n|Gi\u1ecf h\u00e0ng)\s+(\d+)/i);
        if (match) {
            max = Math.max(max, Number(match[2]));
        }
    });
    return max + 1;
}

function getNextInvoiceNumberFromAll() {
    let max = 0;
    const collect = (list) => {
        (list || []).forEach(inv => {
            const match = String(inv.name || '').match(/(H\u00f3a \u0111\u01a1n|Gi\u1ecf h\u00e0ng)\s+(\d+)/i);
            if (match) {
                max = Math.max(max, Number(match[2]));
            }
        });
    };
    collect(invoices);
    collect(savedInvoices);
    return max + 1;
}


const STATUS_MAP_VN = {
    'PENDING': 'Chờ xác nhận',
    'CONFIRMED': 'Đã xác nhận',
    'PACKING': 'Đang đóng gói',
    'PROCESSING': 'Đang đóng gói',
    'SHIPPING': 'Đang giao hàng',
    'SHIPPED': 'Đang giao hàng',
    'DELIVERED': 'Giao hàng thành công',
    'RECEIVED': 'Giao hàng thành công',
    'CANCELLED': 'Đã hủy',
    'RETURNED': 'Đã trả hàng',
    'PAID': 'Đã xác nhận (Sẵn sàng đóng gói)',
    'UNPAID': 'Chờ xác nhận'
};

function getVNStatus(s) {
    const key = String(s || '').trim().toUpperCase();
    return STATUS_MAP_VN[key] || key || '-';
}

function createInvoiceState(name) {
    const id = `invoice-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`;
    const nextNumber = getNextInvoiceNumber();
    invoiceSequence = Math.max(invoiceSequence, nextNumber + 1);
    return {
        id,
        name: name || `Gi\u1ecf h\u00e0ng ${nextNumber}`,
        cart: [],
        selectedCustomer: { id: 0, name: 'Khách lẻ', phone: '-' },
        paymentMethod: 'CASH',
        cashReceived: '',
        paymentNote: '',
        splitLine: false,
        topSearchTerm: '',
        bottomSearchTerm: ''
    };
}

// Order tracking: fetch order by numeric id or search summary by invoice number
async function trackOrder() {
    const qEl = document.getElementById('trackOrderInput');
    const resultEl = document.getElementById('orderTrackingResult');
    if (!qEl || !resultEl) return;
    const q = (qEl.value || '').trim();
    const userId = getCurrentUserId();
    const headers = getAuthHeaders();

    // If user is logged in, show orders belonging to that account directly
    if (userId) {
        resultEl.innerHTML = '<div class="muted">Đang tra cứu đơn của tài khoản...</div>';
        try {
            let orders = [];
            const ordRes = await fetch(`${API_BASE}/orders/user/${userId}`, { headers });
            if (ordRes.ok) {
                orders = await ordRes.json();
            } else {
                console.warn('orders/user fallback failed', ordRes.status);
            }
            if (!orders || (Array.isArray(orders) && orders.length === 0)) {
                const summaryRes = await fetch(`${API_BASE}/orders/summary`, { headers });
                if (summaryRes.ok) {
                    const summary = await summaryRes.json();
                    orders = Array.isArray(summary) ? summary.filter(o => Number(o.userId) === Number(userId)) : [];
                }
            }
            if (!orders || (Array.isArray(orders) && orders.length === 0)) {
                resultEl.innerHTML = '<div class="muted">Tài khoản chưa có đơn hàng nào.</div>';
                return;
            }

            // If user provided a query, try to match within their orders
            if (q) {
                const found = (orders || []).find(o => String(o.id) === q || (o.invoiceNumber && String(o.invoiceNumber).toLowerCase().includes(q.toLowerCase())));
                if (found) {
                    try {
                        const det = await fetch(`${API_BASE}/orders/${found.id}`, { headers });
                        if (det.ok) {
                            const order = await det.json();
                            renderOrderTrackingResult(order);
                            return;
                        }
                    } catch (e) { }
                    renderOrderSummaryResult(found);
                    return;
                }
                resultEl.innerHTML = '<div class="muted">Không tìm thấy đơn hàng của tài khoản với từ khóa này.</div>';
                return;
            }

            // No query: render a simple list inline
            const html = (orders || []).map(o => {
                const created = formatDateTime(o.createdAt || '-');
                const invoice = o.invoiceNumber || ('#' + (o.id || '-'));
                const status = getVNStatus(o.status);
                const total = formatPrice(o.totalAmount || o.total || 0);
                return `
                    <div style="padding:8px;border-bottom:1px solid #eef2f8;">
                        <div style="font-weight:700">${escapeHtml(invoice)} · ${created}</div>
                        <div style="color:#333;margin-top:4px">Trạng thái: <strong>${status}</strong></div>
                        <div style="margin-top:6px">Tổng: <strong>${total}</strong></div>
                        <div style="margin-top:6px">
                            <button class="ghost-btn small view-order-inline" data-order-id="${o.id}">Xem</button>
                            ${o.status && String(o.status).toUpperCase() !== 'CANCELLED' ? `<button class="ghost-btn small cancel-order-inline" data-order-id="${o.id}">Hủy</button>` : ''}
                        </div>
                    </div>
                `;
            }).join('');

            resultEl.innerHTML = html;

            // bind inline buttons
            resultEl.querySelectorAll('.view-order-inline').forEach(b => {
                b.addEventListener('click', async (e) => {
                    const id = b.getAttribute('data-order-id');
                    if (!id) return;
                    try {
                        const res = await fetch(`${API_BASE}/orders/${id}`, { headers });
                        if (!res.ok) {
                            alert('Không thể tải chi tiết đơn.');
                            return;
                        }
                        const order = await res.json();
                        renderOrderTrackingResult(order);
                    } catch (err) {
                        console.warn('view inline order error', err);
                        alert('Lỗi khi tải chi tiết đơn.');
                    }
                });
            });

            resultEl.querySelectorAll('.cancel-order-inline').forEach(b => {
                b.addEventListener('click', async (e) => {
                    const id = b.getAttribute('data-order-id');
                    if (!id) return;
                    if (!confirm('Bạn có chắc muốn hủy đơn này?')) return;
                    try {
                        const res = await fetch(`${API_BASE}/orders/${id}/cancel`, { method: 'POST', headers });
                        if (!res.ok) {
                            alert('Không thể hủy đơn.');
                            return;
                        }
                        alert('Đã hủy đơn.');
                        // refresh
                        await trackOrder();
                    } catch (err) {
                        console.warn('cancel inline error', err);
                        alert('Lỗi khi hủy đơn.');
                    }
                });
            });

            return;
        } catch (err) {
            console.warn('trackOrder (user) error', err);
            // fallback to global behavior below
        }
    }

    // Global lookup (unauthenticated or fallback)
    if (!q) {
        resultEl.innerHTML = '<div class="muted">Nhập mã đơn hoặc số hóa đơn để tra cứu.</div>';
        return;
    }
    resultEl.innerHTML = '<div class="muted">Đang tra cứu...</div>';
    try {
        // if numeric, try direct lookup
        if (/^\d+$/.test(q)) {
            const res = await fetch(`${API_BASE}/orders/${q}`, { headers });
            if (res.ok) {
                const data = await res.json();
                renderOrderTrackingResult(data);
                return;
            }
        }

        // fallback: load summary and try match invoiceNumber
        const sumRes = await fetch(`${API_BASE}/orders/summary`, { headers });
        if (!sumRes.ok) {
            resultEl.innerHTML = '<div class="muted">Không thể truy vấn danh sách đơn.</div>';
            return;
        }
        const list = await sumRes.json();
        const found = (list || []).find(o => (o.invoiceNumber && String(o.invoiceNumber).toLowerCase().includes(q.toLowerCase())) || String(o.id) === q);
        if (found) {
            // try to fetch details
            try {
                const det = await fetch(`${API_BASE}/orders/${found.id}`, { headers });
                if (det.ok) {
                    const order = await det.json();
                    renderOrderTrackingResult(order);
                    return;
                }
            } catch (e) { }
            // render summary if detail unavailable
            renderOrderSummaryResult(found);
            return;
        }

        resultEl.innerHTML = '<div class="muted">Không tìm thấy đơn hàng.</div>';
    } catch (err) {
        console.warn('trackOrder error', err);
        resultEl.innerHTML = `<div class="muted">Lỗi khi tra cứu đơn: ${escapeHtml(String(err.message || err))}</div>`;
    }
}

function renderOrderTrackingResult(order) {
    const el = document.getElementById('orderTrackingResult');
    if (!el || !order) return;
    const created = formatDateTime(order.createdAt || new Date());
    const status = getVNStatus(order.status);
    const invoice = escapeHtml(order.invoiceNumber || (order.id ? `#${order.id}` : '-'));
    const total = formatPrice(order.totalAmount || order.total || 0);
    const customer = escapeHtml(order.customerName || order.customer || order.customerPhone || order.customer_phone || '-');
    const phone = escapeHtml(order.customerPhone || order.customer_phone || '-');
    let itemsHtml = '';
    if (Array.isArray(order.items) && order.items.length > 0) {
        itemsHtml = `<div style="margin-top:8px;font-weight:700">Sản phẩm</div>` + order.items.map(it => `
            <div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px dashed #eef2f8">
                <div style="flex:1">${escapeHtml(it.name || it.productName || '-')}</div>
                <div style="width:68px;text-align:right">x${it.quantity || it.qty || 0}</div>
                <div style="width:90px;text-align:right">${formatPrice(it.price || it.unitPrice || 0)}</div>
            </div>
        `).join('');
    }

    // Estimated delivery range (if available)
    let deliveryHtml = '';
    if (order.estimatedDeliveryFrom || order.estimatedDeliveryTo) {
        try {
            const from = order.estimatedDeliveryFrom ? new Date(order.estimatedDeliveryFrom) : null;
            const to = order.estimatedDeliveryTo ? new Date(order.estimatedDeliveryTo) : null;
            const fmt = d => d.toLocaleDateString('vi-VN');
            if (from && to) {
                deliveryHtml = `<div>Ước tính giao hàng: <strong>${fmt(from)} → ${fmt(to)}</strong></div>`;
            } else if (from) {
                deliveryHtml = `<div>Ước tính giao hàng: <strong>${fmt(from)}</strong></div>`;
            } else {
                deliveryHtml = `<div>Ước tính giao hàng: <strong>${fmt(to)}</strong></div>`;
            }
        } catch (e) {
            deliveryHtml = '';
        }
    }

    const canReceive = !['CANCELLED', 'RECEIVED'].includes(String(order.status || '').toUpperCase());
    el.innerHTML = `
        <div style="font-weight:700;margin-bottom:6px">${invoice}</div>
        <div>Trạng thái: <strong>${status}</strong></div>
        <div>Khách hàng: ${customer} · ${phone}</div>
        <div>Thời gian: ${created}</div>
        ${deliveryHtml}
        <div style="margin-top:6px">Tổng: <strong>${total}</strong></div>
        ${itemsHtml}
        ${canReceive ? `<div style="margin-top:12px"><button id="receiveOrderDetailBtn" class="ghost-btn">Đã nhận được hàng</button></div>` : ''}
    `;

    const receiveBtn = el.querySelector('#receiveOrderDetailBtn');
    if (receiveBtn) {
        receiveBtn.addEventListener('click', async () => {
            const confirmed = await confirmOrderReceived(order.id);
            if (confirmed) {
                await refreshSelectedCustomerPointsForOrder(order);
                await loadOrdersForModal(getCurrentUserId());
            }
        });
    }
}

function canShowReceiveButton(order) {
    if (!order) return false;
    const status = String(order.status || '').trim().toUpperCase();
    if (status === 'CANCELLED' || status === 'RECEIVED') return false;
    return !['CANCELLED', 'RECEIVED'].includes(status);
}

async function confirmOrderReceived(orderId) {
    if (!orderId) return false;
    if (!confirm('Xác nhận đã nhận hàng và cộng điểm vào tài khoản khách?')) return false;
    const headers = getAuthHeaders();
    const endpoints = [
        `${API_BASE}/orders/${orderId}/received`,
        `${API_BASE}/orders/${orderId}/confirm-received`,
        `${API_BASE}/orders/received/${orderId}`,
        `${API_BASE}/orders/confirm-received/${orderId}`
    ];

    for (const url of endpoints) {
        try {
            const res = await fetch(url, { method: 'POST', headers });
            const contentType = res.headers.get('content-type') || '';
            const text = await res.text();
            let body = null;
            if (contentType.includes('application/json')) {
                try {
                    body = JSON.parse(text || '{}');
                } catch (err) {
                    body = null;
                }
            }

            if (res.ok) {
                if (body && body.message) {
                    const details = body.pointsAdded != null ? `\nĐã cộng: ${body.pointsAdded} điểm` : '';
                    const message = `${body.message || 'Đã xác nhận nhận hàng'}${details}`;
                    alert(message);
                    if (body && body.pointsAdded != null) {
                        try { showPointsAdded(Number(body.pointsAdded)); } catch (e) { }
                    }
                } else {
                    alert(text || 'Đã xác nhận nhận hàng. Điểm đã được cộng nếu có khách hàng hợp lệ.');
                }

                // Show review modal after successful confirmation
                setTimeout(() => {
                    showReviewModal(orderId);
                }, 500);

                return true;
            }

            if (res.status !== 404) {
                if (body && body.message) {
                    alert(`Không thể xác nhận đơn hàng: ${body.message}`);
                } else {
                    alert(`Không thể xác nhận đơn hàng: ${text || res.statusText}`);
                }
                return false;
            }
        } catch (err) {
            console.warn('confirmOrderReceived attempt failed for', url, err);
        }
    }

    alert('Không thể xác nhận đơn hàng: endpoint chưa được hỗ trợ hoặc đơn hàng không tồn tại.');
    return false;
}

function renderOrderSummaryResult(summary) {
    const el = document.getElementById('orderTrackingResult');
    if (!el || !summary) return;
    const created = formatDateTime(summary.createdAt || new Date());
    const invoice = escapeHtml(summary.invoiceNumber || (summary.id ? `#${summary.id}` : '-'));
    const status = getVNStatus(summary.status);
    const total = formatPrice(summary.totalAmount || 0);
    el.innerHTML = `
        <div style="font-weight:700;margin-bottom:6px">${invoice}</div>
        <div>Trạng thái: <strong>${status}</strong></div>
        <div>Thời gian: ${created}</div>
        <div style="margin-top:6px">Tổng: <strong>${total}</strong></div>
    `;
}

// Review Migration for backward compatibility
function migrateReviewsFormat() {
    try {
        const reviews = JSON.parse(localStorage.getItem('bizflow_reviews') || '[]');
        if (!Array.isArray(reviews) || reviews.length === 0) return;

        let hasOldFormat = false;
        const migratedReviews = [];

        reviews.forEach(r => {
            if (r.productIds && Array.isArray(r.productIds) && r.productIds.length > 0) {
                // New format - keep it
                migratedReviews.push(r);
            } else {
                // Old format without productIds - mark as needing update
                hasOldFormat = true;
                console.log('Found old review format to migrate', r);
            }
        });

        // Save only migrated (new format) reviews
        if (hasOldFormat && migratedReviews.length >= 0) {
            localStorage.setItem('bizflow_reviews', JSON.stringify(migratedReviews));
            console.log(`Migrated reviews: removed ${reviews.length - migratedReviews.length} old format reviews`);
        }
    } catch (e) {
        console.warn('Failed to migrate reviews format', e);
    }
}

// Review Modal Functions
let currentReviewOrderId = null;
let currentReviewRating = 0;
let currentReviewOrderItems = []; // Store order items to get productIds

function showReviewModal(orderId) {
    currentReviewOrderId = orderId;
    currentReviewRating = 0;
    currentReviewOrderItems = [];

    const modal = document.getElementById('reviewModal');
    const orderIdEl = document.getElementById('reviewOrderId');
    const comment = document.getElementById('reviewComment');
    const ratingHidden = document.getElementById('reviewRatingHidden');

    if (orderIdEl) orderIdEl.textContent = orderId || '-';
    if (comment) comment.value = '';
    if (ratingHidden) ratingHidden.value = '0';

    // Reset star rating display
    document.querySelectorAll('.rating-star').forEach((star, index) => {
        star.style.color = '#ddd';
        star.setAttribute('data-rating', index + 1);
    });
    const ratingText = document.getElementById('ratingText');
    if (ratingText) ratingText.textContent = 'Chưa chọn';

    // Fetch order details to get product information
    fetchOrderDetails(orderId);

    // Setup star click handlers
    setupRatingStars();

    if (modal) {
        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
    }
}

async function fetchOrderDetails(orderId) {
    try {
        const headers = getAuthHeaders();
        const endpoints = [
            `${API_BASE}/orders/${orderId}`,
            `${API_BASE}/orders/detail/${orderId}`
        ];

        for (const url of endpoints) {
            try {
                const res = await fetch(url, { headers });
                if (res.ok) {
                    const order = await res.json();
                    if (order.items && Array.isArray(order.items)) {
                        currentReviewOrderItems = order.items;
                        console.log('Order items loaded', currentReviewOrderItems);
                        break;
                    }
                }
            } catch (e) {
                console.warn('Failed to fetch from', url, e);
            }
        }
    } catch (err) {
        console.warn('fetchOrderDetails error', err);
    }
}

function closeReviewModal() {
    const modal = document.getElementById('reviewModal');
    if (modal) {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
    }
    currentReviewOrderId = null;
    currentReviewRating = 0;
}

function setupRatingStars() {
    document.querySelectorAll('.rating-star').forEach(star => {
        star.addEventListener('click', (e) => {
            const rating = parseInt(e.target.getAttribute('data-rating'), 10);
            currentReviewRating = rating;

            // Update star colors
            document.querySelectorAll('.rating-star').forEach((s, index) => {
                if (index < rating) {
                    s.style.color = '#ffc107';
                } else {
                    s.style.color = '#ddd';
                }
            });

            // Update rating text
            const ratingText = document.getElementById('ratingText');
            const ratingLabels = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'];
            if (ratingText) ratingText.textContent = ratingLabels[rating] || 'Chưa chọn';

            // Store rating
            const ratingHidden = document.getElementById('reviewRatingHidden');
            if (ratingHidden) ratingHidden.value = rating;
        });

        // Add hover effect
        star.addEventListener('mouseover', (e) => {
            const rating = parseInt(e.target.getAttribute('data-rating'), 10);
            document.querySelectorAll('.rating-star').forEach((s, index) => {
                if (index < rating) {
                    s.style.color = '#ffc107';
                } else {
                    s.style.color = '#ddd';
                }
            });
        });
    });

    // Reset on mouse leave
    const starsContainer = document.querySelector('.rating-star').parentElement;
    if (starsContainer) {
        starsContainer.addEventListener('mouseleave', () => {
            document.querySelectorAll('.rating-star').forEach((s, index) => {
                if (index < currentReviewRating) {
                    s.style.color = '#ffc107';
                } else {
                    s.style.color = '#ddd';
                }
            });
        });
    }
}

function submitReview() {
    if (!currentReviewOrderId) {
        alert('Không tìm thấy đơn hàng. Vui lòng thử lại.');
        return;
    }

    const rating = currentReviewRating || 0;
    const comment = (document.getElementById('reviewComment')?.value || '').trim();

    if (rating === 0) {
        alert('Vui lòng chọn mức độ hài lòng trước khi gửi đánh giá.');
        return;
    }

    // Get product IDs from order items
    const productIds = currentReviewOrderItems.map(item => item.productId || item.id).filter(id => id);

    // If productIds is empty, show warning and don't submit
    if (productIds.length === 0) {
        alert('Không thể tải thông tin sản phẩm từ đơn hàng. Vui lòng làm mới trang và thử lại.');
        return;
    }

    // Prepare review data - store for all products in the order
    const reviewData = {
        orderId: currentReviewOrderId,
        productIds: productIds,  // Array of product IDs in this order
        rating: rating,
        comment: comment,
        userId: getCurrentUserId(),
        username: sessionStorage.getItem('username') || 'Khách hàng',
        createdAt: new Date().toISOString()
    };

    // Store in localStorage as backup (since backend may not have review endpoints yet)
    try {
        const reviews = JSON.parse(localStorage.getItem('bizflow_reviews') || '[]');
        reviews.push(reviewData);
        localStorage.setItem('bizflow_reviews', JSON.stringify(reviews));
        console.log('Review saved to localStorage', reviewData);
    } catch (e) {
        console.warn('Failed to save review to localStorage', e);
    }

    // Try to send to backend API
    const headers = getAuthHeaders();
    const endpoints = [
        `${API_BASE}/orders/${currentReviewOrderId}/review`,
        `${API_BASE}/reviews/create`,
        `${API_BASE}/feedback/create`
    ];

    Promise.resolve().then(() => {
        for (const url of endpoints) {
            fetch(url, {
                method: 'POST',
                headers: { ...headers, 'Content-Type': 'application/json' },
                body: JSON.stringify(reviewData)
            })
                .then(res => {
                    if (res.ok) {
                        console.log('Review sent to backend', url);
                        return;
                    }
                })
                .catch(err => console.warn('Failed to send review to', url, err));
        }
    });

    // Show success message and redirect
    alert(`Cảm ơn bạn đã đánh giá! ${rating} sao - "${comment}"`);
    closeReviewModal();
}

function setupReviewModalHandlers() {
    const closeBtn = document.getElementById('closeReviewModal');
    const skipBtn = document.getElementById('skipReviewBtn');
    const submitBtn = document.getElementById('submitReviewBtn');

    if (closeBtn) closeBtn.addEventListener('click', closeReviewModal);
    if (skipBtn) skipBtn.addEventListener('click', closeReviewModal);
    if (submitBtn) submitBtn.addEventListener('click', submitReview);

    // Setup handlers for view reviews modal
    const closeViewReviewsBtn = document.getElementById('closeViewReviewsBtn');
    const closeViewReviewsModal = document.getElementById('closeViewReviewsModal');
    if (closeViewReviewsBtn) closeViewReviewsBtn.addEventListener('click', closeViewProductReviewsModal);
    if (closeViewReviewsModal) closeViewReviewsModal.addEventListener('click', closeViewProductReviewsModal);
}

// Product Reviews Viewing Functions
let currentViewProductId = null;
let currentViewProductName = null;

function showProductReviews(productId, productName) {
    currentViewProductId = productId;
    currentViewProductName = productName;

    const modal = document.getElementById('viewProductReviewsModal');
    const productNameEl = document.getElementById('reviewProductName');
    const productImageEl = document.getElementById('reviewProductImage');

    if (productNameEl) productNameEl.textContent = productName || 'Sản phẩm';

    // Fetch product image
    if (productImageEl) {
        (async () => {
            try {
                const response = await fetch(`${API_BASE}/products/${productId}`, {
                    headers: getAuthHeaders()
                });
                if (response.ok) {
                    const data = await response.json();
                    const product = data.data || data;
                    if (product.image || product.thumbnail) {
                        productImageEl.src = product.image || product.thumbnail;
                    }
                }
            } catch (e) {
                console.warn('Failed to fetch product image', e);
            }
        })();
    }

    // Get reviews from localStorage
    try {
        const allReviews = JSON.parse(localStorage.getItem('bizflow_reviews') || '[]');
        // Filter reviews for this product - check if productId is in the productIds array
        // Also support backward compatibility for reviews without productIds
        const productReviews = allReviews.filter(r => {
            const productIds = r.productIds || [];
            // New format: check if productId is in the array
            if (productIds.length > 0) {
                return productIds.includes(Number(productId)) || productIds.includes(String(productId));
            }
            // Old format backward compatibility: if no productIds but has orderId, show review
            // This is for reviews created before the fix
            return false; // Don't show old reviews without productIds
        });

        renderProductReviewsList(productReviews);
    } catch (e) {
        console.warn('Failed to load product reviews', e);
        renderProductReviewsList([]);
    }

    if (modal) {
        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
    }
}

function closeViewProductReviewsModal() {
    const modal = document.getElementById('viewProductReviewsModal');
    if (modal) {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
    }
    currentViewProductId = null;
    currentViewProductName = null;
}

function renderProductReviewsList(reviews) {
    const listEl = document.getElementById('reviewsList');
    const avgRatingEl = document.getElementById('reviewAverageRating');
    const avgStarsEl = document.getElementById('reviewAverageStars');
    const totalCountEl = document.getElementById('reviewTotalCount');

    if (!listEl) return;

    if (!reviews || reviews.length === 0) {
        listEl.innerHTML = '<div style="text-align:center;color:#999;padding:40px 20px;"><div style="font-size:48px;margin-bottom:12px;">⭐</div><div>Chưa có đánh giá nào</div></div>';
        if (avgRatingEl) avgRatingEl.textContent = '0.0';
        if (avgStarsEl) avgStarsEl.innerHTML = '☆☆☆☆☆';
        if (totalCountEl) totalCountEl.textContent = '0 đánh giá';
        return;
    }

    // Calculate average rating
    const totalRating = reviews.reduce((sum, r) => sum + (Number(r.rating) || 0), 0);
    const avgRating = (totalRating / reviews.length).toFixed(1);

    // Update stats
    if (avgRatingEl) avgRatingEl.textContent = avgRating;
    if (totalCountEl) totalCountEl.textContent = `${reviews.length} đánh giá`;

    // Render star display
    const fullStars = Math.floor(avgRating);
    const hasHalfStar = avgRating % 1 >= 0.5;
    let starDisplay = '';
    for (let i = 0; i < 5; i++) {
        if (i < fullStars) starDisplay += '★';
        else if (i === fullStars && hasHalfStar) starDisplay += '⯨';
        else starDisplay += '☆';
    }
    if (avgStarsEl) avgStarsEl.innerHTML = '<span style="color:#ffc107;letter-spacing:2px;">' + starDisplay + '</span>';

    // Render reviews
    const ratingLabels = ['', 'Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt'];
    const ratingColors = ['', '#ef4444', '#f97316', '#f59e0b', '#10b981', '#06b6d4'];

    const html = reviews.map((r, idx) => {
        const stars = '★'.repeat(r.rating) + '☆'.repeat(5 - r.rating);
        const ratingLabel = ratingLabels[r.rating] || 'Không xác định';
        const ratingColor = ratingColors[r.rating] || '#6b7280';
        const date = r.createdAt ? new Date(r.createdAt).toLocaleDateString('vi-VN') : 'N/A';
        const username = r.username || 'Khách hàng';
        const userInitial = username.charAt(0).toUpperCase();
        const comment = r.comment ? `<div style="margin-top:12px;padding:12px;background:#f5f5f5;border-radius:6px;color:#555;font-size:13px;line-height:1.5;">"${escapeHtml(r.comment)}"</div>` : '';
        const reply = r.reply ? `
            <div style="margin-top:12px;padding:12px;background:linear-gradient(135deg, #e0f2fe 0%, #cffafe 100%);border-left:4px solid #06b6d4;border-radius:6px;">
                <div style="font-weight:600;color:#0369a1;font-size:12px;margin-bottom:6px;display:flex;align-items:center;gap:6px;"><span>💬</span> Trả lời từ quản trị viên</div>
                <div style="color:#164e63;font-size:12px;line-height:1.5;">${escapeHtml(r.reply)}</div>
            </div>
        ` : '';

        return `
            <div style="padding:16px;background:#fff;border-radius:10px;border:1px solid #e5e7eb;box-shadow:0 1px 3px rgba(0,0,0,0.08);transition:all 0.2s ease;position:relative;overflow:hidden;">
                <div style="position:absolute;top:0;left:0;width:4px;height:100%;background:${ratingColor};"></div>
                <div style="display:flex;gap:12px;">
                    <div style="width:40px;height:40px;border-radius:50%;background:linear-gradient(135deg, ${ratingColor}, ${ratingColor}cc);color:white;display:flex;align-items:center;justify-content:center;font-weight:700;flex-shrink:0;">${userInitial}</div>
                    <div style="flex:1;">
                        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">
                            <div>
                                <div style="font-weight:700;color:#1f2937;font-size:14px;">${escapeHtml(username)}</div>
                                <div style="font-size:12px;color:#6b7280;margin-top:2px;">${date}</div>
                            </div>
                            <div style="display:inline-flex;align-items:center;gap:6px;padding:4px 10px;background:linear-gradient(135deg, #fbbf24, #f59e0b);color:white;border-radius:12px;font-weight:600;font-size:11px;">
                                <span>${stars}</span>
                                <span>${ratingLabel}</span>
                            </div>
                        </div>
                        ${comment}
                        ${reply}
                    </div>
                </div>
            </div>
        `;
    }).join('');

    listEl.innerHTML = html;
}

// Call setup when document is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setupReviewModalHandlers);
} else {
    setupReviewModalHandlers();
}

function setupReviewModalHandlers() {
    const closeBtn = document.getElementById('closeReviewModal');
    const skipBtn = document.getElementById('skipReviewBtn');
    const submitBtn = document.getElementById('submitReviewBtn');

    if (closeBtn) closeBtn.addEventListener('click', closeReviewModal);
    if (skipBtn) skipBtn.addEventListener('click', closeReviewModal);
    if (submitBtn) submitBtn.addEventListener('click', submitReview);

    // Setup handlers for view reviews modal
    const closeViewReviewsBtn = document.getElementById('closeViewReviewsBtn');
    const closeViewReviewsModal = document.getElementById('closeViewReviewsModal');
    if (closeViewReviewsBtn) closeViewReviewsBtn.addEventListener('click', closeViewProductReviewsModal);
    if (closeViewReviewsModal) closeViewReviewsModal.addEventListener('click', closeViewProductReviewsModal);
}

function getActiveInvoice() {
    return invoices.find(inv => inv.id === activeInvoiceId) || null;
}

function cloneCart(items) {
    return (items || []).map(item => ({ ...item }));
}

function saveActiveInvoiceState() {
    const invoice = getActiveInvoice();
    if (!invoice) return;
    invoice.cart = cloneCart(cart);
    // Persist minimal customer info only. Avoid saving point counts so
    // saved drafts cannot overwrite server totals on restore.
    invoice.selectedCustomer = selectedCustomer
        ? { id: selectedCustomer.id || 0, name: selectedCustomer.name || 'Khách lẻ', phone: selectedCustomer.phone || '-' }
        : { id: 0, name: 'Khách lẻ', phone: '-' };
    invoice.paymentMethod = currentPaymentMethod;
    invoice.cashReceived = document.getElementById('cashReceivedInput')?.value || '';
    invoice.paymentNote = document.getElementById('paymentNote')?.value || '';
    invoice.splitLine = document.getElementById('splitLine')?.checked || false;
    invoice.topSearchTerm = topSearchTerm || '';
    invoice.bottomSearchTerm = bottomSearchTerm || '';
}

function applyInvoiceState(invoice) {
    cart = cloneCart(invoice.cart);
    currentPaymentMethod = invoice.paymentMethod || 'CASH';
    topSearchTerm = invoice.topSearchTerm || '';
    bottomSearchTerm = invoice.bottomSearchTerm || '';
    const topSearchInput = document.getElementById('searchInput');
    if (topSearchInput) {
        topSearchInput.value = topSearchTerm;
    }
    const bottomSearchInput = document.getElementById('suggestionSearchInput');
    if (bottomSearchInput) {
        bottomSearchInput.value = bottomSearchTerm;
    }
    const splitLine = document.getElementById('splitLine');
    if (splitLine) {
        splitLine.checked = !!invoice.splitLine;
    }

    const cashInput = document.getElementById('cashReceivedInput');
    if (cashInput) {
        cashInput.value = invoice.cashReceived || '';
    }
    const noteInput = document.getElementById('paymentNote');
    if (noteInput) {
        noteInput.value = invoice.paymentNote || '';
    }

    const methodInputs = document.querySelectorAll('input[name="paymentMethod"]');
    methodInputs.forEach(input => {
        input.checked = input.value === currentPaymentMethod;
    });
    updatePaymentPanels();

    if (invoice.selectedCustomer && invoice.selectedCustomer.id > 0) {
        const sc = invoice.selectedCustomer;
        // Apply the restored customer immediately so the UI can show the
        // selected customer and use any cached point fallback values.
        applyCustomerSelection(sc, { openDetail: false });

        const scPoints = Number(sc.totalPoints ?? sc.total_points ?? NaN);
        if (!Number.isFinite(scPoints) || scPoints <= 0) {
            try { refreshCustomerDetailFromApi(Number(sc.id)); } catch (e) { }
        }
    } else {
        // Account-based loyalty: if a logged-in account customer is already
        // selected, keep it instead of resetting to guest when a draft has no
        // explicit customer attached.
        if (!(selectedCustomer && Number(selectedCustomer.id) > 0 && getCurrentUserId())) {
            clearSelectedCustomer({ showList: false });
        }
    }

    renderCart();
    updateTotal();
    filterProducts();
}

function renderInvoiceTabs() {
    const container = document.getElementById('orderTabs');
    if (!container) return;
    const tabs = invoices.map(invoice => `
        <button class="order-tab ${invoice.id === activeInvoiceId ? 'active' : ''}" data-invoice-id="${invoice.id}">
            ${invoice.name} <span class="tab-close" data-close="${invoice.id}">\u00d7</span>
        </button>
    `).join('');
    container.innerHTML = `
        ${tabs}
        <button class="order-tab ghost" id="addInvoiceBtn" title="Th\u00eam gi\u1ecf h\u00e0ng">+</button>
        <button class="order-tab ghost" id="savedInvoiceBtn"><span class="saved-cart-icon" aria-hidden="true">&#128722;</span>Gi\u1ecf h\u00e0ng</button>
        <button class="order-tab ghost" id="trackOrdersHeaderBtn" title="Theo dõi đơn hàng">Theo dõi đơn hàng</button>
    `;
}

function setupInvoiceTabs() {
    const container = document.getElementById('orderTabs');
    if (!container) return;

    container.addEventListener('click', (e) => {
        const target = e.target instanceof Element ? e.target : e.target.parentElement;
        const trackBtnClicked = target?.closest('#trackOrdersHeaderBtn');
        if (trackBtnClicked) {
            openOrderTrackModal();
            return;
        }
        const addBtn = target?.closest('#addInvoiceBtn');
        if (addBtn) {
            createAndSwitchInvoice();
            return;
        }

        const savedBtn = target?.closest('#savedInvoiceBtn');
        if (savedBtn) {
            toggleSavedBillsPanel();
            return;
        }

        const closeBtn = target?.closest('.tab-close');
        if (closeBtn) {
            e.stopPropagation();
            const invoiceId = closeBtn.getAttribute('data-close');
            if (invoiceId && confirm('X\u00f3a gi\u1ecf h\u00e0ng n\u00e0y?')) {
                removeInvoice(invoiceId);
            }
            return;
        }

        const tab = target?.closest('.order-tab[data-invoice-id]');
        if (tab) {
            const invoiceId = tab.getAttribute('data-invoice-id');
            if (invoiceId) {
                switchInvoice(invoiceId);
            }
        }
    });

    const savedPanel = document.getElementById('savedBillsPanel');
    const closeSavedBtn = document.getElementById('closeSavedBills');
    closeSavedBtn?.addEventListener('click', () => toggleSavedBillsPanel(false));

    document.addEventListener('click', (e) => {
        if (!savedPanel || savedPanel.getAttribute('aria-hidden') === 'true') return;
        const savedBtn = document.getElementById('savedInvoiceBtn');
        if (savedPanel.contains(e.target) || savedBtn?.contains(e.target)) return;
        toggleSavedBillsPanel(false);
    });

    savedPanel?.addEventListener('click', (e) => {
        const openBtn = e.target.closest('[data-open-draft]');
        if (openBtn) {
            const draftId = openBtn.getAttribute('data-open-draft');
            if (draftId) {
                openSavedInvoice(draftId);
            }
        }
        const removeBtn = e.target.closest('[data-remove-draft]');
        if (removeBtn) {
            const draftId = removeBtn.getAttribute('data-remove-draft');
            if (draftId) {
                removeSavedInvoice(draftId);
            }
        }
    });
}

// Order tracking modal + actions
function setupOrderTracking() {
    const btn = document.getElementById('trackOrdersBtn');
    const headerBtn = document.getElementById('trackOrdersHeaderBtn');
    const modal = document.getElementById('orderTrackModal');
    const closeBtn = document.getElementById('closeOrderTrackModal');
    const closeFooter = document.getElementById('closeOrderTrackBtn');
    const findBtn = document.getElementById('orderTrackFindBtn');
    const phoneInput = document.getElementById('orderTrackPhoneInput');

    [btn, headerBtn].forEach(b => {
        if (b) b.addEventListener('click', async () => openOrderTrackModal());
    });
    if (closeBtn) {
        closeBtn.addEventListener('click', () => closeOrderTrackModal());
    }
    if (closeFooter) {
        closeFooter.addEventListener('click', () => closeOrderTrackModal());
    }
    if (findBtn) {
        findBtn.addEventListener('click', async () => {
            await loadOrdersForModal(getCurrentUserId());
        });
    }
    if (phoneInput) {
        phoneInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                findBtn.click();
            }
        });
    }
}

async function openOrderTrackModal() {
    const modal = document.getElementById('orderTrackModal');
    const controls = document.getElementById('orderTrackControls');
    if (!modal) return;
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');

    const info = document.getElementById('orderTrackCustomerInfo');
    const phoneInput = document.getElementById('orderTrackPhoneInput');

    const showSearchControls = (show) => {
        if (controls) {
            controls.style.display = show ? 'flex' : 'none';
        }
    };

    showSearchControls(false);

    const userId = getCurrentUserId();
    if (userId) {
        if (info) info.textContent = 'Đang tải đơn hàng của tài khoản...';
        if (phoneInput) phoneInput.value = '';
        await loadOrdersForModal(userId);
        return;
    }

    if (info) info.textContent = 'Bạn cần đăng nhập để xem đơn hàng của tài khoản.';
    if (phoneInput) phoneInput.value = '';
    const list = document.getElementById('orderTrackList');
    if (list) list.innerHTML = '<div class="muted">Chưa đăng nhập. Vui lòng đăng nhập để xem đơn hàng của tài khoản.</div>';
}

function closeOrderTrackModal() {
    const modal = document.getElementById('orderTrackModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

async function loadOrdersForModal(userId) {
    const listEl = document.getElementById('orderTrackList');
    if (!listEl) return;
    
    const phoneInput = document.getElementById('orderTrackPhoneInput');
    const searchTerm = phoneInput ? phoneInput.value.trim() : '';

    listEl.innerHTML = '<div class="muted">Đang tải...</div>';

    try {
        const headers = getAuthHeaders();
        let orders = [];

        // Case 1: Search by specific phone or invoice if provided
        if (searchTerm) {
            const res = await fetch(`${API_BASE}/orders/search?keyword=${encodeURIComponent(searchTerm)}&phone=${encodeURIComponent(searchTerm)}`, { headers });
            if (res.ok) {
                orders = await res.json();
            }
        } 
        // Case 2: Fallback to current user's orders if no search term
        else if (userId) {
            const res = await fetch(`${API_BASE}/orders/user/${userId}`, { headers });
            if (res.ok) {
                orders = await res.json();
            }

            if (!orders || (Array.isArray(orders) && orders.length === 0)) {
                const summaryRes = await fetch(`${API_BASE}/orders/summary`, { headers });
                if (summaryRes.ok) {
                    const summary = await summaryRes.json();
                    orders = Array.isArray(summary) ? summary.filter(o => Number(o.userId) === Number(userId)) : [];
                }
            }
        }

        if (!orders || (Array.isArray(orders) && orders.length === 0)) {
            listEl.innerHTML = '<div class="muted">Không tìm thấy đơn hàng nào. Vui lòng kiểm tra lại mã hoặc SĐT.</div>';
            return;
        }

        // Sort by date desc
        orders.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

        renderOrdersInModal(orders);
    } catch (err) {
        console.warn('loadOrdersForModal error', err);
        listEl.innerHTML = '<div class="muted">Lỗi khi tải đơn hàng. Vui lòng thử lại.</div>';
    }
}

function renderOrdersInModal(orders) {
    const listEl = document.getElementById('orderTrackList');
    if (!listEl) return;
    if (!orders || orders.length === 0) {
        listEl.innerHTML = `
            <div style="text-align:center;padding:40px 20px;color:#999;">
                <div style="font-size:40px;margin-bottom:12px;">📭</div>
                <div>Không tìm thấy đơn hàng nào</div>
            </div>
        `;
        return;
    }

    const html = orders.map(o => {
        const created = formatDateTime(o.createdAt || '-');
        const invoice = o.invoiceNumber || ('#' + (o.id || '-'));
        const status = (o.status || 'PENDING').toUpperCase();
        const total = formatPrice(o.totalAmount || o.total || 0);
        
        // Premium Status Styles
        const statusConfigs = {
            'PENDING': { label: 'Đơn mới', color: '#ff9800', bg: '#fff7ed' },
            'UNPAID': { label: 'Đơn mới', color: '#ef4444', bg: '#fef2f2' },
            'PAID': { label: 'Đã xác nhận (Chờ đóng gói)', color: '#00bfa5', bg: '#f0fdfa' },
            'CONFIRMED': { label: 'Đã xác nhận', color: '#2563eb', bg: '#eff6ff' },
            'PACKING': { label: 'Đang đóng gói', color: '#8b5cf6', bg: '#f5f3ff' },
            'PROCESSING': { label: 'Đang đóng gói', color: '#8b5cf6', bg: '#f5f3ff' },
            'SHIPPING': { label: 'Đang giao hàng', color: '#7c3aed', bg: '#f5f3ff' },
            'SHIPPED': { label: 'Đang giao hàng', color: '#7c3aed', bg: '#f5f3ff' },
            'DELIVERED': { label: 'Giao hàng thành công', color: '#16a34a', bg: '#f0fdf4' },
            'CANCELLED': { label: 'Đã hủy', color: '#6b7280', bg: '#f3f4f6' },
            'RECEIVED': { label: 'Hoàn tất', color: '#10b981', bg: '#ecfdf5' }
        };
        const cfg = statusConfigs[status] || { label: status, color: '#333', bg: '#f5f5f5' };

        return `
            <div class="order-fancy-card" style="background:#fff;border:1px solid #eee;border-radius:12px;padding:16px;margin-bottom:12px;transition:all 0.2s;">
                <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px;">
                    <div>
                        <div style="font-weight:800;color:#111;font-size:15px;margin-bottom:2px;">${escapeHtml(invoice)}</div>
                        <div style="font-size:12px;color:#888;">${created}</div>
                    </div>
                    <span style="background:${cfg.bg};color:${cfg.color};padding:4px 10px;border-radius:8px;font-size:11px;font-weight:700;text-transform:uppercase;">${cfg.label}</span>
                </div>
                
                <div style="display:flex;justify-content:space-between;align-items:center;padding-top:12px;border-top:1px dashed #eee;">
                    <div>
                        <div style="font-size:12px;color:#666;margin-bottom:2px;">Tổng thanh toán</div>
                        <div style="font-weight:800;color:#ee4d2d;font-size:16px;">${total}</div>
                    </div>
                    <div style="display:flex;gap:6px;align-items:center;">
                        ${(status === 'DELIVERED') ? `
                            <button class="receive-order-btn" data-order-id="${o.id}" 
                                     style="background:#10b981;color:#fff;border:none;padding:8px 12px;border-radius:8px;font-size:12px;font-weight:700;cursor:pointer;">
                                Phê duyệt nhận hàng
                            </button>
                        ` : ''}
                        <button class="view-order-btn" data-order-id="${o.id}" data-invoice-number="${o.invoiceNumber || ''}" 
                                 style="background:#2563eb;color:#fff;border:none;padding:8px 12px;border-radius:8px;font-size:12px;font-weight:700;cursor:pointer;">
                            📍 Hành trình
                        </button>
                        ${(status !== 'CANCELLED' && status !== 'RECEIVED') ? `
                            <button class="cancel-order-btn" data-order-id="${o.id}" 
                                    style="background:#fef2f2;color:#ef4444;border:none;padding:8px;border-radius:8px;cursor:pointer;" title="Hủy đơn">
                                🗑️
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');

    listEl.innerHTML = `<div style="padding:4px;">${html}</div>`;

    // bind buttons
    listEl.querySelectorAll('.cancel-order-btn').forEach(b => {
        b.addEventListener('click', async (e) => {
            const id = b.getAttribute('data-order-id');
            if (!id) return;
            if (!confirm('Bạn có chắc muốn hủy đơn này?')) return;
            try {
                const headers = getAuthHeaders();
                const res = await fetch(`${API_BASE}/orders/${id}/cancel`, { method: 'POST', headers });
                if (!res.ok) {
                    alert('Không thể hủy đơn.');
                    return;
                }
                alert('Đã hủy đơn.');
                await loadOrdersForModal(getCurrentUserId());
            } catch (err) {
                console.warn('cancel error', err);
                alert('Lỗi khi hủy đơn.');
            }
        });
    });

    listEl.querySelectorAll('.receive-order-btn').forEach(b => {
        b.addEventListener('click', async () => {
            const id = b.getAttribute('data-order-id');
            if (!id) return;
            const order = orders.find(o => String(o.id) === String(id));
            const confirmed = await confirmOrderReceived(id);
            if (confirmed) {
                await refreshSelectedCustomerPointsForOrder(order);
                await loadOrdersForModal(getCurrentUserId());
            }
        });
    });

    listEl.querySelectorAll('.view-order-btn').forEach(b => {
        b.addEventListener('click', (e) => {
            const invoice = b.getAttribute('data-invoice-number');
            if (invoice) {
                window.location.href = `/pages/order-tracking.html?invoice=${encodeURIComponent(invoice)}`;
            } else {
                alert('Mã hóa đơn không tồn tại.');
            }
        });
    });
}

function createAndSwitchInvoice() {
    saveActiveInvoiceState();
    const invoice = createInvoiceState();
    invoices.push(invoice);
    activeInvoiceId = invoice.id;
    renderInvoiceTabs();
    applyInvoiceState(invoice);
    queuePersistCartState();
}

function switchInvoice(invoiceId) {
    if (invoiceId === activeInvoiceId) return;
    saveActiveInvoiceState();
    activeInvoiceId = invoiceId;
    const invoice = getActiveInvoice();
    if (invoice) {
        renderInvoiceTabs();
        applyInvoiceState(invoice);
        queuePersistCartState();
    }
}

function removeInvoice(invoiceId, options = {}) {
    const { resetSequence = true } = options;
    if (!invoiceId) return;
    const index = invoices.findIndex(inv => inv.id === invoiceId);
    if (index === -1) return;
    const wasActive = invoiceId === activeInvoiceId;
    invoices.splice(index, 1);
    if (invoices.length === 0) {
        if (resetSequence) {
            invoiceSequence = 1;
        }
        const fresh = createInvoiceState();
        invoices.push(fresh);
        activeInvoiceId = fresh.id;
        renderInvoiceTabs();
        applyInvoiceState(fresh);
        queuePersistCartState();
        return;
    }
    invoiceSequence = Math.max(invoiceSequence, getNextInvoiceNumber());
    if (wasActive) {
        activeInvoiceId = invoices[0].id;
        renderInvoiceTabs();
        applyInvoiceState(invoices[0]);
        queuePersistCartState();
    } else {
        renderInvoiceTabs();
        queuePersistCartState();
    }
}

function saveDraftInvoice() {
    if (cart.length === 0) {
        showPopup('Giỏ hàng trống, không thể thêm vào giỏ hàng.', { type: 'error' });
        return;
    }
    saveActiveInvoiceState();
    const invoice = getActiveInvoice();
    if (!invoice) return;
    const draft = {
        ...invoice,
        name: `Gi\u1ecf h\u00e0ng ${getNextInvoiceNumberFromAll()}`,
        cart: cloneCart(invoice.cart),
        savedAt: new Date().toISOString()
    };
    savedInvoices.unshift(draft);
    removeInvoice(invoice.id, { resetSequence: false });
    renderSavedBills();
    toggleSavedBillsPanel(false);
    queuePersistCartState();
}

function renderSavedBills() {
    const list = document.getElementById('savedBillsList');
    const empty = document.getElementById('savedBillsEmpty');
    if (!list || !empty) return;
    if (savedInvoices.length === 0) {
        list.innerHTML = '';
        empty.style.display = 'block';
        return;
    }
    empty.style.display = 'none';
    list.innerHTML = savedInvoices.map(draft => {
        const total = draft.cart.reduce((sum, item) => sum + (item.productPrice * item.quantity), 0);
        const customerName = (draft.selectedCustomer?.name || '').trim();
        const customerLabel = customerName && customerName.toLowerCase() !== 'khách lẻ'
            ? `<span>${escapeHtml(customerName)}</span>`
            : '';
        return `
            <div class="saved-bill-item">
                <button class="saved-bill-open" data-open-draft="${draft.id}">
                    <strong>${draft.name}</strong>
                    ${customerLabel}
                    <span>${formatPrice(total)}</span>
                </button>
                <button class="saved-bill-remove" data-remove-draft="${draft.id}">×</button>
            </div>
        `;
    }).join('');
}

function toggleSavedBillsPanel(forceState) {
    const panel = document.getElementById('savedBillsPanel');
    const button = document.getElementById('savedInvoiceBtn');
    if (!panel || !button) return;
    const show = typeof forceState === 'boolean' ? forceState : panel.getAttribute('aria-hidden') === 'true';
    if (show) {
        renderSavedBills();
        const rect = button.getBoundingClientRect();
        panel.style.top = `${rect.bottom + window.scrollY + 8}px`;
        panel.style.left = `${rect.left + window.scrollX}px`;
        panel.classList.add('show');
        panel.setAttribute('aria-hidden', 'false');
    } else {
        panel.classList.remove('show');
        panel.setAttribute('aria-hidden', 'true');
    }
}

function openSavedInvoice(draftId) {
    const index = savedInvoices.findIndex(draft => draft.id === draftId);
    if (index === -1) return;
    saveActiveInvoiceState();
    const draft = savedInvoices.splice(index, 1)[0];
    const emptyTarget = invoices.find(inv => isInvoiceEmpty(inv));
    if (emptyTarget) {
        emptyTarget.cart = cloneCart(draft.cart);
        emptyTarget.selectedCustomer = draft.selectedCustomer ? { ...draft.selectedCustomer } : { id: 0, name: 'Khách lẻ', phone: '-' };
        emptyTarget.paymentMethod = draft.paymentMethod || 'CASH';
        emptyTarget.cashReceived = draft.cashReceived || '';
        emptyTarget.paymentNote = draft.paymentNote || '';
        emptyTarget.splitLine = !!draft.splitLine;
        emptyTarget.topSearchTerm = draft.topSearchTerm || '';
        emptyTarget.bottomSearchTerm = draft.bottomSearchTerm || '';
        activeInvoiceId = emptyTarget.id;
    } else {
        const nextNumber = getNextInvoiceNumber();
        draft.name = `Gi\u1ecf h\u00e0ng ${nextNumber}`;
        invoiceSequence = Math.max(invoiceSequence, nextNumber + 1);
        invoices.push(draft);
        activeInvoiceId = draft.id;
    }
    renderInvoiceTabs();
    applyInvoiceState(getActiveInvoice());
    renderSavedBills();
    toggleSavedBillsPanel(false);
    queuePersistCartState();
}

function isInvoiceEmpty(invoice) {
    if (!invoice) return false;
    const hasItems = Array.isArray(invoice.cart) && invoice.cart.length > 0;
    const hasCustomer = invoice.selectedCustomer && invoice.selectedCustomer.id > 0;
    const hasNote = (invoice.paymentNote || '').trim().length > 0;
    const hasCash = (invoice.cashReceived || '').toString().trim().length > 0;
    const hasSearch = (invoice.topSearchTerm || '').trim().length > 0 || (invoice.bottomSearchTerm || '').trim().length > 0;
    return !hasItems && !hasCustomer && !hasNote && !hasCash && !hasSearch;
}

function removeSavedInvoice(draftId) {
    const index = savedInvoices.findIndex(draft => draft.id === draftId);
    if (index === -1) return;
    savedInvoices.splice(index, 1);
    renderSavedBills();
    queuePersistCartState();
}

function applyCustomerSelection(customer, options = {}) {
    const { openDetail = false, highlightEvent = null } = options;
    const effectiveTier = getEffectiveTier(customer);
    // Preserve existing in-memory points when the provided `customer` object
    // doesn't include point fields (this can happen when restoring saved
    // invoice/cart state which normalizes out point counts). If the incoming
    // customer lacks `totalPoints`/`monthlyPoints` but it's the same customer
    // currently selected, keep the previous values to avoid clobbering with 0.
    const isSameCustomer = selectedCustomer && selectedCustomer.id === customer.id;
    const existingTotal = isSameCustomer ? (Number.isFinite(Number(selectedCustomer.totalPoints)) ? Number(selectedCustomer.totalPoints) : selectedCustomer.totalPoints) : undefined;
    const existingMonthly = isSameCustomer ? (Number.isFinite(Number(selectedCustomer.monthlyPoints)) ? Number(selectedCustomer.monthlyPoints) : selectedCustomer.monthlyPoints) : undefined;

    // Prefer explicit incoming values when present. If missing, try to use
    // cached customer in-memory, then localStorage fallback, then existing
    // selectedCustomer (only when same customer). This avoids clobbering the
    // UI with 0 when the restored invoice didn't include point counts.
    let incomingTotal = (customer.totalPoints ?? customer.total_points);
    let incomingMonthly = (customer.monthlyPoints ?? customer.monthly_points);
    let resolvedTotal = null;
    let resolvedMonthly = null;

    if (incomingTotal == null || incomingMonthly == null) {
        // Try in-memory cache of `customers` list
        try {
            const cached = (customers || []).find(c => c && c.id === customer.id && (c.totalPoints != null || c.total_points != null));
            if (cached) {
                incomingTotal = incomingTotal == null ? (cached.totalPoints ?? cached.total_points) : incomingTotal;
                incomingMonthly = incomingMonthly == null ? (cached.monthlyPoints ?? cached.monthly_points) : incomingMonthly;
                console.debug('[applyCustomerSelection] using in-memory cached points for customer', customer.id);
            }
        } catch (e) {
            console.warn('[applyCustomerSelection] failed to lookup cached/local points', e);
        }
    }

    resolvedTotal = incomingTotal != null ? incomingTotal : (existingTotal != null ? existingTotal : 0);
    resolvedMonthly = incomingMonthly != null ? incomingMonthly : (existingMonthly != null ? existingMonthly : 0);

    selectedCustomer = {
        id: customer.id,
        name: customer.name,
        phone: customer.phone,
        totalPoints: resolvedTotal,
        monthlyPoints: resolvedMonthly,
        tier: effectiveTier || customer.tier || (isSameCustomer ? selectedCustomer.tier : '')
    };

    // Persist the selected customer state, but do not store points locally.
    try {
        if (selectedCustomer && selectedCustomer.id && Number(selectedCustomer.id) > 0) {
            persistSelectedCustomerState();
        }
    } catch (e) {
        console.warn('[applyCustomerSelection] failed to persist selectedCustomer state', e);
    }

    // If the passed customer is missing authoritative point fields, refresh
    // the full customer details in the background so the UI can be updated
    // with the server's values when available.
    if (customer && customer.id && (customer.totalPoints == null && customer.total_points == null)) {
        // fire-and-forget
        try { refreshCustomerDetailFromApi(customer.id).catch(() => { }); } catch (e) { }
    }

    document.querySelectorAll('.customer-item').forEach(item => {
        item.classList.remove('active');
    });
    const row = highlightEvent?.target?.closest?.('.customer-item');
    if (row) row.classList.add('active');

    const searchInput = document.getElementById('customerSearch');
    if (searchInput) {
        searchInput.value = customer.name || customer.phone || '';
        searchInput.classList.add('has-selection');
        searchInput.readOnly = true;
    }

    const selectedCustomerLabel = document.getElementById('selectedCustomer');
    if (selectedCustomerLabel) {
        selectedCustomerLabel.textContent = selectedCustomer.name || selectedCustomer.phone || 'Khách lẻ';
    }

    const addBtn = document.getElementById('addCustomerBtn');
    if (addBtn) {
        addBtn.style.display = 'none';
    }
    const clearBtn = document.getElementById('clearCustomerBtn');
    if (clearBtn) {
        clearBtn.style.display = 'inline-flex';
    }

    if (openDetail && customer.id) {
        openCustomerDetail(customer.id);
    }

    updateTotal();
    queuePersistCartState();
}

function setupCustomerModal() {
    const addButton = document.getElementById('addCustomerBtn');
    const modal = document.getElementById('customerModal');
    const closeBtn = document.getElementById('closeCustomerModal');
    const cancelBtn = document.getElementById('cancelCustomerModal');
    const form = document.getElementById('customerForm');
    const pointsInput = document.getElementById('customerPointsInput');
    const cityInput = document.getElementById('customerCityInput');
    const districtInput = document.getElementById('customerDistrictInput');
    const wardInput = document.getElementById('customerWardInput');

    if (!addButton || !modal || !closeBtn || !form) return;

    addButton.addEventListener('click', () => {
        const searchValue = document.getElementById('customerSearch')?.value.trim();
        const normalized = (searchValue || '').replace(/\s+/g, '');
        if (normalized) {
            const existing = customers.find(c => (c.phone || '').replace(/\s+/g, '') === normalized);
            if (existing) {
                selectCustomer(null, existing.id, existing.name, existing.phone || '-');
                return;
            }
        }
        openCustomerModalWithPhone(searchValue || '');
    });

    closeBtn.addEventListener('click', () => closeCustomerModal());
    cancelBtn?.addEventListener('click', () => closeCustomerModal());

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeCustomerModal();
        }
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await createCustomerFromForm();
    });

    pointsInput?.addEventListener('input', () => {
        const raw = parseInt(pointsInput.value, 10) || 0;
        setTierByPoints(raw);
    });

    cityInput?.addEventListener('focus', () => {
        renderDatalistOptions(document.getElementById('customerCityList'), cityCache);
    });

    cityInput?.addEventListener('input', () => {
        filterDatalist('customerCityList', cityCache, cityInput.value);
        const match = findMatchByName(cityCache, cityInput.value);
        if (match && cityInput.dataset.code !== String(match.code)) {
            cityInput.value = match.displayName || match.name;
            cityInput.dataset.code = match.code;
            resetAddressInput(districtInput);
            resetAddressInput(wardInput);
            loadDistricts(match.code);
            return;
        }
        if (!match) {
            cityInput.dataset.code = '';
            resetAddressInput(districtInput);
            resetAddressInput(wardInput);
        }
    });

    districtInput?.addEventListener('focus', () => {
        renderDatalistOptions(document.getElementById('customerDistrictList'), districtCache);
    });

    districtInput?.addEventListener('input', () => {
        filterDatalist('customerDistrictList', districtCache, districtInput.value);
        const match = findMatchByName(districtCache, districtInput.value);
        if (match && districtInput.dataset.code !== String(match.code)) {
            districtInput.value = match.displayName || match.name;
            districtInput.dataset.code = match.code;
            resetAddressInput(wardInput);
            loadWards(match.code);
            return;
        }
        if (!match) {
            districtInput.dataset.code = '';
            resetAddressInput(wardInput);
        }
    });

    wardInput?.addEventListener('focus', () => {
        renderDatalistOptions(document.getElementById('customerWardList'), wardCache);
    });

    wardInput?.addEventListener('input', () => {
        filterDatalist('customerWardList', wardCache, wardInput.value);
        const match = findMatchByName(wardCache, wardInput.value);
        if (match) {
            wardInput.value = match.displayName || match.name;
            wardInput.dataset.code = match.code;
        } else {
            wardInput.dataset.code = '';
        }
    });
}

function setupProductDetailModal() {
    const modal = document.getElementById('productDetailModal');
    if (!modal) return;

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeProductDetail();
        }
    });
}

function setupInventoryModal() {
    const modal = document.getElementById('inventoryModal');
    if (!modal) return;

    const closeBtn = document.getElementById('closeInventoryModal');
    const cancelBtn = document.getElementById('cancelInventoryBtn');
    const saveBtn = document.getElementById('saveInventoryBtn');
    const productSelect = document.getElementById('inventoryProductSelect');

    closeBtn?.addEventListener('click', closeInventoryModal);
    cancelBtn?.addEventListener('click', closeInventoryModal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeInventoryModal();
        }
    });

    productSelect?.addEventListener('change', () => {
        const selected = parseInt(productSelect.value, 10);
        if (!Number.isFinite(selected)) return;
        activeInventoryProductId = selected;
        updateInventoryStockDisplay(selected);
        loadInventoryHistory(selected);
    });

    saveBtn?.addEventListener('click', submitInventoryReceipt);
}

function setupCustomerDetailModal() {
    const modal = document.getElementById('customerDetailModal');
    if (!modal) return;
    const selectedView = document.getElementById('selectedCustomer');

    selectedView?.addEventListener('click', () => {
        if (!selectedCustomer || !selectedCustomer.id) return;
        openCustomerDetail(selectedCustomer.id);
    });

    const tabs = modal.querySelectorAll('.detail-tabs .tab[data-tab]');
    tabs.forEach((tab) => {
        tab.addEventListener('click', () => {
            const key = tab.dataset.tab;
            if (!key) return;
            switchCustomerDetailTab(key);
        });
    });

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeCustomerDetail();
        }
    });
}

function openCustomerModalWithPhone(phone) {
    const modal = document.getElementById('customerModal');
    const form = document.getElementById('customerForm');
    if (!modal || !form) return;

    editingCustomerId = null;
    setAddressRequired(true);
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    form.reset();
    const phoneInput = document.getElementById('customerPhoneInput');
    if (phoneInput) {
        phoneInput.value = phone || document.getElementById('customerSearch')?.value.trim() || '';
    }
    const confirmInput = document.getElementById('customerConfirmInput');
    if (confirmInput) {
        confirmInput.checked = false;
    }
    const cityInput = document.getElementById('customerCityInput');
    const districtInput = document.getElementById('customerDistrictInput');
    const wardInput = document.getElementById('customerWardInput');
    if (cityInput) {
        cityInput.value = '';
        cityInput.dataset.code = '';
    }
    resetAddressInput(districtInput);
    resetAddressInput(wardInput);
    const districtList = document.getElementById('customerDistrictList');
    const wardList = document.getElementById('customerWardList');
    if (districtList) districtList.innerHTML = '';
    if (wardList) wardList.innerHTML = '';

    document.getElementById('customerNameInput')?.focus();
    loadCities();
}

function closeCustomerModal() {
    const modal = document.getElementById('customerModal');
    if (!modal) return;
    editingCustomerId = null;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function setAddressRequired(required) {
    const cityInput = document.getElementById('customerCityInput');
    const districtInput = document.getElementById('customerDistrictInput');
    const wardInput = document.getElementById('customerWardInput');
    if (cityInput) cityInput.required = required;
    if (districtInput) districtInput.required = required;
    if (wardInput) wardInput.required = required;
}

async function createCustomerFromForm() {
    const name = document.getElementById('customerNameInput')?.value.trim();
    const phone = document.getElementById('customerPhoneInput')?.value.trim();
    const email = document.getElementById('customerEmailInput')?.value.trim();
    const address = document.getElementById('customerAddressInput')?.value.trim();
    const cccd = document.getElementById('customerCccdInput')?.value.trim();
    const dob = document.getElementById('customerDobInput')?.value;
    const gender = document.querySelector('input[name="customerGender"]:checked')?.value || 'UNKNOWN';
    const cityInput = document.getElementById('customerCityInput');
    const districtInput = document.getElementById('customerDistrictInput');
    const wardInput = document.getElementById('customerWardInput');
    const confirmed = document.getElementById('customerConfirmInput')?.checked;
    const isEditing = Boolean(editingCustomerId);

    if (!name) {
        showPopup('Vui lòng nhập tên khách hàng.', { type: 'error' });
        return;
    }

    const normalizedPhone = (phone || '').replace(/\s+/g, '');
    if (normalizedPhone) {
        const existing = customers.find(c => (c.phone || '').replace(/\s+/g, '') === normalizedPhone);
        if (existing) {
            selectCustomer(null, existing.id, existing.name, existing.phone || '-');
            closeCustomerModal();
            return;
        }
    }

    if (!phone) {
        showPopup('Vui lòng nhập số điện thoại.', { type: 'error' });
        return;
    }
    if (!/^\d{9,11}$/.test(phone)) {
        showPopup('Số điện thoại phải là 9-11 chữ số.', { type: 'error' });
        return;
    }

    const cityCode = cityInput?.dataset.code || '';
    const districtCode = districtInput?.dataset.code || '';
    const wardCode = wardInput?.dataset.code || '';
    if (!isEditing) {
        if (!cityInput?.value || !districtInput?.value || !wardInput?.value || !address || !cityCode || !districtCode || !wardCode) {
            showPopup('Vui lòng nhập đầy đủ địa chỉ.', { type: 'error' });
            return;
        }
        if (!confirmed) {
            showPopup('Vui lòng xác nhận thông tin khách hàng.', { type: 'error' });
            return;
        }
    } else if (!address) {
        showPopup('Vui lòng nhập địa chỉ.', { type: 'error' });
        return;
    }

    const hasFullAddressParts = cityInput?.value && districtInput?.value && wardInput?.value;
    const mergedAddress = hasFullAddressParts
        ? `${address}, ${wardInput.value}, ${districtInput.value}, ${cityInput.value}`
        : address;

    try {
        const url = isEditing ? `${API_BASE}/customers/${editingCustomerId}` : `${API_BASE}/customers`;
        const res = await fetch(url, {
            method: isEditing ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            },
            body: JSON.stringify({
                name,
                phone: phone || null,
                email: email || null,
                address: mergedAddress || null,
                cccd: cccd || null,
                dob: dob || null,
                gender: gender || null
            })
        });

        if (!res.ok) {
            const message = await res.text();
            showPopup(message || (isEditing ? 'Không thể cập nhật khách hàng.' : 'Không thể tạo khách hàng.'), { type: 'error' });
            return;
        }

        const saved = await res.json();
        const index = customers.findIndex(c => c.id === saved.id);
        if (index >= 0) {
            customers[index] = saved;
        } else {
            customers.unshift(saved);
        }
        customersLoaded = true;
        customerSearchTerm = '';
        const searchInput = document.getElementById('customerSearch');
        if (searchInput) searchInput.value = '';
        applyCustomerFilter();
        closeCustomerModal();
        selectCustomer(null, saved.id, saved.name, saved.phone || '-');
        if (searchInput) {
            searchInput.value = saved.name || saved.phone || '';
            searchInput.classList.add('has-selection');
        }
        if (activeCustomerDetailId === saved.id) {
            applyCustomerDetailData(saved);
        }
    } catch (err) {
        showPopup(isEditing ? 'Lỗi kết nối khi cập nhật khách hàng.' : 'Lỗi kết nối khi tạo khách hàng.', { type: 'error' });
    }
}

async function loadCities() {
    const cityList = document.getElementById('customerCityList');
    if (!cityList || cityList.children.length > 0) return;

    try {
        const res = await fetch('https://provinces.open-api.vn/api/p/');
        if (!res.ok) return;
        const data = await res.json();
        cityCache = data || [];
        renderDatalistOptions(cityList, cityCache);
    } catch (err) {
    }
}

async function loadDistricts(cityCode) {
    const districtInput = document.getElementById('customerDistrictInput');
    const districtList = document.getElementById('customerDistrictList');
    if (!districtInput || !districtList) return;
    districtInput.disabled = true;
    try {
        const res = await fetch(`https://provinces.open-api.vn/api/p/${cityCode}?depth=2`);
        if (!res.ok) return;
        const data = await res.json();
        districtCache = data.districts || [];
        renderDatalistOptions(districtList, districtCache);
        districtInput.disabled = false;
    } catch (err) {
    }
}

async function loadWards(districtCode) {
    const wardInput = document.getElementById('customerWardInput');
    const wardList = document.getElementById('customerWardList');
    if (!wardInput || !wardList) return;
    wardInput.disabled = true;
    try {
        const res = await fetch(`https://provinces.open-api.vn/api/d/${districtCode}?depth=2`);
        if (!res.ok) return;
        const data = await res.json();
        wardCache = data.wards || [];
        renderDatalistOptions(wardList, wardCache);
        wardInput.disabled = false;
    } catch (err) {
    }
}

function resetAddressInput(input) {
    if (!input) return;
    input.value = '';
    input.dataset.code = '';
    input.disabled = true;
}

function getAddressDisplayName(item) {
    const rawName = item?.name || '';
    const cleaned = rawName.replace(/^(Tỉnh|Thành phố)\s+/i, '').trim();
    return cleaned || rawName;
}

function getAddressVariants(item) {
    const rawName = item?.name || '';
    const displayName = getAddressDisplayName(item);
    const variants = [displayName];
    if (displayName !== rawName) {
        variants.push(rawName);
    }
    return variants;
}

function renderDatalistOptions(listEl, items) {
    if (!listEl) return;
    listEl.innerHTML = '';
    const seen = new Set();
    (items || []).forEach(item => {
        const displayName = getAddressDisplayName(item);
        if (!displayName || seen.has(displayName)) return;
        const option = document.createElement('option');
        option.value = displayName;
        listEl.appendChild(option);
        seen.add(displayName);
    });
}

function findMatchByName(items, value) {
    const normalized = normalizeKeyword(value);
    if (!normalized) return null;
    const list = items || [];
    for (const item of list) {
        const variants = getAddressVariants(item);
        for (const variant of variants) {
            if (normalizeKeyword(variant) === normalized) {
                return {
                    ...item,
                    displayName: variant
                };
            }
        }
    }
    return null;
}

function filterDatalist(listId, items, query) {
    const listEl = document.getElementById(listId);
    if (!listEl) return;
    const normalizedQuery = normalizeKeyword(query);
    listEl.innerHTML = '';
    const seen = new Set();
    (items || []).forEach(item => {
        const variants = getAddressVariants(item);
        const matches = !normalizedQuery || variants.some(variant => (
            normalizeKeyword(variant).includes(normalizedQuery)
        ));
        if (!matches) return;
        const displayName = getAddressDisplayName(item);
        if (!displayName || seen.has(displayName)) return;
        const option = document.createElement('option');
        option.value = displayName;
        listEl.appendChild(option);
        seen.add(displayName);
    });
}

function filterProducts(searchTerm = '') {
    const explicitKeyword = normalizeKeyword(searchTerm);
    const bottomKeyword = normalizeKeyword(bottomSearchTerm);

    if (bottomKeyword) {
        const filtered = applySort(filterProductList(products, bottomKeyword));
        setSuggestionMode('bottom');
        renderProducts(filtered, 'detailed');
        return;
    }

    if (currentCategory === BEST_SELLERS_CATEGORY_ID || currentCategory === 'all') {
        const bestSellerList = getBestSellerProducts();
        const filtered = explicitKeyword
            ? applySort(bestSellerList.filter((p) => productMatchesKeyword(p, explicitKeyword)))
            : applySort(bestSellerList);
        setSuggestionMode('category', 'Danh mục sản phẩm bán chạy');
        renderProducts(filtered, 'default');
        return;
    }

    const selected = getCategoryOptions().find((item) => String(item.id) === String(currentCategory));
    const filtered = applySort(filterProductList(products, explicitKeyword));
    setSuggestionMode('category', selected?.name || 'Danh mục');
    renderProducts(filtered, 'default');
}

function sortProducts(sortBy) {
    currentSort = sortBy;
    filterProducts();
}

function getBestSellerProducts() {
    const list = [...products].sort((a, b) => {
        const soldDiff = getSoldScore(b) - getSoldScore(a);
        if (soldDiff !== 0) return soldDiff;
        const stockDiff = (b.stock || 0) - (a.stock || 0);
        if (stockDiff !== 0) return stockDiff;
        return (a.name || '').localeCompare((b.name || ''), 'vi');
    });

    const hasSoldData = list.some((item) => getSoldScore(item) > 0);
    return hasSoldData ? list.filter((item) => getSoldScore(item) > 0) : list;
}

function getSoldScore(product) {
    const candidates = [
        product?.soldQuantity,
        product?.totalSold,
        product?.quantitySold,
        product?.soldCount,
        product?.salesCount,
        product?.orderCount,
        product?.sold,
        product?.totalSales
    ];

    for (const value of candidates) {
        const parsed = Number(value);
        if (Number.isFinite(parsed) && parsed > 0) {
            return parsed;
        }
    }

    return 0;
}

function applySort(list) {
    const sorted = [...(list || [])];
    switch (currentSort) {
        case 'price-low':
            sorted.sort((a, b) => (a.price || 0) - (b.price || 0));
            break;
        case 'price-high':
            sorted.sort((a, b) => (b.price || 0) - (a.price || 0));
            break;
        case 'name':
        default:
            sorted.sort((a, b) => (a.name || '').localeCompare(b.name || '', 'vi'));
            break;
    }
    return sorted;
}

function getCurrentQty() {
    const raw = parseInt(document.getElementById('qtyInput').value, 10);
    return Number.isFinite(raw) && raw > 0 ? raw : 1;
}

function stripDiacritics(value) {
    return value.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function normalizeKeyword(value) {
    return stripDiacritics((value || '').toString().trim().toLowerCase());
}

function escapeHtml(value) {
    return (value || '').replace(/[&<>"']/g, (char) => {
        switch (char) {
            case '&':
                return '&amp;';
            case '<':
                return '&lt;';
            case '>':
                return '&gt;';
            case '"':
                return '&quot;';
            case '\'':
                return '&#39;';
            default:
                return char;
        }
    });
}

function escapeForSingleQuote(value) {
    return (value || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function getEffectivePrice(product) {
    const basePrice = Number(product?.price);
    const direct = Number(
        product?.promoPrice ??
        product?.promotionPrice ??
        product?.discountPrice ??
        product?.salePrice ??
        product?.discountedPrice
    );
    const percent = Number(
        product?.discountPercent ??
        product?.discountRate ??
        product?.promoPercent ??
        product?.salePercent
    );

    if (Number.isFinite(direct)) {
        return direct;
    }

    if (Number.isFinite(basePrice) && Number.isFinite(percent) && percent > 0) {
        return Math.max(0, basePrice * (1 - percent / 100));
    }

    return Number.isFinite(basePrice) ? basePrice : NaN;
}

function getProductPricing(product) {
    const basePrice = Number(product?.price);
    let promoPrice = NaN;
    let promoLabel = '';
    let promoType = null;
    let promo = null;

    if (promotionIndex && product?.id != null) {
        const promoInfo = promotionIndex.get(product.id);
        if (promoInfo?.promo) {
            promo = promoInfo.promo;
            promoPrice = getPromoPrice(basePrice, promoInfo.promo);
            promoLabel = promoInfo.label || formatPromotionLabel(promoInfo.promo);
            promoType = normalizeDiscountType(promoInfo.promo.discountType);
        }
    }

    if (!Number.isFinite(promoPrice)) {
        const directPromo = Number(
            product?.promoPrice ??
            product?.promotionPrice ??
            product?.discountPrice ??
            product?.salePrice ??
            product?.discountedPrice
        );
        if (Number.isFinite(directPromo)) {
            promoPrice = directPromo;
        } else {
            const percent = Number(
                product?.discountPercent ??
                product?.discountRate ??
                product?.promoPercent ??
                product?.salePercent
            );
            if (Number.isFinite(basePrice) && Number.isFinite(percent) && percent > 0) {
                promoPrice = Math.max(0, basePrice * (1 - percent / 100));
            }
        }
    }

    const hasPromo = Number.isFinite(basePrice)
        && Number.isFinite(promoPrice)
        && promoPrice < basePrice;

    // Bundle promotions should always show as having a promo, even if price doesn't change
    const hasBundlePromo = promoType === 'BUNDLE' && promoLabel;

    return {
        basePrice: Number.isFinite(basePrice) ? basePrice : NaN,
        promoPrice,
        hasPromo: hasPromo || hasBundlePromo,
        promoType,
        label: promoLabel,
        promo
    };
}

function getRemainingPromoText(promo) {
    if (!promo) return '';
    const maxQty = Number(promo.maxQuantity);
    if (!Number.isFinite(maxQty) || maxQty <= 0) {
        return '';
    }
    const remaining = Number.isFinite(Number(promo.remainingQuantity))
        ? Math.max(0, Number(promo.remainingQuantity))
        : Math.max(0, maxQty - Math.max(0, Number(promo.usedQuantity || 0)));
    return `SL ${remaining}`;
}

function buildProductPriceParts(product) {
    const pricing = getProductPricing(product);
    const basePrice = Number.isFinite(pricing.basePrice) ? pricing.basePrice : 0;
    const promoPrice = Number.isFinite(pricing.promoPrice) ? pricing.promoPrice : basePrice;
    const hasPromo = pricing.hasPromo;
    const isBundlePromo = pricing.promoType === 'BUNDLE';

    // Show badge for both price discounts and bundle promotions
    const remainingText = getRemainingPromoText(pricing.promo);
    const badgeText = remainingText ? `KM ${remainingText}` : 'KM';
    const badge = hasPromo ? `<span class="promo-badge">${badgeText}</span>` : '';
    const tagClass = hasPromo && !isBundlePromo ? 'origin' : 'hidden';
    const priceTag = formatPriceCompact(basePrice);
    const label = hasPromo && pricing.label ? `<span class="price-label">${escapeHtml(pricing.label)}</span>` : '';

    // For bundle promos, show the condition instead of discounted price
    const priceBlock = hasPromo
        ? `
            <div class="product-pricing">
                <span class="price-new">${isBundlePromo ? 'Mua: ' + formatPrice(basePrice) : formatPrice(promoPrice)}</span>
                ${label}
            </div>
        `
        : `
            <div class="product-pricing">
                <span class="price-new">${formatPrice(basePrice)}</span>
            </div>
        `;

    return {
        hasPromo,
        badge,
        tagClass,
        priceTag,
        priceBlock
    };
}

function filterProductList(list, keyword) {
    return list.filter((p) => {
        const categoryKey = String(currentCategory || '');
        const matchCategory = categoryKey === BEST_SELLERS_CATEGORY_ID
            || categoryKey === 'all'
            || p.categoryId === parseInt(categoryKey, 10);
        const matchSearch = productMatchesKeyword(p, keyword);
        return matchCategory && matchSearch;
    });
}

function getCategoryLabel(product) {
    const categoryId = Number(product?.categoryId);
    // Prefer API category name so product-to-category mapping stays faithful to backend data.
    const raw = (product?.categoryName || product?.category || '').toString().trim();
    if (raw) return raw;

    if (Number.isFinite(categoryId) && categoryId > 0 && CATEGORY_NAME_BY_ID[categoryId]) {
        return CATEGORY_NAME_BY_ID[categoryId];
    }

    if (!Number.isFinite(categoryId) || categoryId <= 0) {
        return FALLBACK_CATEGORY_LABEL;
    }
    return `Danh mục ${categoryId}`;
}

function getCategoryOptions() {
    const categoryMap = new Map();
    (products || []).forEach((product) => {
        let rawName = (product?.categoryName || product?.category || '').toString().trim();
        let rawId = Number(product?.categoryId);
        let categoryIdToUse = rawId;

        // If backend didn't provide a numeric categoryId, create a stable synthetic id based on name
        if (!Number.isFinite(rawId) || rawId <= 0) {
            const nameKey = stripDiacritics((rawName || FALLBACK_CATEGORY_LABEL).toLowerCase()).replace(/[^a-z0-9]+/g, '') || 'khac';
            if (syntheticCategoryMap.has(nameKey)) {
                categoryIdToUse = syntheticCategoryMap.get(nameKey);
            } else {
                categoryIdToUse = nextSyntheticCategoryId--;
                syntheticCategoryMap.set(nameKey, categoryIdToUse);
            }
            // ensure product has a categoryId for consistent filtering
            product.categoryId = categoryIdToUse;
            if (!rawName) rawName = FALLBACK_CATEGORY_LABEL;
        }

        if (!categoryMap.has(categoryIdToUse)) {
            categoryMap.set(categoryIdToUse, {
                id: categoryIdToUse,
                name: rawName || getCategoryLabel(product),
                count: 0
            });
        }
        categoryMap.get(categoryIdToUse).count += 1;
    });

    return Array.from(categoryMap.values()).sort((a, b) => String(a.name).localeCompare(String(b.name), 'vi'));
}

function renderCategoryList() {
    const container = document.getElementById('productCategoryList');
    if (!container) return;

    const categories = getCategoryOptions();
    const hasCurrent = currentCategory === BEST_SELLERS_CATEGORY_ID
        || currentCategory === 'all'
        || categories.some((item) => String(item.id) === String(currentCategory));
    if (!hasCurrent) {
        currentCategory = BEST_SELLERS_CATEGORY_ID;
    }

    const bestSellerCount = getBestSellerProducts().length;
    const allButtonClass = (currentCategory === BEST_SELLERS_CATEGORY_ID || currentCategory === 'all')
        ? 'category-item active'
        : 'category-item';
    const allHtml = `<button class="${allButtonClass}" type="button" data-category-id="${BEST_SELLERS_CATEGORY_ID}"><span class="category-item-name">Danh mục sản phẩm bán chạy</span><span class="category-item-meta"><span class="category-item-caret" aria-hidden="true">&rsaquo;</span></span></button>`;

    const categoryHtml = categories.map((category) => {
        const isActive = String(category.id) === String(currentCategory);
        const buttonClass = isActive ? 'category-item active' : 'category-item';
        return `<button class="${buttonClass}" type="button" data-category-id="${category.id}"><span class="category-item-name">${escapeHtml(category.name)}</span><span class="category-item-meta"><span class="category-item-caret" aria-hidden="true">&rsaquo;</span></span></button>`;
    }).join('');

    container.innerHTML = allHtml + categoryHtml;
}

function setCategoryFilter(categoryId) {
    const normalized = categoryId == null ? BEST_SELLERS_CATEGORY_ID : String(categoryId);
    currentCategory = normalized;
    renderCategoryList();
    filterProducts();
}

function productMatchesKeyword(product, keyword) {
    const normalized = normalizeKeyword(keyword);
    if (!normalized) return true;
    const name = normalizeKeyword(product.name);
    const code = normalizeKeyword(product.code);
    const barcode = normalizeKeyword(product.barcode);
    return name.includes(normalized) || code.includes(normalized) || barcode.includes(normalized);
}

function handleBarcodeSearch() {
    const input = document.getElementById('searchInput');
    if (!input) return;
    const keyword = normalizeKeyword(input.value);
    if (!keyword) return;

    const exactMatch = products.find(p =>
        normalizeKeyword(p.code) === keyword ||
        normalizeKeyword(p.barcode) === keyword
    );
    if (exactMatch) {
        addToCart(exactMatch.id, exactMatch.name, exactMatch.price || 0);
        clearToolbarSearch();
        return;
    }

    const nameMatch = products.find(p => normalizeKeyword(p.name) === keyword);
    if (nameMatch) {
        addToCart(nameMatch.id, nameMatch.name, nameMatch.price || 0);
        clearToolbarSearch();
        return;
    }

    renderToolbarSearchResults(input.value);
}

function getStockValue(product) {
    const candidates = [product.stock, product.quantity, product.inventory, product.onHand];
    const found = candidates.find(val => Number.isFinite(Number(val)));
    return Number.isFinite(Number(found)) ? Number(found) : 0;
}

function renderProducts(filteredProducts = null, viewMode = 'default') {
    const displayProducts = filteredProducts || products;
    const grid = document.getElementById('productsGrid');

    if (!displayProducts || displayProducts.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #999;">Kh\u00f4ng c\u00f3 s\u1ea3n ph\u1ea9m</div>';
        return;
    }

    if (viewMode === 'compact') {
        grid.innerHTML = displayProducts.map(p => {
            const priceParts = buildProductPriceParts(p);
            return `
                <div class="product-card compact" onclick="addToCart(${p.id}, '${p.name}', ${p.price || 0})">
                    ${priceParts.badge}
                    <div class="product-price-tag ${priceParts.tagClass}">${priceParts.priceTag}</div>
                    <div class="product-image">${buildProductImageMarkup(p)}</div>
                    <div style="display:flex;align-items:center;gap:8px;">
                        <div class="product-name" style="flex:1;">${p.name || 'S\u1ea3n ph\u1ea9m'}</div>
                        <button class="review-btn" onclick="event.stopPropagation();showProductReviews(${p.id}, '${(p.name || '').replace(/'/g, "\\'")}')" style="background:none;border:none;padding:4px;cursor:pointer;color:#0066cc;font-size:14px;" title="Xem \u0111\u00e1nh gi\u00e1">★</button>
                    </div>
                    <div class="product-sku">${p.code || p.barcode || 'SKU'}</div>
                    ${priceParts.priceBlock}
                </div>
            `;
        }).join('');
        setTimeout(() => resolveMissingImagesOnPage(), 50);
        return;
    }

    if (viewMode === 'detailed') {
        grid.innerHTML = displayProducts.map(p => {
            const priceParts = buildProductPriceParts(p);
            return `
                <div class="product-card detailed" onclick="openProductDetail(${p.id})">
                    ${priceParts.badge}
                    <div class="product-price-tag ${priceParts.tagClass}">${priceParts.priceTag}</div>
                    <div class="product-image">${buildProductImageMarkup(p)}</div>
                    ${priceParts.priceBlock}
                    <div style="display:flex;align-items:center;gap:8px;">
                        <div class="product-name" style="flex:1;">${p.name || 'S\u1ea3n ph\u1ea9m'}</div>
                        <button class="review-btn" onclick="event.stopPropagation();showProductReviews(${p.id}, '${(p.name || '').replace(/'/g, "\\'")}')" style="background:none;border:none;padding:4px;cursor:pointer;color:#0066cc;font-size:14px;" title="Xem \u0111\u00e1nh gi\u00e1">★</button>
                    </div>
                    <div class="product-sku">${p.code || p.barcode || 'SKU'}</div>
                    <div class="product-meta">
                        <span>${p.unit ? '\u0110VT: ' + p.unit : '\u0110VT: -'}</span>
                    </div>
                    <div class="product-description">${p.description || 'Ch\u01b0a c\u00f3 m\u00f4 t\u1ea3'}</div>
                </div>
            `;
        }).join('');
        setTimeout(() => resolveMissingImagesOnPage(), 50);
        return;
    }

    grid.innerHTML = displayProducts.map(p => {
        const priceParts = buildProductPriceParts(p);
        return `
            <div class="product-card" onclick="addToCart(${p.id}, '${p.name}', ${p.price || 0})">
                ${priceParts.badge}
                <div class="product-price-tag ${priceParts.tagClass}">${priceParts.priceTag}</div>
                <div class="product-image">${buildProductImageMarkup(p)}</div>
                ${priceParts.priceBlock}
                <div style="display:flex;align-items:center;gap:8px;">
                    <div class="product-name" style="flex:1;">${p.name || 'S\u1ea3n ph\u1ea9m'}</div>
                    <button class="review-btn" onclick="event.stopPropagation();showProductReviews(${p.id}, '${(p.name || '').replace(/'/g, "\\'")}')" style="background:none;border:none;padding:4px;cursor:pointer;color:#0066cc;font-size:14px;" title="Xem \u0111\u00e1nh gi\u00e1">★</button>
                </div>
                <div class="product-sku">${p.code || 'SKU'}</div>
            </div>
        `;
    }).join('');
    setTimeout(() => resolveMissingImagesOnPage(), 50);
}
function setSuggestionMode(mode, categoryName = '') {
    const title = document.getElementById('suggestionTitle');
    const controls = document.getElementById('suggestionControls');

    if (!controls || !title) return;

    if (mode === 'bottom') {
        controls.style.display = 'flex';
        title.textContent = 'TƯ VẤN BÁN HÀNG';
        return;
    }

    if (mode === 'top') {
        controls.style.display = 'none';
        title.textContent = 'KẾT QUẢ TÌM KIẾM';
        return;
    }

    if (mode === 'category') {
        controls.style.display = 'flex';
        title.textContent = `DANH MỤC: ${categoryName}`;
        return;
    }

    controls.style.display = 'flex';
    title.textContent = 'SẢN PHẨM BÁN CHẠY';
}

function openProductDetail(productId) {
    const product = products.find(p => p.id === productId);
    if (!product) return;

    const modal = document.getElementById('productDetailModal');
    if (!modal) return;

    document.getElementById('detailProductName').textContent = product.name || 'Sản phẩm';
    document.getElementById('detailProductSku').textContent = product.code || product.barcode || '-';
    document.getElementById('detailProductBarcode').textContent = product.barcode || '-';
    document.getElementById('detailProductUnit').textContent = product.unit || '-';
    const pricing = getProductPricing(product);
    const basePrice = Number.isFinite(pricing.basePrice) ? pricing.basePrice : 0;
    const promoPrice = pricing.hasPromo ? pricing.promoPrice : basePrice;
    const promoLabel = pricing.hasPromo && pricing.label ? ` (${pricing.label})` : '';
    const detailPrice = pricing.hasPromo
        ? `${formatPrice(promoPrice)} (gốc ${formatPrice(basePrice)})${promoLabel}`
        : formatPrice(basePrice);
    document.getElementById('detailProductPrice').textContent = detailPrice;
    document.getElementById('detailProductStock').textContent = getStockValue(product);
    document.getElementById('detailProductDescription').textContent = product.description || 'Chưa có mô tả';

    const detailImage = modal.querySelector('.detail-image');
    if (detailImage) {
        const imageSrc = getProductImageSrc(product);
        if (imageSrc) {
            const safeName = escapeHtml(product?.name || 'Sản phẩm');
            detailImage.innerHTML = `<img src="${encodeURI(imageSrc)}" alt="${safeName}" />`;
        } else {
            detailImage.innerHTML = '<div class="detail-image-placeholder">Anh san pham</div>';
        }
    }

    const addBtn = document.getElementById('detailAddToCart');
    if (addBtn) {
        addBtn.onclick = () => {
            addToCart(product.id, product.name, product.price || 0);
            closeProductDetail();
        };
    }

    const inventoryBtn = document.getElementById('detailInventoryBtn');
    if (inventoryBtn) {
        inventoryBtn.onclick = () => openInventoryModal(product.id);
    }

    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function closeProductDetail() {
    const modal = document.getElementById('productDetailModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function openInventoryModal(productId = null) {
    const modal = document.getElementById('inventoryModal');
    if (!modal) return;

    const resolvedId = resolveInventoryProductId(productId);
    populateInventoryProducts(resolvedId);
    activeInventoryProductId = resolvedId;
    updateInventoryStockDisplay(resolvedId);
    loadInventoryHistory(resolvedId);

    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function closeInventoryModal() {
    const modal = document.getElementById('inventoryModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function resolveInventoryProductId(productId) {
    if (Number.isFinite(Number(productId))) {
        return Number(productId);
    }
    const first = products && products.length > 0 ? products[0] : null;
    return first?.id || null;
}

function populateInventoryProducts(selectedId) {
    const select = document.getElementById('inventoryProductSelect');
    if (!select) return;
    const options = (products || []).map((product) => {
        const label = `${product.name || 'San pham'} (${product.code || product.barcode || '-'})`;
        return `<option value="${product.id}">${escapeHtml(label)}</option>`;
    }).join('');
    select.innerHTML = options;
    if (selectedId && select.querySelector(`option[value="${selectedId}"]`)) {
        select.value = String(selectedId);
    }
}

function updateInventoryStockDisplay(productId) {
    const stockEl = document.getElementById('inventoryCurrentStock');
    if (!stockEl) return;
    const product = products.find(p => p.id === productId);
    stockEl.textContent = formatCompactNumber(getStockValue(product || {}));
}

async function loadInventoryHistory(productId) {
    const list = document.getElementById('inventoryHistoryList');
    const empty = document.getElementById('inventoryHistoryEmpty');
    if (!list || !empty || !productId) return;

    list.innerHTML = '';
    empty.style.display = 'none';

    try {
        const res = await fetch(`${API_BASE}/inventory/history?productId=${productId}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
        });
        if (!res.ok) {
            throw new Error('Failed to load inventory history');
        }
        const data = await res.json();
        renderInventoryHistory(Array.isArray(data) ? data : []);
    } catch (err) {
        list.innerHTML = '';
        empty.style.display = 'block';
    }
}

function renderInventoryHistory(items) {
    const list = document.getElementById('inventoryHistoryList');
    const empty = document.getElementById('inventoryHistoryEmpty');
    if (!list || !empty) return;

    if (!items || items.length === 0) {
        list.innerHTML = '';
        empty.style.display = 'block';
        return;
    }

    empty.style.display = 'none';
    list.innerHTML = items.map((item) => {
        const qty = Number(item.quantity) || 0;
        const label = formatInventoryType(item.type);
        const when = item.createdAt ? ` - ${escapeHtml(item.createdAt)}` : '';
        return `
            <div class="summary-row">
                <span>${escapeHtml(label)}${when}</span>
                <strong>${formatCompactNumber(qty)}</strong>
            </div>
        `;
    }).join('');
}

function formatInventoryType(type) {
    switch ((type || '').toUpperCase()) {
        case 'IN':
            return '\u004e\u0068\u1ead\u0070';
        case 'SALE':
            return '\u0042\u00e1\u006e';
        case 'OUT':
            return '\u0058\u0075\u1ea5\u0074';
        case 'RETURN':
            return '\u0054\u0072\u1ea3';
        case 'ADJUST':
            return '\u0110\u0069\u1ec1\u0075\u0020\u0063\u0068\u1ec9\u006e\u0068';
        default:
            return '\u004b\u0068\u00e1\u0063';
    }
}

async function submitInventoryReceipt() {
    const productId = activeInventoryProductId;
    if (!productId) {
        showPopup('Vui l\u00f2ng ch\u1ecdn s\u1ea3n ph\u1ea9m.', { type: 'error' });
        return;
    }
    const qtyInput = document.getElementById('inventoryQtyInput');
    const unitPriceInput = document.getElementById('inventoryUnitPriceInput');
    const noteInput = document.getElementById('inventoryNoteInput');
    const qty = parseInt(qtyInput?.value || '0', 10);
    if (!Number.isFinite(qty) || qty <= 0) {
        showPopup('Vui l\u00f2ng nh\u1eadp s\u1ed1 l\u01b0\u1ee3ng h\u1ee3p l\u1ec7.', { type: 'error' });
        return;
    }

    const rawUnitPrice = Number(unitPriceInput?.value || '');
    const unitPrice = Number.isFinite(rawUnitPrice) && rawUnitPrice > 0 ? rawUnitPrice : null;
    const note = (noteInput?.value || '').trim();
    const userId = parseInt(sessionStorage.getItem('userId'), 10) || null;

    try {
        const res = await fetch(`${API_BASE}/inventory/receipts`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
            },
            body: JSON.stringify({
                productId,
                quantity: qty,
                unitPrice,
                note: note || null,
                userId
            })
        });

        if (!res.ok) {
            const message = await res.text();
            showPopup(message || 'Kh\u00f4ng th\u1ec3 nh\u1eadp kho.', { type: 'error' });
            return;
        }

        const data = await res.json();
        updateLocalProductStock(productId, data?.newStock);
        loadInventoryHistory(productId);
        if (qtyInput) qtyInput.value = '1';
        if (unitPriceInput) unitPriceInput.value = '';
        if (noteInput) noteInput.value = '';
        showPopup('\u0110\u00e3 nh\u1eadp kho th\u00e0nh c\u00f4ng.', { type: 'success' });
    } catch (err) {
        showPopup('L\u1ed7i k\u1ebft n\u1ed1i khi nh\u1eadp kho.', { type: 'error' });
    }
}

function updateLocalProductStock(productId, newStock) {
    const product = products.find(p => p.id === productId);
    if (product) {
        const stockValue = Number.isFinite(Number(newStock)) ? Number(newStock) : getStockValue(product);
        product.stock = stockValue;
    }
    cart.forEach(item => {
        if (item.productId === productId) {
            item.stock = Number.isFinite(Number(newStock)) ? Number(newStock) : item.stock;
        }
    });
    updateInventoryStockDisplay(productId);
    filterProducts();
    updateProductDetailStock(productId);
}

function applyLocalStockAfterSale() {
    if (!cart || cart.length === 0) return;
    const updates = new Map();
    cart.forEach(item => {
        if (!item || item.isReturnItem || !Number.isFinite(Number(item.productId))) return;
        const product = products.find(p => p.id === item.productId);
        if (!product) return;
        const current = getStockValue(product);
        const qty = Math.max(0, Number(item.quantity) || 0);
        const next = Math.max(0, current - qty);
        updates.set(product.id, next);
    });

    if (updates.size === 0) return;
    updates.forEach((stock, productId) => {
        const product = products.find(p => p.id === productId);
        if (product) {
            product.stock = stock;
        }
    });
    filterProducts();
}

function updateProductDetailStock(productId) {
    const modal = document.getElementById('productDetailModal');
    if (!modal || !modal.classList.contains('show')) return;
    const detailName = document.getElementById('detailProductName')?.textContent || '';
    const product = products.find(p => p.id === productId);
    if (!product || (detailName && product.name && detailName !== product.name)) {
        return;
    }
    const stockEl = document.getElementById('detailProductStock');
    if (stockEl) {
        stockEl.textContent = formatCompactNumber(getStockValue(product));
    }
}

function isToolbarSearchOpen() {
    const panel = document.getElementById('toolbarSearchResults');
    return panel?.classList.contains('show');
}

function showToolbarSearchResults() {
    const panel = document.getElementById('toolbarSearchResults');
    if (!panel) return;
    panel.classList.add('show');
    panel.setAttribute('aria-hidden', 'false');
}

function hideToolbarSearchResults() {
    const panel = document.getElementById('toolbarSearchResults');
    if (!panel) return;
    panel.classList.remove('show');
    panel.setAttribute('aria-hidden', 'true');
}

function clearToolbarSearch() {
    const input = document.getElementById('searchInput');
    if (input) {
        input.value = '';
    }
    topSearchTerm = '';
    hideToolbarSearchResults();
}

function renderToolbarSearchResults(rawKeyword) {
    const keyword = normalizeKeyword(rawKeyword);
    const panel = document.getElementById('toolbarSearchResults');
    const list = document.getElementById('toolbarSearchList');
    const empty = document.getElementById('toolbarSearchEmpty');
    if (!panel || !list || !empty) return;

    if (!keyword) {
        list.innerHTML = '';
        empty.style.display = 'none';
        hideToolbarSearchResults();
        return;
    }

    const matches = applySort(filterProductList(products, keyword));
    const qty = getCurrentQty();
    if (!matches || matches.length === 0) {
        list.innerHTML = '';
        empty.style.display = 'block';
        showToolbarSearchResults();
        return;
    }

    empty.style.display = 'none';
    list.innerHTML = matches.map((p, idx) => {
        const name = p.name || 'Sản phẩm';
        const code = p.code || p.barcode || '-';
        const sku = p.sku || p.skuCode || p.skuId || p.code || p.barcode || '-';
        const unit = p.unit || '-';
        const pricing = getProductPricing(p);
        const price = pricing.hasPromo ? pricing.promoPrice : (Number.isFinite(pricing.basePrice) ? pricing.basePrice : 0);
        const total = price * qty;
        return `
            <button type="button" class="toolbar-search-row item"
                data-product-id="${p.id}"
                data-product-name="${escapeHtml(name)}"
                data-product-price="${price}">
                <span>${idx + 1}</span>
                <span>${escapeHtml(code)}</span>
                <span>${escapeHtml(sku)}</span>
                <span class="toolbar-search-name">${escapeHtml(name)}</span>
                <span>${qty}</span>
                <span>${escapeHtml(unit)}</span>
                <span>${formatPrice(price)}</span>
                <span>${formatPrice(total)}</span>
            </button>
        `;
    }).join('');
    showToolbarSearchResults();
}

function openCustomerDetail(customerId) {
    const modal = document.getElementById('customerDetailModal');
    if (!modal) return;

    const cachedCustomer = customers.find(c => c.id === customerId);
    if (!cachedCustomer) {
        showPopup('Không tìm thấy khách hàng.', { type: 'error' });
        return;
    }

    activeCustomerDetailId = customerId;
    switchCustomerDetailTab('overview');

    applyCustomerDetailData(cachedCustomer);
    refreshCustomerDetailFromApi(customerId);

    resetCustomerHistoryView();
    if (activeCustomerDetailId) {
        loadCustomerOrderHistory(activeCustomerDetailId);
    }
    updateCustomerDetailActions('overview');
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
}

function applyCustomerDetailData(customer) {
    if (!customer) return;
    const customerCode = customer.id ? `KH${customer.id}` : '--';
    setText('detailCustomerMemberCode', customerCode);
    setText('detailCustomerCodeInfo', customerCode);

    document.getElementById('detailCustomerName').textContent = (customer.name || '').toUpperCase();
    const nameCard = document.getElementById('detailCustomerNameCard');
    if (nameCard) {
        nameCard.textContent = customer.name || '-';
    }
    setText('detailCustomerPhone', customer.phone || '-');
    setText('detailCustomerEmail', customer.email || '-');
    setText('detailCustomerAddress', customer.address || '-');
    setText('detailCustomerNameInfo', customer.name || '-');
    setText('detailCustomerPhoneInfo', customer.phone || '-');
    setText('detailCustomerEmailInfo', customer.email || '-');
    setText('detailCustomerAddressInfo', customer.address || '-');
    setText('detailCustomerCccdInfo', customer.cccd || '-');
    setText('detailCustomerDobInfo', customer.dob ? formatDateTime(customer.dob) : '-');
    setText('detailCustomerGenderInfo', formatGenderLabel(customer.gender));

    const detailPoints = getCustomerPoints(customer);
    const detailTier = getEffectiveTier(customer);
    const redeemRate = TIER_DISCOUNT_BY_100[detailTier] || 0;
    setText('detailCustomerTier', formatTierLabel(detailTier));
    setText('detailCustomerPoints', formatCompactNumber(detailPoints));
    setText('detailCustomerPointsUsed', formatCompactNumber(getCustomerPointsUsed(customer)));
    const earnPolicyAmount = POINTS_EARN_RATE_VND * EARN_POLICY_POINTS;
    setText('detailEarnPolicy', `${formatCompactNumber(earnPolicyAmount)}đ = ${formatCompactNumber(EARN_POLICY_POINTS)} điểm`);
    setText('detailRedeemPolicy', redeemRate ? `100 điểm = ${formatCompactNumber(redeemRate)}d` : '-');
}

function formatGenderLabel(value) {
    const normalized = (value || '').toString().trim().toUpperCase();
    if (!normalized) return 'Chưa có thông tin';
    if (normalized === 'MALE' || normalized === 'NAM') return 'Nam';
    if (normalized === 'FEMALE' || normalized === 'NU' || normalized === 'NỮ') return 'Nữ';
    return 'Không xác định';
}

async function refreshCustomerDetailFromApi(customerId) {
    if (!customerId) return;
    try {
        const response = await fetch(`${API_BASE}/customers/${customerId}`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` }
        });
        if (!response.ok) return;
        const customer = await response.json();
        if (!customer) return;
        applyCustomerDetailData(customer);

        const index = customers.findIndex(c => c.id === customerId);
        if (index >= 0) {
            customers[index] = customer;
        }
        // Do not persist point values locally. The server is the source of truth.
        try {
            if (customer && customer.id) {
                persistSelectedCustomerState();
            }
        } catch (e) {
            console.warn('[refreshCustomerDetailFromApi] failed to persist selected customer state', e);
        }
        // Always apply the authoritative customer data we fetched so the UI
        // reflects the server state (points, tier, etc.). This prevents
        // situations where a minimal saved invoice/customer object would
        // temporarily overwrite or zero the displayed points.
        try { applyCustomerSelection(customer, { openDetail: false }); console.debug('[refreshCustomerDetailFromApi] applied customer', customerId); } catch (e) { console.warn('[refreshCustomerDetailFromApi] applyCustomerSelection failed', e); }
    } catch (err) {
        console.warn('[refreshCustomerDetailFromApi] failed to fetch customer', customerId, err);
    }
}

function getCustomerPointsUsed(customer) {
    const used =
        customer?.pointsUsed ??
        customer?.usedPoints ??
        customer?.redeemedPoints ??
        0;
    return Number.isFinite(Number(used)) ? Number(used) : 0;
}

function closeCustomerDetail() {
    const modal = document.getElementById('customerDetailModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
}

function switchCustomerDetailTab(key) {
    const modal = document.getElementById('customerDetailModal');
    if (!modal) return;

    modal.querySelectorAll('.detail-tabs .tab').forEach((tab) => {
        tab.classList.toggle('active', tab.dataset.tab === key);
    });
    modal.querySelectorAll('.detail-panel').forEach((panel) => {
        panel.classList.toggle('active', panel.dataset.panel === key);
    });

    updateCustomerDetailActions(key);

    if (key === 'history' && activeCustomerDetailId) {
        loadCustomerOrderHistory(activeCustomerDetailId);
    }
}

function updateCustomerDetailActions(key) {
    const primary = document.getElementById('detailPrimaryAction');
    const secondary = document.getElementById('detailSecondaryAction');
    if (!primary || !secondary) return;

    secondary.style.display = '';
    primary.onclick = null;

    if (key === 'info') {
        primary.textContent = 'Sửa';
        primary.onclick = () => {
            const customer = customers.find(c => c.id === activeCustomerDetailId);
            if (customer) {
                openCustomerModalForEdit(customer);
            }
        };
        return;
    }
    if (key === 'history') {
        primary.textContent = 'Đóng';
        secondary.style.display = 'none';
        primary.onclick = closeCustomerDetail;
        return;
    }
    if (key === 'debt') {
        primary.textContent = 'Xác nhận';
        return;
    }
    primary.textContent = 'Xác nhận';
}

function openCustomerModalForEdit(customer) {
    if (!customer || !customer.id) return;
    editingCustomerId = customer.id;
    const modal = document.getElementById('customerModal');
    const form = document.getElementById('customerForm');
    if (!modal || !form) return;

    setAddressRequired(false);
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    form.reset();

    document.getElementById('customerNameInput').value = customer.name || '';
    document.getElementById('customerPhoneInput').value = customer.phone || '';
    document.getElementById('customerEmailInput').value = customer.email || '';
    document.getElementById('customerCccdInput').value = customer.cccd || '';
    const dobInput = document.getElementById('customerDobInput');
    if (dobInput) {
        dobInput.value = customer.dob ? String(customer.dob).slice(0, 10) : '';
    }

    const genderValue = (customer.gender || 'UNKNOWN').toString().toUpperCase();
    const genderInputs = document.querySelectorAll('input[name="customerGender"]');
    genderInputs.forEach(input => {
        input.checked = input.value === genderValue;
    });

    const addressInput = document.getElementById('customerAddressInput');
    if (addressInput) addressInput.value = customer.address || '';

    const confirmInput = document.getElementById('customerConfirmInput');
    if (confirmInput) confirmInput.checked = true;

    // Không bắt buộc địa chỉ theo tỉnh/huyện/xã khi sửa
    const cityInput = document.getElementById('customerCityInput');
    const districtInput = document.getElementById('customerDistrictInput');
    const wardInput = document.getElementById('customerWardInput');
    if (cityInput) {
        cityInput.value = '';
        cityInput.dataset.code = '';
    }
    resetAddressInput(districtInput);
    resetAddressInput(wardInput);
    const districtList = document.getElementById('customerDistrictList');
    const wardList = document.getElementById('customerWardList');
    if (districtList) districtList.innerHTML = '';
    if (wardList) wardList.innerHTML = '';
}

function resetCustomerHistoryView() {
    const list = document.getElementById('detailCustomerHistoryList');
    if (list) {
        list.innerHTML = '<div class="history-empty">Chưa có lịch sử mua hàng</div>';
    }
    setText('detailCustomerSpend', '0');
    setText('detailCustomerOrders', '0');
    setText('detailCustomerSpendHistory', '0');
    setText('detailCustomerOrdersHistory', '0');
}

async function loadCustomerOrderHistory(customerId, force = false) {
    if (!customerId) return;
    const list = document.getElementById('detailCustomerHistoryList');
    if (list) {
        list.innerHTML = '<div class="history-empty">Đang tải lịch sử mua hàng...</div>';
    }

    if (!force && customerOrderCache.has(customerId)) {
        renderCustomerOrderHistory(customerOrderCache.get(customerId));
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/customers/${customerId}/orders`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (!response.ok) {
            if (response.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            throw new Error('Failed to load customer order history');
        }

        const data = await response.json();
        const orders = Array.isArray(data) ? data : [];
        customerOrderCache.set(customerId, orders);
        renderCustomerOrderHistory(orders);
    } catch (err) {
        if (list) {
            list.innerHTML = '<div class="history-empty">Không tải được lịch sử mua hàng.</div>';
        }
    }
}

function renderCustomerOrderHistory(orders) {
    const list = document.getElementById('detailCustomerHistoryList');
    if (!list) return;

    if (!orders || orders.length === 0) {
        list.innerHTML = '<div class="history-empty">Chưa có lịch sử mua hàng</div>';
        updateCustomerOrderSummary([], 0);
        return;
    }

    const totalSpent = orders.reduce((sum, order) => sum + (Number(order.totalAmount) || 0), 0);
    updateCustomerOrderSummary(orders, totalSpent);

    list.innerHTML = orders.map((order) => {
        const createdAt = formatDateTime(order.createdAt);
        const invoice = order.invoiceNumber || `HD-${order.id || '-'}`;
        const statusText = getOrderStatusText(order.status);
        const statusClass = getOrderStatusClass(order.status);
        const storeName = order.storeName || order.branchName || '-';
        const total = formatPrice(order.totalAmount || 0);
        const note = order.note || '-';
        return `
            <div class="history-item">
                <span>${escapeHtml(createdAt)}</span>
                <strong>${escapeHtml(invoice)}</strong>
                <span>${escapeHtml(storeName)}</span>
                <span class="status-badge ${statusClass}">${escapeHtml(statusText)}</span>
                <span>${escapeHtml(total)}</span>
                <span>${escapeHtml(note)}</span>
            </div>
        `;
    }).join('');
}

function updateCustomerOrderSummary(orders, totalSpent) {
    const count = orders.length;
    setText('detailCustomerSpend', formatPrice(totalSpent));
    setText('detailCustomerOrders', formatCompactNumber(count));
    setText('detailCustomerSpendHistory', formatPrice(totalSpent));
    setText('detailCustomerOrdersHistory', formatCompactNumber(count));
}

function getOrderStatusText(status) {
    if (!status) return 'Đã thanh toán';
    const textMap = {
        COMPLETED: 'Đã thanh toán',
        PAID: 'Đã thanh toán',
        PENDING: 'Chờ xử lý',
        CANCELLED: 'Đã hủy'
    };
    return textMap[status] || 'Đã thanh toán';
}

function getOrderStatusClass(status) {
    if (!status) return 'success';
    const classMap = {
        COMPLETED: 'success',
        PAID: 'success',
        PENDING: 'pending',
        CANCELLED: 'cancelled'
    };
    return classMap[status] || 'success';
}

function toggleEmptyState(isEmpty) {
    const emptyState = document.getElementById('emptyState');
    if (!emptyState) return;
    emptyState.style.display = isEmpty ? 'grid' : 'none';
}

function setupEmployeeSelector() {
    const button = document.getElementById('employeeSelector');
    const dropdown = document.getElementById('employeeList');

    if (!button || !dropdown) return;

    button.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = dropdown.style.display !== 'none';
        dropdown.style.display = isOpen ? 'none' : 'block';

        if (!isOpen && !employeesLoaded) {
            loadEmployees();
        }
    });

    document.addEventListener('click', (e) => {
        if (!dropdown || !button) return;
        if (!button.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.style.display = 'none';
        }
    });
}

async function loadEmployees() {
    if (employeesLoaded) return;

    const dropdown = document.getElementById('employeeList');
    if (!dropdown) return;

    dropdown.innerHTML = '<div class="employee-empty">Đang tải...</div>';

    try {
        const response = await fetch(`${API_BASE}/users`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });

        if (!response.ok) {
            if (response.status === 401) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
                return;
            }
            throw new Error('Failed to load employees');
        }

        const allUsers = await response.json();
        // Filter for users with EMPLOYEE role (exclude ADMIN, OWNER, MANAGER)
        employees = allUsers.filter(u => {
            const userRole = u.role ? (typeof u.role === 'string' ? u.role : u.role.name || '') : '';
            return userRole === 'EMPLOYEE';
        });
        employeesLoaded = true;
        renderEmployees();
    } catch (err) {
        dropdown.innerHTML = '<div class="employee-empty">Lỗi tải danh sách nhân viên</div>';
    }
}

function renderEmployees() {
    const dropdown = document.getElementById('employeeList');
    if (!dropdown) return;

    if (!employees || employees.length === 0) {
        dropdown.innerHTML = '<div class="employee-empty">Chưa có nhân viên</div>';
        return;
    }

    const employeesHtml = employees.map(emp => {
        const roleDisplay = emp.role ? (typeof emp.role === 'object' && emp.role.displayName ? emp.role.displayName : 'Nhân viên') : 'Nhân viên';
        return `
        <div class="employee-item" data-employee-id="${emp.id}" onclick="selectEmployee(event, ${emp.id}, '${emp.username.replace(/'/g, "\\'")}', '${(emp.fullName || emp.username).replace(/'/g, "\\'")}')">
            <div class="employee-info">
                <p class="employee-name">${emp.fullName || emp.username}</p>
                <p class="employee-role">${roleDisplay}</p>
            </div>
        </div>
        `;
    }).join('');

    dropdown.innerHTML = employeesHtml || '<div class="employee-empty">Chưa có nhân viên</div>';
}

function selectEmployee(evt, employeeId, employeeUsername, employeeName) {
    selectedEmployee = { id: employeeId, username: employeeUsername, name: employeeName };

    const button = document.getElementById('employeeSelector');
    if (button) {
        const nameSpan = button.querySelector('span:not(.chip-caret)');
        if (nameSpan) {
            nameSpan.textContent = employeeName || employeeUsername;
        }
    }

    const dropdown = document.getElementById('employeeList');
    if (dropdown) {
        dropdown.style.display = 'none';
    }

    document.querySelectorAll('.employee-item').forEach(item => {
        item.classList.remove('active');
    });
    const row = evt?.target?.closest('.employee-item');
    if (row) row.classList.add('active');
}

// ==================== AI COMBO PROMOTION FUNCTIONS ====================

// ComboPromotionAI v× ComboPromotionUI du?c load t? file combo-promotion-ai.js

/**
 * Ph×n t×ch gi? h×ng v× t? d?ng th×m qu× t?ng combo
 */
async function analyzeCartForCombo() {
    // Tr×nh v×ng l?p v× h?n
    if (isAnalyzingCombo) {
        return;
    }

    // Ki?m tra c× promotions kh×ng
    if (!allPromotions || allPromotions.length === 0) {
        return;
    }

    isAnalyzingCombo = true;

    try {
        // Chuy?n d?i gi? h×ng sang format cho AI (ch? s?n ph?m th?t)
        const cartItems = cart
            .filter(item => !item.isFreeGift)
            .map(item => ({
                product_id: item.productId,
                product_name: item.productName,
                quantity: item.quantity,
                price: item.productPrice
            }));

        if (cartItems.length === 0) {
            console.log('[analyzeCartForCombo] Cart is empty');
            isAnalyzingCombo = false;
            return;
        }

        // Format promotions cho AI (truyền products để lookup tên)
        const formattedPromotions = ComboPromotionAI.formatPromotions(allPromotions, products);

        console.log('[analyzeCartForCombo] Analyzing:', {
            cartItems: cartItems.length,
            promotions: formattedPromotions.length,
            cart: cartItems
        });

        // G?i AI ph×n t×ch
        const result = await ComboPromotionAI.analyzeCart(cartItems, formattedPromotions);

        console.log('[analyzeCartForCombo] AI result:', result);

        // Hi?n th? suggestions (ELIGIBLE ho?c UPSELL)
        if (result.suggestions && result.suggestions.length > 0) {
            displayComboSuggestions(result.suggestions);
        }

        // T? d?ng th×m/c?p nh?t qu× t?ng
        if (result.auto_add_gifts && result.auto_add_gifts.length > 0) {
            result.auto_add_gifts.forEach(gift => {
                autoAddGiftToCart(gift);
            });
        }

        // X×a qu× t?ng kh×ng hộp l?
        await removeIneligibleGifts(result.auto_add_gifts || []);

    } catch (error) {
        console.error('[analyzeCartForCombo] Error:', error);
    } finally {
        isAnalyzingCombo = false;
    }
}

/**
 * Hi?n th? g?i × combo
 */
function displayComboSuggestions(suggestions) {
    suggestions.forEach(suggestion => {
        if (suggestion.suggestion_type === 'ELIGIBLE') {
            // ×? di?u ki?n - Hi?n th? th×ng b×o th×nh c×ng
            ComboPromotionUI.showNotification(suggestion.message, 'success');
        } else if (suggestion.suggestion_type === 'UPSELL') {
            // G?n d? - Hi?n th? modal g?i × (ch? hi?n th? 1 l?n)
            if (!document.querySelector('.upsell-modal')) {
                ComboPromotionUI.showUpsellModal(suggestion, handleUpsellAddMore);
            }
        }
    });
}

/**
 * X? l× khi ngu?i d×ng nh?n "Th×m ngay" trong modal upsell
 */
function handleUpsellAddMore(suggestion) {
    console.log('[handleUpsellAddMore] Adding more:', suggestion);

    // T×m s?n ph?m trong gi?
    const cartItem = cart.find(item =>
        item.productId === suggestion.main_product_id && !item.isFreeGift
    );

    if (cartItem) {
        // Tang s? lu?ng l×n d? d? nh?n qu×
        const needed = suggestion.required_quantity - suggestion.current_quantity;
        cartItem.quantity += needed;

        renderCart();
        updateTotal();

        // Ph×n t×ch l?i d? t? d?ng th×m qu×
        setTimeout(() => analyzeCartForCombo(), 300);
    }
}

function findPromotionById(promoId) {
    if (promoId == null || !Array.isArray(allPromotions)) {
        return null;
    }
    return allPromotions.find((promo) => Number(promo?.id) === Number(promoId)) || null;
}

function readBundleNumber(bundle, camelKey, snakeKey, fallback = 0) {
    const value = bundle?.[camelKey] ?? bundle?.[snakeKey];
    const number = Number(value);
    return Number.isFinite(number) ? number : fallback;
}

function capGiftQuantityByQuota(gift) {
    const requestedQty = Math.max(0, Number(gift?.quantity || 0));
    if (requestedQty <= 0) {
        return 0;
    }

    const promo = findPromotionById(gift?.promo_id);
    if (!promo) {
        return requestedQty;
    }

    const bundle = (promo.bundleItems || []).find((item) => {
        const giftProductId = readBundleNumber(item, 'giftProductId', 'gift_product_id', null);
        return Number(giftProductId) === Number(gift?.product_id);
    });

    if (!bundle) {
        return requestedQty;
    }

    const mainProductId = readBundleNumber(bundle, 'mainProductId', 'main_product_id', null);
    const mainQty = Math.max(1, readBundleNumber(bundle, 'mainQuantity', 'main_quantity', 1));
    const giftQty = Math.max(1, readBundleNumber(bundle, 'giftQuantity', 'gift_quantity', 1));

    const purchasedMainQty = cart
        .filter((item) => !item.isFreeGift && Number(item.productId) === Number(mainProductId))
        .reduce((sum, item) => sum + Math.max(0, Number(item.quantity || 0)), 0);

    const eligibleSets = Math.floor(purchasedMainQty / mainQty);
    const eligibleGiftQty = eligibleSets * giftQty;

    const maxQty = Number(promo.maxQuantity);
    const usedQty = Math.max(0, Number(promo.usedQuantity || 0));
    let allowedGiftQty = eligibleGiftQty;

    if (Number.isFinite(maxQty) && maxQty > 0) {
        allowedGiftQty = Math.max(0, Math.min(eligibleGiftQty, maxQty - usedQty));
    }

    return Math.max(0, Math.min(requestedQty, allowedGiftQty));
}

/**
 * Tự động thêm quà tặng vào giỏ
 */
async function autoAddGiftToCart(gift) {
    console.log('[autoAddGiftToCart] Adding gift:', gift);
    console.log('[autoAddGiftToCart] Products cache:', products?.length || 0, 'items');

    const cappedGiftQty = capGiftQuantityByQuota(gift);
    if (cappedGiftQty <= 0) {
        console.log('[autoAddGiftToCart] Gift skipped due to quota/eligibility:', gift);
        return;
    }

    // Lookup tên sản phẩm từ cache hoặc API
    let productName = gift.product_name || null;

    if (!productName && gift.product_id) {
        // Thử tìm trong cache products trước
        const cachedProduct = products.find(p =>
            Number(p.productId) === Number(gift.product_id) ||
            Number(p.id) === Number(gift.product_id)
        );

        if (cachedProduct) {
            productName = cachedProduct.name || cachedProduct.productName || cachedProduct.product_name;
            console.log('[autoAddGiftToCart] ✓ Found in cache:', productName);
        } else {
            console.log('[autoAddGiftToCart] ⚠️ Not found in cache, trying API...');
            // Không có trong cache, gọi API
            try {
                const res = await fetch(`${API_BASE}/products/${gift.product_id}`, {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}`
                    }
                });
                if (res.ok) {
                    const product = await res.json();
                    productName = product.name || product.productName || product.product_name;
                    console.log('[autoAddGiftToCart] ✓ Fetched from API:', productName);
                } else {
                    console.error('[autoAddGiftToCart] ❌ API returned:', res.status);
                }
            } catch (err) {
                console.error('[autoAddGiftToCart] ❌ Failed to fetch:', err);
            }
        }
    }

    // Fallback cuối cùng
    if (!productName) {
        productName = `Sản phẩm #${gift.product_id}`;
        console.warn('[autoAddGiftToCart] ⚠️ Using fallback name:', productName);
    }

    // Kiểm tra xem quà đã có trong giỏ chưa
    const existingGift = cart.find(item =>
        item.productId === gift.product_id &&
        item.isFreeGift === true &&
        item.promoId === gift.promo_id
    );

    if (existingGift) {
        // Cập nhật số lượng và tên nếu khác
        if (existingGift.quantity !== cappedGiftQty) {
            console.log('[autoAddGiftToCart] Updating gift quantity:', cappedGiftQty);
            existingGift.quantity = cappedGiftQty;
        }
        if (existingGift.productName !== productName) {
            existingGift.productName = productName;
        }
        renderCart();
        updateTotal();
    } else {
        // Thêm quà mới
        console.log('[autoAddGiftToCart] Adding new gift');
        cart.push({
            productId: gift.product_id,
            productName: productName,
            productPrice: 0, // Miễn phí
            quantity: cappedGiftQty,
            productCode: '',
            unit: '',
            stock: 999, // Set stock cao để không bị check "out of stock"
            isFreeGift: true,
            promoId: gift.promo_id,
            promoCode: gift.promo_code,
            promoLabel: `🎁 ${gift.promo_name}`
        });

        renderCart();
        updateTotal();

        // Hiển thị thông báo
        ComboPromotionUI.showNotification(
            `✨ Đã thêm ${cappedGiftQty} ${productName} (Quà tặng)`,
            'success'
        );
    }
}

/**
 * Xóa quà tặng không hợp lệ và cập nhật số lượng
 */
async function removeIneligibleGifts(validGifts) {
    // Tạo Map các gift hợp lệ với số lượng
    const validGiftMap = new Map();
    (validGifts || []).forEach(g => {
        const cappedQty = capGiftQuantityByQuota(g);
        if (cappedQty <= 0) {
            return;
        }
        const key = `${g.product_id}-${g.promo_id}`;
        validGiftMap.set(key, {
            quantity: cappedQty,
            productName: g.product_name
        });
    });

    let hasChanges = false;
    const itemsToRemove = [];

    // Kiểm tra từng quà tặng trong giỏ
    cart.forEach((item, idx) => {
        if (item.isFreeGift) {
            const key = `${item.productId}-${item.promoId}`;
            const validGift = validGiftMap.get(key);

            if (!validGift) {
                // Quà không hợp lệ nữa - đánh dấu xóa
                console.log('[removeIneligibleGifts] ❌ Removing ineligible gift:', item.productName);
                itemsToRemove.push(idx);
                hasChanges = true;

                // Hiển thị thông báo
                ComboPromotionUI.showNotification(
                    `⚠️ Đã xóa quà tặng: ${item.productName} (không đủ điều kiện)`,
                    'warning'
                );
            } else if (item.quantity !== validGift.quantity) {
                // Cập nhật số lượng nếu thay đổi
                console.log('[removeIneligibleGifts] 🔄 Updating gift quantity:', {
                    product: item.productName,
                    old: item.quantity,
                    new: validGift.quantity
                });
                item.quantity = validGift.quantity;
                hasChanges = true;
            }
        }
    });

    // Xóa các item từ cuối lên đầu để không bị lỗi index
    itemsToRemove.sort((a, b) => b - a).forEach(idx => {
        cart.splice(idx, 1);
    });

    // Nếu có thay đổi, render lại
    if (hasChanges) {
        console.log('[removeIneligibleGifts] ✅ Cart updated, rendering...');
        renderCart();
        updateTotal();
    }
}

/**
 * X? l× khi thay d?i s? lu?ng trong gi?
 */
async function onCartItemQuantityChange() {
    // Ph×n t×ch l?i gi? h×ng
    await analyzeCartForCombo();
}

// G?i khi load trang d? ki?m tra AI Service
window.addEventListener('load', async () => {
    try {
        const response = await fetch('/ai/health');
        const data = await response.json();
        if (data.status === 'ok') {
            console.log('[AI Combo] Service ready');
        }
    } catch (error) {
        console.log('[AI Combo] Service offline (combo features disabled)');
    }
});
