/**
 * Admin Orders Management
 * Quản lý đơn hàng
 */

class AdminOrdersManager {
    constructor() {
        this.api = adminAPI;
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalPages = 0;
        this.orders = [];
        this.isLoading = false;
    }

    /**
     * Khởi tạo
     */
    async init() {
        this.setupEventListeners();
        await this.loadOrders();
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Pagination
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        if (prevBtn) prevBtn.addEventListener('click', () => this.previousPage());
        if (nextBtn) nextBtn.addEventListener('click', () => this.nextPage());
    }

    /**
     * Load đơn hàng
     */
    async loadOrders() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();

        try {
            const result = await this.api.getOrders(this.currentPage, this.pageSize);
            
            if (result.success) {
                this.orders = result.data || result.orders || [];
                this.totalPages = result.totalPages || Math.ceil((result.total || 0) / this.pageSize);
                this.renderOrders();
                this.updatePagination();
            } else {
                this.showError(result.error || 'Không thể tải dữ liệu đơn hàng');
            }
        } catch (error) {
            this.showError('Lỗi kết nối: ' + error.message);
        } finally {
            this.isLoading = false;
            this.hideLoading();
        }
    }

    /**
     * Render danh sách đơn hàng
     */
    renderOrders() {
        const tbody = document.getElementById('ordersBody');
        if (!tbody) return;

        if (this.orders.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-box">
                        <div style="text-align: center; padding: 20px;">
                            <div style="font-size: 24px; margin-bottom: 10px;">📋</div>
                            <div>Chưa có đơn hàng nào</div>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.orders.map((order, index) => `
            <tr>
                <td><strong>${this.currentPage * this.pageSize + index + 1}</strong></td>
                <td>
                    <strong>#${this.escapeHtml(order.id || 'N/A')}</strong>
                </td>
                <td>
                    <div style="font-size: 14px; font-weight: 500;">${this.escapeHtml(order.customerName || 'N/A')}</div>
                    <div style="font-size: 12px; color: #666;">${this.escapeHtml(order.customerEmail || 'N/A')}</div>
                </td>
                <td>
                    <strong style="color: #2196F3; font-size: 14px;">
                        ${this.formatPrice(order.totalAmount || 0)}
                    </strong>
                </td>
                <td>
                    ${this.getStatusBadge(order.status || 'PENDING')}
                </td>
                <td>
                    ${this.formatDate(order.createdAt || order.createdDate || new Date())}
                </td>
                <td>
                    <div style="display: flex; gap: 8px;">
                        <button class="btn-small" onclick="ordersManager.viewOrder(${order.id})" title="Xem chi tiết">
                            👁️
                        </button>
                        <select class="btn-small" onchange="ordersManager.updateStatus(${order.id}, this.value)" style="padding: 4px 6px; font-size: 12px; cursor: pointer;">
                            <option value="">Cập nhật trạng thái...</option>
                            <option value="PENDING">PENDING</option>
                            <option value="PROCESSING">PROCESSING</option>
                            <option value="SHIPPED">SHIPPED</option>
                            <option value="DELIVERED">DELIVERED</option>
                            <option value="CANCELLED">CANCELLED</option>
                        </select>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    /**
     * View đơn hàng
     */
    viewOrder(orderId) {
        const order = this.orders.find(o => o.id === orderId);
        if (!order) {
            alert('Không tìm thấy đơn hàng');
            return;
        }

        alert(`Đơn hàng #${order.id}\n\nKhách hàng: ${order.customerName}\nTổng tiền: ${this.formatPrice(order.totalAmount || 0)}\nTrạng thái: ${order.status}\n\nChi tiết: Mở bảng chi tiết để xem đầy đủ`);
    }

    /**
     * Update trạng thái đơn hàng
     */
    async updateStatus(orderId, status) {
        if (!status) return;

        if (confirm(`Cập nhật trạng thái đơn hàng #${orderId} thành ${status}?`)) {
            try {
                const result = await this.api.updateOrderStatus(orderId, status);
                if (result.success) {
                    alert('✓ Cập nhật thành công');
                    await this.loadOrders();
                } else {
                    alert('✗ Cập nhật thất bại: ' + (result.error || 'Unknown error'));
                }
            } catch (error) {
                alert('✗ Lỗi: ' + error.message);
            }
        }
    }

    /**
     * Next page
     */
    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadOrders();
        }
    }

    /**
     * Previous page
     */
    previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadOrders();
        }
    }

    /**
     * Update pagination UI
     */
    updatePagination() {
        const pageInfo = document.getElementById('pageInfo');
        if (pageInfo) {
            pageInfo.textContent = `Trang ${this.currentPage + 1} / ${this.totalPages}`;
        }

        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        
        if (prevBtn) prevBtn.disabled = this.currentPage === 0;
        if (nextBtn) nextBtn.disabled = this.currentPage >= this.totalPages - 1;
    }

    /**
     * Get status badge
     */
    getStatusBadge(status) {
        const statusConfig = {
            'PENDING': { color: '#FF9800', label: '⏳ Chờ xử lý' },
            'PROCESSING': { color: '#2196F3', label: '⚙️ Đang xử lý' },
            'SHIPPED': { color: '#9C27B0', label: '🚚 Đã gửi' },
            'DELIVERED': { color: '#4CAF50', label: '✓ Đã giao' },
            'CANCELLED': { color: '#F44336', label: '✗ Đã hủy' },
        };

        const config = statusConfig[status] || { color: '#757575', label: status };

        return `
            <span style="background: ${config.color}40; color: ${config.color}; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500;">
                ${config.label}
            </span>
        `;
    }

    /**
     * Format giá
     */
    formatPrice(price) {
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
        }).format(price);
    }

    /**
     * Format ngày tháng
     */
    formatDate(date) {
        if (!date) return 'N/A';
        
        const d = new Date(date);
        return new Intl.DateTimeFormat('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
        }).format(d);
    }

    /**
     * Show loading
     */
    showLoading() {
        const tbody = document.getElementById('ordersBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-box">
                        <div style="text-align: center; padding: 20px;">
                            <div style="display: inline-block; animation: spin 1s linear infinite;">⏳</div>
                            <div style="margin-top: 10px;">Đang tải...</div>
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Hide loading
     */
    hideLoading() {
        // Implementation can be added if needed
    }

    /**
     * Show error
     */
    showError(message) {
        const tbody = document.getElementById('ordersBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-box">
                        <div style="text-align: center; padding: 20px; color: #d32f2f;">
                            <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                            <div>${this.escapeHtml(message)}</div>
                            <button onclick="ordersManager.loadOrders()" style="margin-top: 10px; padding: 6px 12px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer;">
                                Thử lại
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    /**
     * Escape HTML
     */
    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, m => map[m]);
    }
}

// Initialize
let ordersManager;
window.addEventListener('DOMContentLoaded', () => {
    ordersManager = new AdminOrdersManager();
    ordersManager.init();
});
