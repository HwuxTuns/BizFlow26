const API_BASE = resolveApiBase();

let promotions = [];
let products = [];
let promoProducts = [];
let productMap = new Map();
let searchTerm = '';
let filterType = 'ALL';
const PRODUCT_IMAGE_LIST_URL = '/assets/data/product-image-files.json';
const productImageMap = new Map();
let productImageMapReady = false;

const PRODUCT_ICON = `
    <svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
        <path d="M6 8h12l-1.2 11H7.2L6 8Z" />
        <path d="M9 8V6a3 3 0 0 1 6 0v2" />
    </svg>
`;

window.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    loadUserInfo();
    setupActions();
    loadPromotions();
});

// Reload promotions khi quay lại trang để cập nhật trạng thái active/inactive
document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
        console.log('[promotions] Page visible, reloading promotions...');
        loadPromotions();
    }
});

// --- Khởi tạo & Auth ---
function resolveApiBase() {
    const configured = window.API_BASE_URL || window.API_BASE;
    if (configured) return configured.replace(/\/$/, '');
    if (window.location.protocol === 'file:') {
        return 'http://localhost:8000/api';
    }
    if (window.location.hostname === 'localhost' && window.location.port === '3000') {
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
    const searchInput = document.getElementById('promoSearch');
    const typeFilter = document.getElementById('promoTypeFilter');
    const reloadBtn = document.getElementById('promoReloadBtn');

    searchInput?.addEventListener('input', (e) => {
        searchTerm = e.target.value || '';
        applyFilters();
    });

    typeFilter?.addEventListener('change', (e) => {
        filterType = e.target.value || 'ALL';
        applyFilters();
    });

    reloadBtn?.addEventListener('click', () => loadPromotions());
}

// --- Xử lý dữ liệu ---
async function loadPromotions() {
    const grid = document.getElementById('promoGrid');
    const empty = document.getElementById('promoEmpty');
    if (grid) grid.innerHTML = '<div class="promo-empty">Đang tải dữ liệu khuyến mãi...</div>';
    if (empty) empty.hidden = true;

    try {
        const headers = { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken') || ''}` };
        const [promoRes, productRes] = await Promise.all([
            fetch(`${API_BASE}/v1/promotions`, { headers }),
            fetch(`${API_BASE}/products`, { headers })
        ]);
        
        await loadProductImageMap();

        if (promoRes.status === 401 || productRes.status === 401) {
            sessionStorage.clear();
            window.location.href = '/pages/login.html';
            return;
        }

        if (!promoRes.ok || !productRes.ok) throw new Error('Failed to load');

        const allPromotions = await promoRes.json();
        promotions = (allPromotions || []).filter(isPromotionActive);
        products = await productRes.json();
        productMap = new Map((Array.isArray(products) ? products : []).map(p => [p.id, p]));
        promoProducts = buildPromoProducts(promotions, products);
        
        updateSummary();
        applyFilters();
    } catch (err) {
        if (grid) grid.innerHTML = '';
        if (empty) {
            empty.hidden = false;
            empty.textContent = 'Không thể tải danh sách khuyến mãi.';
        }
    }
}

function buildPromoProducts(activePromos, productList) {
    const safeProducts = Array.isArray(productList) ? productList : [];
    const safePromos = Array.isArray(activePromos) ? activePromos : [];
    const productMap = new Map(safeProducts.map((p) => [p.id, p]));
    const results = new Map();

    safePromos.forEach((promo) => {
        const targetIds = new Set();
        (promo.targets || []).forEach((t) => {
            if (t.targetType === 'PRODUCT') targetIds.add(t.targetId);
            if (t.targetType === 'CATEGORY') {
                safeProducts.forEach(p => { if (p.categoryId === t.targetId) targetIds.add(p.id); });
            }
        });
        (promo.bundleItems || []).forEach(bi => {
            const mainId = bi.mainProductId ?? bi.productId;
            const giftId = bi.giftProductId ?? null;
            if (mainId) targetIds.add(mainId);
            if (giftId) targetIds.add(giftId);
        });

        targetIds.forEach((id) => {
            const product = productMap.get(id);
            if (!product) return;
            if (!results.has(id)) results.set(id, { product, promotions: [] });
            results.get(id).promotions.push(promo);
        });
    });

    return Array.from(results.values()).map((entry) => {
        const best = selectBestPromotion(entry.product, entry.promotions);
        return {
            product: entry.product,
            promotions: entry.promotions,
            bestPromotion: best.promo,
            promoPrice: best.price,
            promoLabel: best.label
        };
    });
}

function selectBestPromotion(product, promos) {
    if (!Array.isArray(promos) || promos.length === 0) {
        return { promo: null, price: NaN, label: '-' };
    }
    const basePrice = Number(product?.price);
    const candidates = promos.map(promo => ({ promo, price: getPromoPrice(basePrice, promo) }));
    const priced = candidates.filter(c => Number.isFinite(c.price)).sort((a, b) => a.price - b.price);

    if (priced.length > 0) {
        return { promo: priced[0].promo, price: priced[0].price, label: getDiscountLabel(priced[0].promo) };
    }
    return { promo: promos[0], price: NaN, label: getDiscountLabel(promos[0]) };
}

// --- Hiển thị (Render) ---
function renderPromoGrid(list) {
    const grid = document.getElementById('promoGrid');
    if (!grid) return;

    if (!list || list.length === 0) {
        grid.innerHTML = '';
        return;
    }

    grid.innerHTML = list.map((entry) => {
        const product = entry.product || {};
        const promo = entry.bestPromotion || {};
        const basePrice = Number(product.price);
        const promoPrice = Number(entry.promoPrice);
        const imageMarkup = buildProductImageMarkup(product);
        const discountLabel = appendRemainingToLabel(entry.promoLabel || '-', promo);
        const badgeLabel = buildPromoBadgeLabel(promo);
        const promoDates = formatPromoDates(promo.startDate, promo.endDate);
        const bundleInfo = promo.discountType === 'BUNDLE'
            ? getBundleInfo(promo, product.id)
            : null;

        return `
            <div class="promo-card">
                <div class="promo-badge-km">${badgeLabel}</div>
                
                <div class="promo-image">
                    ${imageMarkup}
                </div>

                <div class="promo-info">
                    <div class="promo-title">${escapeHtml(product.name || 'Sản phẩm')}</div>
                    <div class="promo-sku">SKU: ${escapeHtml(product.code || product.barcode || '-')}</div>
                    
                    <div class="promo-prices">
                        <span class="promo-price-new">${Number.isFinite(promoPrice) ? formatPrice(promoPrice) : formatPrice(basePrice)}</span>
                        <span class="promo-price-old">${Number.isFinite(promoPrice) && promoPrice < basePrice ? formatPrice(basePrice) : ''}</span>
                    </div>

                    <div class="promo-meta">
                        <div>CT: <strong>${escapeHtml(promo.name || promo.code || 'Khuyến mãi')}</strong></div>
                        <div class="promo-discount">Ưu đãi: ${discountLabel}</div>
                        <div class="promo-expiry">Hạn: ${promoDates}</div>
                        ${bundleInfo ? renderBundleInfo(bundleInfo) : ''}
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function applyFilters() {
    const grid = document.getElementById('promoGrid');
    const empty = document.getElementById('promoEmpty');
    if (!grid) return;

    const keyword = normalizeKeyword(searchTerm);
    const filtered = promoProducts.filter((entry) => {
        const matchesType = filterType === 'ALL' || entry.promotions.some(p => 
            p.discountType === filterType || (filterType === 'FIXED' && p.discountType === 'FIXED_AMOUNT')
        );
        if (!matchesType) return false;
        if (!keyword) return true;

        const text = `${entry.product.name} ${entry.product.code} ${entry.bestPromotion.name}`;
        return normalizeKeyword(text).includes(keyword);
    });

    renderPromoGrid(filtered);
    if (empty) empty.hidden = filtered.length > 0;
}

// --- Helpers (Giữ nguyên các hàm bổ trợ) ---
async function loadProductImageMap() {
    if (productImageMapReady) return;
    productImageMapReady = true;
    try {
        const response = await fetch(PRODUCT_IMAGE_LIST_URL, { cache: 'no-store' });
        const files = await response.json();
        if (Array.isArray(files)) {
            files.forEach(filePath => {
                const baseName = filePath.split('/').pop().replace(/\.[^.]+$/, '');
                productImageMap.set(normalizeProductKey(baseName), filePath);
            });
        }
    } catch (err) { console.error("Image map load failed"); }
}

function getProductImageSrc(product) {
    const keys = [product?.name, product?.code, product?.barcode];
    for (let k of keys) {
        const normalized = normalizeProductKey(k);
        if (normalized && productImageMap.has(normalized)) return productImageMap.get(normalized);
    }
    return '';
}

function buildProductImageMarkup(product) {
    const src = getProductImageSrc(product);
    return src ? `<img src="${encodeURI(src)}" alt="product" loading="lazy" />` : PRODUCT_ICON;
}

function buildTinyProductImageMarkup(product) {
    const src = getProductImageSrc(product);
    return src ? `<img src="${encodeURI(src)}" alt="product" loading="lazy" />` : PRODUCT_ICON;
}

function getBundleInfo(promo, productId) {
    const items = Array.isArray(promo?.bundleItems) ? promo.bundleItems : [];
    if (!items.length) return null;
    const match = items.find(b => {
        const mainId = b.mainProductId ?? b.productId;
        const giftId = b.giftProductId ?? null;
        return Number(mainId) === Number(productId) || Number(giftId) === Number(productId);
    }) || items[0];
    if (!match) return null;
    const mainId = match.mainProductId ?? match.productId;
    const giftId = match.giftProductId ?? match.productId;
    const mainQty = match.mainQuantity ?? match.quantity ?? 1;
    const giftQty = match.giftQuantity ?? 1;
    const mainProduct = productMap.get(Number(mainId));
    const giftProduct = productMap.get(Number(giftId)) || mainProduct;
    return { mainProduct, giftProduct, mainQty, giftQty };
}

function renderBundleInfo(bundle) {
    const main = bundle.mainProduct || {};
    const gift = bundle.giftProduct || {};
    const mainName = main.name || main.code || 'Sáº£n pháº©m';
    const giftName = gift.name || gift.code || 'Sáº£n pháº©m';
    return `
        <div class="promo-bundle">
            <div class="bundle-row">
                <span class="bundle-label">Mua</span>
                <div class="bundle-item">
                    <div class="bundle-thumb">${buildTinyProductImageMarkup(main)}</div>
                    <div class="bundle-text">x${bundle.mainQty} ${escapeHtml(mainName)}</div>
                </div>
            </div>
            <div class="bundle-row">
                <span class="bundle-label">Tặng</span>
                <div class="bundle-item">
                    <div class="bundle-thumb">${buildTinyProductImageMarkup(gift)}</div>
                    <div class="bundle-text">x${bundle.giftQty} ${escapeHtml(giftName)}</div>
                </div>
            </div>
        </div>
    `;
}

function getPromoPrice(base, promo) {
    const val = Number(promo.discountValue);
    if (promo.discountType === 'PERCENT') return base * (1 - val / 100);
    if (promo.discountType === 'FIXED' || promo.discountType === 'FIXED_AMOUNT') return Math.max(0, base - val);
    if (promo.discountType === 'BUNDLE') return val;
    return base;
}

function getDiscountLabel(promo) {
    if (!promo) return '-';
    const val = Number(promo.discountValue);
    if (promo.discountType === 'PERCENT') return `-${val}%`;
    if (promo.discountType === 'FIXED' || promo.discountType === 'FIXED_AMOUNT') return `-${formatPrice(val)}`;
    if (promo.discountType === 'BUNDLE') return 'Combo';
    return 'Tặng kèm';
}

function getRemainingPromoQuantity(promo) {
    const maxQty = Number(promo?.maxQuantity);
    if (!Number.isFinite(maxQty) || maxQty <= 0) {
        return null;
    }
    if (Number.isFinite(Number(promo?.remainingQuantity))) {
        return Math.max(0, Number(promo.remainingQuantity));
    }
    const usedQty = Math.max(0, Number(promo?.usedQuantity || 0));
    return Math.max(0, maxQty - usedQty);
}

function buildPromoBadgeLabel(promo) {
    const remaining = getRemainingPromoQuantity(promo);
    return remaining == null ? 'KM' : `KM SL ${remaining}`;
}

function appendRemainingToLabel(label, promo) {
    const remaining = getRemainingPromoQuantity(promo);
    if (remaining == null) return label;
    return `${label} (SL còn ${remaining})`;
}

function formatPrice(v) {
    return Number.isFinite(Number(v)) ? `${Math.round(v).toLocaleString('vi-VN')}đ` : '-';
}

function formatPromoDates(s, e) {
    const start = formatDate(s);
    const end = formatDate(e);
    return (start === '-' && end === '-') ? 'Không thời hạn' : `${start} - ${end}`;
}

function formatDate(val) {
    if (!val) return '-';
    let d = Array.isArray(val) ? new Date(val[0], val[1]-1, val[2]) : new Date(val);
    return isNaN(d.getTime()) ? '-' : d.toLocaleDateString('vi-VN');
}

function isPromotionActive(p) {
    if (!p) return false;
    if (p.active === false) return false; // Check active status
    const maxQty = Number(p.maxQuantity);
    if (Number.isFinite(maxQty) && maxQty > 0) {
        const usedQty = Math.max(0, Number(p.usedQuantity || 0));
        if (usedQty >= maxQty) return false;
    }
    const now = new Date();
    const start = parsePromotionDate(p.startDate);
    const end = parsePromotionDate(p.endDate);
    return (!start || now >= start) && (!end || now <= end);
}

function parsePromotionDate(v) {
    if (!v) return null;
    let d = Array.isArray(v) ? new Date(v[0], v[1]-1, v[2], v[3]||0, v[4]||0) : new Date(v);
    return isNaN(d.getTime()) ? null : d;
}

function normalizeProductKey(v) {
    return v ? stripDiacritics(v.toString().toLowerCase().trim()).replace(/[^a-z0-9]+/g, '') : '';
}

function stripDiacritics(v) {
    return v.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function normalizeKeyword(v) {
    if (!v) return '';
    return stripDiacritics(v.toString().toLowerCase().trim());
}

function escapeHtml(v) {
    return (v || '').toString().replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[m]));
}

function updateSummary() {
    const el = document.getElementById('promoSummary');
    if (el) el.textContent = `${promotions.length} khuyến mãi áp dụng | ${promoProducts.length} sản phẩm giảm giá`;
}


