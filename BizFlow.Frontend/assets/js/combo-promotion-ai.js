/**
 * Combo Promotion AI Service Helper
 * 
 * Tích hợp với AI Service để gợi ý và tự động thêm quà tặng combo
 * 
 * @version 1.0.0
 * @date 2026-01-25
 */

const ComboPromotionAI = {
    // Cấu hình
    API_BASE_URL: '/ai',
    
    // Cache
    _promotionsCache: null,
    _lastCacheTime: null,
    CACHE_DURATION: 5 * 60 * 1000, // 5 phút
    
    /**
     * Phân tích giỏ hàng và lấy gợi ý combo
     * 
     * @param {Array} cartItems - Danh sách sản phẩm trong giỏ
     * @param {Array} promotions - Danh sách khuyến mãi đang hoạt động
     * @returns {Promise<Object>} - {suggestions: [], auto_add_gifts: []}
     */
    async analyzeCart(cartItems, promotions) {
        try {
            // Validate input
            if (!Array.isArray(cartItems) || cartItems.length === 0) {
                return { suggestions: [], auto_add_gifts: [] };
            }
            
            if (!Array.isArray(promotions) || promotions.length === 0) {
                return { suggestions: [], auto_add_gifts: [] };
            }
            
            // Gọi API
            const response = await fetch(`${this.API_BASE_URL}/api/analyze-cart-promotions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    cart_items: cartItems,
                    promotions: promotions
                })
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            
            // Log để debug
            console.log('[ComboAI] Analyze result:', result);
            
            return result;
            
        } catch (error) {
            console.error('[ComboAI] Error analyzing cart:', error);
            return { suggestions: [], auto_add_gifts: [] };
        }
    },
    
    /**
     * Kiểm tra khuyến mãi combo cho 1 sản phẩm cụ thể
     * 
     * @param {number} productId - ID sản phẩm
     * @param {Array} promotions - Danh sách khuyến mãi
     * @returns {Promise<Object>} - {has_combo: bool, combos: []}
     */
    async checkProductPromotions(productId, promotions) {
        try {
            // Validate input
            if (!productId) {
                return { has_combo: false, combos: [] };
            }
            
            if (!Array.isArray(promotions) || promotions.length === 0) {
                return { has_combo: false, combos: [] };
            }
            
            // Gọi API
            const response = await fetch(`${this.API_BASE_URL}/api/check-product-promotions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    product_id: productId,
                    promotions: promotions
                })
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            
            // Log để debug
            console.log('[ComboAI] Product promotions:', result);
            
            return result;
            
        } catch (error) {
            console.error('[ComboAI] Error checking product promotions:', error);
            return { has_combo: false, combos: [] };
        }
    },
    
    /**
     * Load danh sách khuyến mãi từ backend (có cache)
     * 
     * @param {string} token - JWT token
     * @param {boolean} forceRefresh - Bỏ qua cache
     * @returns {Promise<Array>} - Danh sách khuyến mãi
     */
    async loadPromotions(token, forceRefresh = false) {
        try {
            // Kiểm tra cache
            const now = Date.now();
            if (!forceRefresh && this._promotionsCache && this._lastCacheTime) {
                if (now - this._lastCacheTime < this.CACHE_DURATION) {
                    console.log('[ComboAI] Using cached promotions');
                    return this._promotionsCache;
                }
            }
            
            // Load từ API
            const response = await fetch('/api/v1/promotions/active', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                throw new Error('Failed to load promotions');
            }
            
            const promotions = await response.json();
            
            // Lưu cache
            this._promotionsCache = promotions;
            this._lastCacheTime = now;
            
            console.log('[ComboAI] Loaded promotions:', promotions.length);
            
            return promotions;
            
        } catch (error) {
            console.error('[ComboAI] Error loading promotions:', error);
            return this._promotionsCache || [];
        }
    },
    
    /**
     * Clear cache
     */
    clearCache() {
        this._promotionsCache = null;
        this._lastCacheTime = null;
    },
    
    /**
     * Kiểm tra health của AI Service
     * 
     * @returns {Promise<boolean>}
     */
    async checkHealth() {
        try {
            const response = await fetch(`${this.API_BASE_URL}/health`, {
                method: 'GET',
                timeout: 5000
            });
            
            if (!response.ok) return false;
            
            const result = await response.json();
            return result.status === 'ok';
            
        } catch (error) {
            console.error('[ComboAI] Health check failed:', error);
            return false;
        }
    },
    
    /**
     * Format cart items từ cart array
     * 
     * @param {Array} cart - Giỏ hàng
     * @returns {Array} - Cart items theo format AI
     */
    formatCartItems(cart) {
        return cart
            .filter(item => !item.isFreeGift) // Bỏ quà tặng
            .map(item => ({
                product_id: item.productId,
                product_name: item.productName,
                quantity: item.quantity,
                price: item.productPrice
            }));
    },
    
    /**
     * Format promotions từ backend response
     * 
     * @param {Array} promotions - Khuyến mãi từ backend
     * @param {Array} productList - Danh sách sản phẩm (optional, để lookup tên)
     * @returns {Array} - Promotions theo format AI
     */
    formatPromotions(promotions, productList = []) {
        return promotions.map(promo => ({
            id: promo.id,
            code: promo.code,
            name: promo.name,
            discount_type: promo.discountType,
            discount_value: promo.discountValue,
            active: promo.active,
            bundle_items: (promo.bundleItems || []).map(bundle => {
                // Tra cứu tên sản phẩm nếu chưa có
                let mainProductName = bundle.mainProductName;
                let giftProductName = bundle.giftProductName;
                
                if ((!mainProductName || mainProductName === 'Unknown') && productList.length > 0) {
                    const mainProduct = productList.find(p => 
                        Number(p.id) === Number(bundle.mainProductId) || 
                        Number(p.productId) === Number(bundle.mainProductId)
                    );
                    if (mainProduct) {
                        mainProductName = mainProduct.name || mainProduct.productName || mainProduct.product_name;
                    }
                }
                
                if ((!giftProductName || giftProductName === 'Unknown') && productList.length > 0) {
                    const giftProduct = productList.find(p => 
                        Number(p.id) === Number(bundle.giftProductId) || 
                        Number(p.productId) === Number(bundle.giftProductId)
                    );
                    if (giftProduct) {
                        giftProductName = giftProduct.name || giftProduct.productName || giftProduct.product_name;
                    }
                }
                
                return {
                    bundle_id: bundle.id,
                    main_product_id: bundle.mainProductId,
                    main_product_name: mainProductName || `Sản phẩm #${bundle.mainProductId}`,
                    gift_product_id: bundle.giftProductId,
                    gift_product_name: giftProductName || `Sản phẩm #${bundle.giftProductId}`,
                    main_quantity: bundle.mainQuantity,
                    gift_quantity: bundle.giftQuantity
                };
            })
        }));
    }
};

/**
 * UI Helper cho Combo Promotions
 */
const ComboPromotionUI = {
    
    /**
     * Hiển thị thông báo combo
     * 
     * @param {string} message - Nội dung thông báo
     * @param {string} type - Loại: 'success', 'info', 'warning'
     */
    showNotification(message, type = 'success') {
        // Xóa notification cũ nếu có
        const oldNotif = document.querySelector('.combo-notification');
        if (oldNotif) {
            oldNotif.remove();
        }
        
        // Tạo notification mới
        const notification = document.createElement('div');
        notification.className = `combo-notification ${type}`;
        notification.innerHTML = `
            <div class="combo-notification-content">
                <i class="fas ${this._getIcon(type)}"></i>
                <span>${message}</span>
            </div>
        `;
        
        // Thêm vào body
        document.body.appendChild(notification);
        
        // Tự động ẩn sau 3 giây
        setTimeout(() => {
            notification.classList.add('fade-out');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    },
    
    /**
     * Hiển thị modal gợi ý mua thêm (upsell)
     * 
     * @param {Object} suggestion - Suggestion từ AI
     * @param {Function} onAddMore - Callback khi nhấn "Thêm ngay"
     */
    showUpsellModal(suggestion, onAddMore) {
        // Xóa modal cũ nếu có
        const oldModal = document.querySelector('.upsell-modal');
        if (oldModal) {
            oldModal.remove();
        }
        
        // Tạo modal mới
        const modal = document.createElement('div');
        modal.className = 'upsell-modal';
        modal.innerHTML = `
            <div class="upsell-content">
                <button class="close-btn" onclick="this.parentElement.parentElement.remove()">×</button>
                <div class="upsell-icon">💡</div>
                <h3>Cơ hội tiết kiệm!</h3>
                <p>${suggestion.message}</p>
                <div class="upsell-details">
                    <div class="upsell-detail-item">
                        <span class="label">Đang có:</span>
                        <span class="value">${suggestion.current_quantity} sản phẩm</span>
                    </div>
                    <div class="upsell-detail-item">
                        <span class="label">Cần thêm:</span>
                        <span class="value">${suggestion.required_quantity - suggestion.current_quantity} sản phẩm</span>
                    </div>
                    <div class="upsell-detail-item">
                        <span class="label">Sẽ được tặng:</span>
                        <span class="value">${suggestion.gift_quantity} ${suggestion.gift_product_name}</span>
                    </div>
                </div>
                <div class="upsell-actions">
                    <button class="btn-secondary" onclick="this.closest('.upsell-modal').remove()">
                        Để sau
                    </button>
                    <button class="btn-primary" id="upsellAddBtn">
                        Thêm ngay
                    </button>
                </div>
            </div>
        `;
        
        // Thêm vào body
        document.body.appendChild(modal);
        
        // Attach event listener
        document.getElementById('upsellAddBtn').addEventListener('click', () => {
            onAddMore(suggestion);
            modal.remove();
        });
        
        // Click outside để đóng
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.remove();
            }
        });
    },
    
    /**
     * Hiển thị danh sách suggestions
     * 
     * @param {Array} suggestions - Danh sách suggestions
     * @param {Function} onAddMore - Callback khi nhấn "Thêm ngay"
     */
    displaySuggestions(suggestions, onAddMore) {
        suggestions.forEach(suggestion => {
            if (suggestion.suggestion_type === 'ELIGIBLE') {
                // Đủ điều kiện - Hiển thị thông báo thành công
                this.showNotification(suggestion.message, 'success');
            } else if (suggestion.suggestion_type === 'UPSELL') {
                // Gần đủ - Hiển thị modal gợi ý
                this.showUpsellModal(suggestion, onAddMore);
            }
        });
    },
    
    /**
     * Hiển thị badge combo trên sản phẩm
     * 
     * @param {HTMLElement} productElement - Element sản phẩm
     * @param {string} label - Label hiển thị (ví dụ: "Combo 3+1")
     */
    addComboBadge(productElement, label) {
        // Kiểm tra đã có badge chưa
        if (productElement.querySelector('.combo-badge')) {
            return;
        }
        
        const badge = document.createElement('div');
        badge.className = 'combo-badge';
        badge.innerHTML = `<i class="fas fa-gift"></i> ${label}`;
        
        productElement.appendChild(badge);
    },
    
    /**
     * Format gift item cho hiển thị trong giỏ hàng
     * 
     * @param {Object} giftItem - Gift item
     * @returns {string} - HTML string
     */
    formatGiftItem(giftItem) {
        return `
            <div class="cart-item gift-item">
                <span class="gift-badge">🎁 TẶNG</span>
                <span class="name">${giftItem.product_name}</span>
                <span class="quantity">${giftItem.quantity}</span>
                <span class="price">0đ</span>
                <span class="gift-label">${giftItem.promo_name}</span>
            </div>
        `;
    },
    
    /**
     * Lấy icon theo loại thông báo
     */
    _getIcon(type) {
        const icons = {
            'success': 'fa-check-circle',
            'info': 'fa-info-circle',
            'warning': 'fa-exclamation-triangle',
            'error': 'fa-times-circle'
        };
        return icons[type] || icons.info;
    }
};

// Export cho sử dụng ở nơi khác
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { ComboPromotionAI, ComboPromotionUI };
}


