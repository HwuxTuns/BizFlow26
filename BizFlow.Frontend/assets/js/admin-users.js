/**
 * Admin Users Management
 * Quản lý người dùng
 */

class AdminUsersManager {
    constructor() {
        this.api = adminAPI;
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalPages = 0;
        this.users = [];
        this.isLoading = false;
    }

    /**
     * Khởi tạo
     */
    async init() {
        this.setupEventListeners();
        await this.loadUsers();
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Search
        const searchInput = document.querySelector('.search input');
        if (searchInput) {
            let searchTimeout;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(() => {
                    this.searchUsers(e.target.value);
                }, 500);
            });
        }

        // Pagination
        const prevBtn = document.getElementById('prevPage');
        const nextBtn = document.getElementById('nextPage');
        if (prevBtn) prevBtn.addEventListener('click', () => this.previousPage());
        if (nextBtn) nextBtn.addEventListener('click', () => this.nextPage());
    }

    /**
     * Load người dùng
     */
    async loadUsers() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading();

        try {
            const result = await this.api.getUsers(this.currentPage, this.pageSize);
            
            if (result.success) {
                this.users = result.data || result.users || [];
                this.totalPages = result.totalPages || Math.ceil((result.total || 0) / this.pageSize);
                this.renderUsers();
                this.updatePagination();
            } else {
                this.showError(result.error || 'Không thể tải dữ liệu người dùng');
            }
        } catch (error) {
            this.showError('Lỗi kết nối: ' + error.message);
        } finally {
            this.isLoading = false;
            this.hideLoading();
        }
    }

    /**
     * Render danh sách người dùng
     */
    renderUsers() {
        const tbody = document.getElementById('usersBody');
        if (!tbody) return;

        if (this.users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-box">
                        <div style="text-align: center; padding: 20px;">
                            <div style="font-size: 24px; margin-bottom: 10px;">📭</div>
                            <div>Chưa có người dùng nào</div>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = this.users.map((user, index) => `
            <tr>
                <td><strong>${this.currentPage * this.pageSize + index + 1}</strong></td>
                <td>
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div class="avatar" style="width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;">
                            ${user.name ? user.name.charAt(0).toUpperCase() : 'U'}
                        </div>
                        <div>
                            <div style="font-weight: 500;">${this.escapeHtml(user.name || 'N/A')}</div>
                            <div style="font-size: 12px; color: #666;">${this.escapeHtml(user.email || 'N/A')}</div>
                        </div>
                    </div>
                </td>
                <td>
                    <span class="status-badge" style="background: ${this.getRoleColor(user.role)}; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">
                        ${this.escapeHtml(user.role || 'USER')}
                    </span>
                </td>
                <td>
                    <span class="status-badge" style="background: ${user.active ? '#4CAF50' : '#FF9800'}; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px;">
                        ${user.active ? '✓ Hoạt động' : '✗ Vô hiệu'}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 8px;">
                        <button class="btn-small" onclick="usersManager.editUser(${user.id})" title="Chỉnh sửa">
                            ✏️
                        </button>
                        <button class="btn-small btn-danger" onclick="usersManager.deleteUserConfirm(${user.id})" title="Xóa">
                            🗑️
                        </button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    /**
     * Tìm kiếm người dùng
     */
    async searchUsers(keyword) {
        if (!keyword.trim()) {
            this.currentPage = 0;
            await this.loadUsers();
            return;
        }

        this.isLoading = true;
        this.showLoading();

        try {
            const result = await this.api.searchUsers(keyword);
            
            if (result.success) {
                this.users = result.data || result.users || [];
                this.currentPage = 0;
                this.totalPages = 1;
                this.renderUsers();
                this.updatePagination();
            } else {
                this.showError(result.error || 'Tìm kiếm thất bại');
            }
        } catch (error) {
            this.showError('Lỗi tìm kiếm: ' + error.message);
        } finally {
            this.isLoading = false;
            this.hideLoading();
        }
    }

    /**
     * Edit người dùng
     */
    editUser(userId) {
        const user = this.users.find(u => u.id === userId);
        if (!user) {
            alert('Không tìm thấy người dùng');
            return;
        }

        const newName = prompt('Tên người dùng:', user.name);
        if (!newName) return;

        this.updateUser(userId, { name: newName });
    }

    /**
     * Update người dùng
     */
    async updateUser(userId, userData) {
        if (confirm('Bạn có chắc muốn cập nhật người dùng này?')) {
            try {
                const result = await this.api.updateUser(userId, userData);
                if (result.success) {
                    alert('✓ Cập nhật thành công');
                    await this.loadUsers();
                } else {
                    alert('✗ Cập nhật thất bại: ' + (result.error || 'Unknown error'));
                }
            } catch (error) {
                alert('✗ Lỗi: ' + error.message);
            }
        }
    }

    /**
     * Xóa người dùng (xác nhận)
     */
    deleteUserConfirm(userId) {
        const user = this.users.find(u => u.id === userId);
        if (!user) {
            alert('Không tìm thấy người dùng');
            return;
        }

        if (confirm(`Bạn có chắc muốn xóa người dùng "${user.name}"?\n\nHành động này không thể hoàn tác!`)) {
            this.deleteUser(userId);
        }
    }

    /**
     * Xóa người dùng
     */
    async deleteUser(userId) {
        try {
            const result = await this.api.deleteUser(userId);
            if (result.success) {
                alert('✓ Xóa thành công');
                await this.loadUsers();
            } else {
                alert('✗ Xóa thất bại: ' + (result.error || 'Unknown error'));
            }
        } catch (error) {
            alert('✗ Lỗi: ' + error.message);
        }
    }

    /**
     * Next page
     */
    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadUsers();
        }
    }

    /**
     * Previous page
     */
    previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadUsers();
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
     * Get role color
     */
    getRoleColor(role) {
        const colors = {
            'ADMIN': '#E91E63',
            'MANAGER': '#2196F3',
            'STAFF': '#4CAF50',
            'USER': '#9C27B0',
            'CUSTOMER': '#FF9800',
        };
        return colors[role] || '#757575';
    }

    /**
     * Show loading
     */
    showLoading() {
        const tbody = document.getElementById('usersBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-box">
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
        const tbody = document.getElementById('usersBody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="empty-box">
                        <div style="text-align: center; padding: 20px; color: #d32f2f;">
                            <div style="font-size: 24px; margin-bottom: 10px;">⚠️</div>
                            <div>${this.escapeHtml(message)}</div>
                            <button onclick="usersManager.loadUsers()" style="margin-top: 10px; padding: 6px 12px; background: #2196F3; color: white; border: none; border-radius: 4px; cursor: pointer;">
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
        return text.replace(/[&<>"']/g, m => map[m]);
    }
}

// Initialize
let usersManager;
window.addEventListener('DOMContentLoaded', () => {
    usersManager = new AdminUsersManager();
    usersManager.init();
});
