/**
 * Customer Purchase History Module
 * Manages viewing and displaying customer order history from Kafka
 */

class CustomerPurchaseHistory {
    constructor() {
        this.apiBaseUrl = '/api/admin/customers';
        this.currentCustomerId = null;
        this.currentPage = 0;
        this.pageSize = 10;
    }

    /**
     * Load and display purchase history for a customer
     */
    async loadPurchaseHistory(customerId, page = 0) {
        try {
            this.currentCustomerId = customerId;
            this.currentPage = page;
            
            const response = await fetch(
                `${this.apiBaseUrl}/${customerId}/purchase-history?page=${page}&size=${this.pageSize}`,
                {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            const result = await response.json();
            
            if (result.success) {
                this.displayPurchaseHistory(result.data, result.totalElements, result.totalPages);
            } else {
                this.showError('Failed to load purchase history: ' + result.error);
            }
        } catch (error) {
            console.error('Error loading purchase history:', error);
            this.showError('Error loading purchase history: ' + error.message);
        }
    }

    /**
     * Load all purchases for a customer (no pagination)
     */
    async loadAllPurchases(customerId) {
        try {
            const response = await fetch(
                `${this.apiBaseUrl}/${customerId}/purchase-history/all`,
                {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            const result = await response.json();
            return result.success ? result.data : [];
        } catch (error) {
            console.error('Error loading all purchases:', error);
            return [];
        }
    }

    /**
     * Load purchases within a date range
     */
    async loadPurchasesByDateRange(customerId, startDate, endDate) {
        try {
            const response = await fetch(
                `${this.apiBaseUrl}/${customerId}/purchase-history/date-range?startDate=${startDate}&endDate=${endDate}`,
                {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            const result = await response.json();
            return result.success ? result.data : [];
        } catch (error) {
            console.error('Error loading purchases by date range:', error);
            return [];
        }
    }

    /**
     * Get purchase count for a customer
     */
    async getPurchaseCount(customerId) {
        try {
            const response = await fetch(
                `${this.apiBaseUrl}/${customerId}/purchase-history/count`,
                {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            const result = await response.json();
            return result.success ? result.purchaseCount : 0;
        } catch (error) {
            console.error('Error getting purchase count:', error);
            return 0;
        }
    }

    /**
     * Display purchase history in table format
     */
    displayPurchaseHistory(purchases, totalElements, totalPages) {
        const container = document.getElementById('purchaseHistoryContainer') || 
                         this.createHistoryContainer();
        
        if (!purchases || purchases.length === 0) {
            container.innerHTML = '<p class="no-data">No purchase history found for this customer.</p>';
            return;
        }

        let html = '<table class="purchase-history-table">';
        html += '<thead><tr>';
        html += '<th>Order ID</th>';
        html += '<th>Invoice Number</th>';
        html += '<th>Total Amount</th>';
        html += '<th>Status</th>';
        html += '<th>Order Date</th>';
        html += '<th>Payment Method</th>';
        html += '<th>Items Count</th>';
        html += '<th>Action</th>';
        html += '</tr></thead>';
        html += '<tbody>';

        purchases.forEach(purchase => {
            const itemsCount = purchase.orderItemsJson ? 
                JSON.parse(purchase.orderItemsJson).length : 0;
            const orderDate = new Date(purchase.orderCreatedAt).toLocaleDateString('vi-VN');
            const statusClass = this.getStatusClass(purchase.status);
            
            html += `<tr>
                <td>${purchase.orderId}</td>
                <td>${purchase.invoiceNumber || 'N/A'}</td>
                <td class="amount">${this.formatCurrency(purchase.totalAmount)}</td>
                <td><span class="status-badge ${statusClass}">${this.getStatusLabel(purchase.status)}</span></td>
                <td>${orderDate}</td>
                <td>${purchase.paymentMethod || 'N/A'}</td>
                <td class="center">${itemsCount}</td>
                <td class="center">
                    <button class="btn-detail" onclick="purchaseHistory.showDetails(${purchase.id})">
                        Chi tiết
                    </button>
                </td>
            </tr>`;
        });

        html += '</tbody></table>';
        
        // Add pagination if needed
        if (totalPages > 1) {
            html += this.createPaginationControls(totalPages);
        }
        
        container.innerHTML = html;
    }

    /**
     * Show detailed view of a purchase
     */
    async showDetails(purchaseId) {
        try {
            const response = await fetch(
                `${this.apiBaseUrl}/${this.currentCustomerId}/purchase-history/${purchaseId}`,
                {
                    headers: {
                        'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status}`);
            }

            const result = await response.json();
            
            if (result.success) {
                this.displayPurchaseDetails(result.data);
            } else {
                this.showError('Failed to load purchase details');
            }
        } catch (error) {
            console.error('Error loading purchase details:', error);
            this.showError('Error loading purchase details: ' + error.message);
        }
    }

    /**
     * Display detailed purchase information
     */
    displayPurchaseDetails(purchase) {
        let html = '<div class="purchase-details-modal">';
        html += '<div class="modal-content">';
        html += '<button class="close-btn" onclick="purchaseHistory.closeDetails()">&times;</button>';
        html += '<h3>Chi tiết đơn hàng #' + purchase.orderId + '</h3>';
        
        html += '<div class="detail-section">';
        html += `<p><strong>Mã hóa đơn:</strong> ${purchase.invoiceNumber || 'N/A'}</p>`;
        html += `<p><strong>Ngày tạo:</strong> ${new Date(purchase.orderCreatedAt).toLocaleString('vi-VN')}</p>`;
        html += `<p><strong>Tổng tiền:</strong> ${this.formatCurrency(purchase.totalAmount)}</p>`;
        html += `<p><strong>Trạng thái:</strong> <span class="status-badge ${this.getStatusClass(purchase.status)}">${this.getStatusLabel(purchase.status)}</span></p>`;
        html += `<p><strong>Phương thức thanh toán:</strong> ${purchase.paymentMethod || 'N/A'}</p>`;
        html += '</div>';

        // Display order items
        if (purchase.orderItemsJson) {
            const items = JSON.parse(purchase.orderItemsJson);
            html += '<div class="detail-section">';
            html += '<h4>Sản phẩm đã mua:</h4>';
            html += '<table class="order-items-table">';
            html += '<thead><tr><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Tổng</th></tr></thead>';
            html += '<tbody>';

            items.forEach(item => {
                const total = item.price * item.quantity;
                html += `<tr>
                    <td>${item.productName}</td>
                    <td class="center">${item.quantity}</td>
                    <td class="amount">${this.formatCurrency(item.price)}</td>
                    <td class="amount">${this.formatCurrency(total)}</td>
                </tr>`;
            });

            html += '</tbody></table>';
            html += '</div>';
        }

        html += '</div>';
        html += '</div>';

        // Show modal
        const modal = document.getElementById('purchaseDetailsModal') || 
                     this.createDetailsModal();
        modal.innerHTML = html;
        modal.style.display = 'block';
    }

    /**
     * Close details modal
     */
    closeDetails() {
        const modal = document.getElementById('purchaseDetailsModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }

    /**
     * Create pagination controls
     */
    createPaginationControls(totalPages) {
        let html = '<div class="pagination">';
        
        if (this.currentPage > 0) {
            html += `<button onclick="purchaseHistory.loadPurchaseHistory(${this.currentCustomerId}, ${this.currentPage - 1})">
                ← Trước
            </button>`;
        }
        
        html += `<span>Trang ${this.currentPage + 1} / ${totalPages}</span>`;
        
        if (this.currentPage < totalPages - 1) {
            html += `<button onclick="purchaseHistory.loadPurchaseHistory(${this.currentCustomerId}, ${this.currentPage + 1})">
                Sau →
            </button>`;
        }
        
        html += '</div>';
        return html;
    }

    /**
     * Get CSS class for status badge
     */
    getStatusClass(status) {
        const s = String(status || '').toUpperCase();
        const statusMap = {
            'PAID': 'status-paid',
            'UNPAID': 'status-unpaid',
            'RETURNED': 'status-returned',
            'CANCELLED': 'status-cancelled',
            'PENDING': 'status-pending',
            'CONFIRMED': 'status-pending',
            'PACKING': 'status-pending',
            'PROCESSING': 'status-pending',
            'SHIPPING': 'status-pending',
            'SHIPPED': 'status-pending',
            'DELIVERED': 'status-paid',
            'RECEIVED': 'status-paid'
        };
        return statusMap[s] || 'status-unknown';
    }

    getStatusLabel(status) {
        const s = String(status || '').toUpperCase();
        const labelMap = {
            'PENDING': 'Chờ xác nhận',
            'CONFIRMED': 'Đã xác nhận',
            'PACKING': 'Đang đóng gói',
            'PROCESSING': 'Đang đóng gói',
            'SHIPPING': 'Đang giao hàng',
            'SHIPPED': 'Đang giao hàng',
            'PAID': 'Đã thanh toán (Sẵn sàng đóng gói)',
            'UNPAID': 'Chưa thanh toán',
            'DELIVERED': 'Giao hàng thành công',
            'RECEIVED': 'Hoàn tất',
            'CANCELLED': 'Đã hủy',
            'RETURNED': 'Đã trả hàng'
        };
        return labelMap[s] || s || '-';
    }

    /**
     * Format currency for display
     */
    formatCurrency(amount) {
        if (!amount) return '0 ₫';
        return new Intl.NumberFormat('vi-VN', {
            style: 'currency',
            currency: 'VND',
            minimumFractionDigits: 0
        }).format(amount);
    }

    /**
     * Show error message
     */
    showError(message) {
        alert('Lỗi: ' + message);
    }

    /**
     * Create history container if it doesn't exist
     */
    createHistoryContainer() {
        const container = document.createElement('div');
        container.id = 'purchaseHistoryContainer';
        container.className = 'purchase-history-container';
        return container;
    }

    /**
     * Create details modal if it doesn't exist
     */
    createDetailsModal() {
        const modal = document.createElement('div');
        modal.id = 'purchaseDetailsModal';
        modal.className = 'modal';
        document.body.appendChild(modal);
        return modal;
    }
}

// Initialize global instance
const purchaseHistory = new CustomerPurchaseHistory();
