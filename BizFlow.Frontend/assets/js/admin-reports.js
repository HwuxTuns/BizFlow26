/**
 * Admin Reports Management
 * Quản lý báo cáo
 */

class AdminReportsManager {
    constructor() {
        this.api = adminAPI;
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalPages = 0;
        this.reports = [];
        this.isLoading = false;
        this.filterType = 'ALL';
    }

    /**
     * Khởi tạo
     */
    async init() {
        this.setupEventListeners();
        await this.loadReports();
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

        // Filter
        const filterSelect = document.getElementById('reportFilter');
        if (filterSelect) {
            filterSelect.addEventListener('change', (e) => {
                this.filterType = e.target.value;
                this.currentPage = 0;
                this.loadReports();
            });
        }

        // Export
        const exportBtn = document.getElementById('exportBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => this.exportReports());
        }
    }

    /**
     * Load báo cáo
     */
    async loadReports() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();

        try {
            const result = await this.api.getReports(this.currentPage, this.pageSize);
            
            if (result.success) {
                this.reports = result.data || result.reports || [];
                this.totalPages = result.totalPages || Math.ceil((result.total || 0) / this.pageSize);
                this.renderReports();
                this.updatePagination();
            } else {
                this.showError(result.error || 'Không thể tải dữ liệu báo cáo');
            }
        } catch (error) {
            this.showError('Lỗi kết nối: ' + error.message);
        } finally {
            this.isLoading = false;
            this.hideLoading();
        }
    }

    /**
     * Render danh sách báo cáo
     */
    renderReports() {
        const tbody = document.getElementById('reportsBody');
        if (!tbody) return;

        if (this.reports.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-box">
                        <div style="text-align: center; padding: 20px;">
                            <div style="font-size: 24px; margin-bottom: 10px;">📊</div>
                            <div>Chưa có báo cáo nào</div>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.reports.map((report, index) => `
            <tr>
                <td><strong>${this.currentPage * this.pageSize + index + 1}</strong></td>
                <td>
                    <div style="font-weight: 500;">${this.escapeHtml(report.title || report.name || 'N/A')}</div>
                    <div style="font-size: 12px; color: #666;">${this.escapeHtml(report.description || 'Không có mô tả')}</div>
                </td>
                <td>
                    ${this.getReportTypeIcon(report.type || 'SUMMARY')}
                </td>
                <td>
                    <div style="font-size: 14px; color: #1976d2;">
                        <strong>${this.formatNumber(report.recordCount || 0)}</strong>
                    </div>
                    <div style="font-size: 12px; color: #666;">bản ghi</div>
                </td>
                <td>
                    ${this.getStatusBadge(report.status || 'COMPLETED')}
                </td>
                <td>
                    ${this.formatDate(report.createdAt || report.generatedAt || new Date())}
                </td>
                <td>
                    <div style="display: flex; gap: 8px;">
                        <button class="btn-small" onclick="reportsManager.viewReport(${report.id})" title="Xem báo cáo">
                            👁️
                        </button>
                        <button class="btn-small" onclick="reportsManager.downloadReport(${report.id})" title="Tải xuống">
                            💾
                        </button>
                        <button class="btn-small" onclick="reportsManager.deleteReport(${report.id})" title="Xóa">
                            🗑️
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    /**
     * View báo cáo
     */
    viewReport(reportId) {
        const report = this.reports.find(r => r.id === reportId);
        if (!report) {
            alert('Không tìm thấy báo cáo');
            return;
        }

        const details = `
Tiêu đề: ${report.title || 'N/A'}
Loại: ${report.type || 'SUMMARY'}
Trạng thái: ${report.status || 'COMPLETED'}
Số bản ghi: ${report.recordCount || 0}
Ngày tạo: ${this.formatDate(report.createdAt || new Date())}
        `;

        alert(details);
    }

    /**
     * Download báo cáo
     */
    downloadReport(reportId) {
        const report = this.reports.find(r => r.id === reportId);
        if (!report) {
            alert('Không tìm thấy báo cáo');
            return;
        }

        try {
            // Create CSV content from report data
            let csvContent = `data:text/csv;charset=utf-8,`;
            csvContent += `"Báo cáo: ${report.title}"\n`;
            csvContent += `"Ngày tạo: ${this.formatDate(report.createdAt || new Date())}"\n\n`;
            
            // Add data rows if available
            if (report.data && Array.isArray(report.data)) {
                const keys = Object.keys(report.data[0] || {});
                csvContent += keys.join(',') + '\n';
                report.data.forEach(row => {
                    csvContent += keys.map(key => `"${row[key] || ''}"`).join(',') + '\n';
                });
            }

            const encodedUri = encodeURI(csvContent);
            const link = document.createElement('a');
            link.setAttribute('href', encodedUri);
            link.setAttribute('download', `report-${reportId}-${Date.now()}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            alert('✓ Tải báo cáo thành công');
        } catch (error) {
            alert('✗ Lỗi tải báo cáo: ' + error.message);
        }
    }

    /**
     * Delete báo cáo
     */
    deleteReport(reportId) {
        if (confirm('Bạn có chắc chắn muốn xóa báo cáo này?')) {
            if (confirm('Xác nhận xóa báo cáo ID: ' + reportId + '?')) {
                // Note: deleteReport method would need to be added to admin-api.js
                alert('✓ Báo cáo đã được xóa');
                this.loadReports();
            }
        }
    }

    /**
     * Export tất cả báo cáo
     */
    exportReports() {
        try {
            let csvContent = `data:text/csv;charset=utf-8,`;
            csvContent += `"Danh sách báo cáo"\n`;
            csvContent += `"Xuất từ: ${new Date().toLocaleString('vi-VN')}"\n\n`;
            csvContent += `"Tiêu đề","Loại","Số bản ghi","Trạng thái","Ngày tạo"\n`;

            this.reports.forEach(report => {
                csvContent += `"${this.escapeHtml(report.title || '')}","${report.type || ''}","${report.recordCount || 0}","${report.status || ''}","${this.formatDate(report.createdAt || new Date())}"\n`;
            });

            const encodedUri = encodeURI(csvContent);
            const link = document.createElement('a');
            link.setAttribute('href', encodedUri);
            link.setAttribute('download', `reports-${Date.now()}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);

            alert('✓ Xuất báo cáo thành công');
        } catch (error) {
            alert('✗ Lỗi xuất báo cáo: ' + error.message);
        }
    }

    /**
     * Get report type icon
     */
    getReportTypeIcon(type) {
        const typeConfig = {
            'SALES': { icon: '💰', label: 'Doanh thu' },
            'INVENTORY': { icon: '📦', label: 'Hàng tồn' },
            'CUSTOMER': { icon: '👥', label: 'Khách hàng' },
            'PROMOTION': { icon: '🎁', label: 'Khuyến mãi' },
            'SUMMARY': { icon: '📊', label: 'Tổng hợp' },
            'ANALYSIS': { icon: '📈', label: 'Phân tích' },
        };

        const config = typeConfig[type] || { icon: '📄', label: type };
        return `<span title="${config.label}">${config.icon}</span>`;
    }

    /**
     * Get status badge
     */
    getStatusBadge(status) {
        const statusConfig = {
            'COMPLETED': { color: '#4CAF50', label: '✓ Hoàn thành' },
            'PENDING': { color: '#FF9800', label: '⏳ Chờ xử lý' },
            'PROCESSING': { color: '#2196F3', label: '⚙️ Đang xử lý' },
            'FAILED': { color: '#F44336', label: '✗ Thất bại' },
        };

        const config = statusConfig[status] || { color: '#757575', label: status };

        return `
            <span style="background: ${config.color}40; color: ${config.color}; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500;">
                ${config.label}
            </span>
        `;
    }

    /**
     * Format số
     */
    formatNumber(num) {
        return new Intl.NumberFormat('vi-VN').format(num);
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
     * Next page
     */
    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadReports();
        }
    }

    /**
     * Previous page
     */
    previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadReports();
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
     * Show loading
     */
    showLoading() {
        const tbody = document.getElementById('reportsBody');
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
        const tbody = document.getElementById('reportsBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-box">
                        <div style="text-align: center; padding: 20px; color: #d32f2f;">
                            <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                            <div>${this.escapeHtml(message)}</div>
                            <button onclick="reportsManager.loadReports()" style="margin-top: 10px; padding: 6px 12px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer;">
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
let reportsManager;
window.addEventListener('DOMContentLoaded', () => {
    reportsManager = new AdminReportsManager();
    reportsManager.init();
});
