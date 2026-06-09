// Customer Details JavaScript
const API_BASE_URL = window.location.hostname === 'localhost' 
    ? 'http://localhost:8000' 
    : 'http://gateway:8000';

// State
let currentCustomer = null;
let currentOrders = [];
let currentPage = 1;
const itemsPerPage = 10;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadCustomerData();
});

function setupEventListeners() {
    // Tab switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });

    // Buttons
    document.getElementById('closeBtn')?.addEventListener('click', () => history.back());
    document.getElementById('editBtn')?.addEventListener('click', () => enableEditMode());
    document.getElementById('saveInfoBtn')?.addEventListener('click', () => saveCustomerInfo());
    
    // Pagination
    document.getElementById('prevPage')?.addEventListener('click', () => changePage(-1));
    document.getElementById('nextPage')?.addEventListener('click', () => changePage(1));
}

function switchTab(tabName) {
    // Update tab buttons
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.tab === tabName);
    });

    // Update tab content
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.toggle('active', content.id === tabName);
    });
}

async function loadCustomerData() {
    const customerId = getCustomerIdFromUrl();
    
    if (!customerId) {
        showError('Không tìm thấy mã khách hàng');
        return;
    }

    try {
        showLoading(true);
        
        // Load customer info and order history in parallel
        const [customerData, ordersData] = await Promise.all([
            fetchCustomerInfo(customerId),
            fetchOrderHistory(customerId)
        ]);

        currentCustomer = customerData;
        currentOrders = ordersData;

        displayCustomerData(customerData);
        displayOrderHistory(ordersData);
        
    } catch (error) {
        console.error('Error loading customer data:', error);
        showError('Không thể tải thông tin khách hàng');
    } finally {
        showLoading(false);
    }
}

async function fetchCustomerInfo(customerId) {
    const token = localStorage.getItem('token');
    
    const response = await fetch(`${API_BASE_URL}/api/customers/${customerId}`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
}

async function fetchOrderHistory(customerId) {
    const token = localStorage.getItem('token');
    
    const response = await fetch(`${API_BASE_URL}/api/customers/${customerId}/orders`, {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return Array.isArray(data) ? data : [];
}

function displayCustomerData(customer) {
    // Update page title
    document.querySelector('.customer-name-value').textContent = customer.name || '-';
    
    // Overview Tab
    document.getElementById('overviewCustomerId').textContent = `KH${String(customer.id).padStart(9, '0')}`;
    document.getElementById('overviewPhone').textContent = customer.phone || 'Chưa có thông tin';
    document.getElementById('overviewCccd').textContent = customer.cccd || 'Chưa có thông tin';
    document.getElementById('overviewEmail').textContent = customer.email || 'Chưa có thông tin';
    document.getElementById('overviewDob').textContent = formatDate(customer.dob) || 'Chưa có thông tin';
    document.getElementById('overviewGender').textContent = customer.gender || 'Chưa có thông tin';
    document.getElementById('overviewAddress').textContent = customer.address || 'Chưa có thông tin';
    
    // Member info
    document.getElementById('overviewLomasId').textContent = customer.id ? `--` : '-';
    const tierBadge = document.getElementById('overviewTier');
    tierBadge.textContent = getTierName(customer.tier);
    tierBadge.style.background = getTierColor(customer.tier);
    
    document.getElementById('overviewGroup').textContent = 'Chưa có thông tin';
    document.getElementById('overviewAssignee').textContent = 'Chưa có thông tin';
    
    // Company info
    document.getElementById('overviewCompany').textContent = 'Chưa có thông tin';
    document.getElementById('overviewNote').textContent = 'Chưa có thông tin';
    document.getElementById('overviewTaxCode').textContent = 'Chưa có thông tin';
    
    // Info Tab
    document.getElementById('infoCustomerId').value = `KH${String(customer.id).padStart(9, '0')}`;
    document.getElementById('infoCustomerName').value = customer.name || '';
    document.getElementById('infoPhone').value = customer.phone || '';
    document.getElementById('infoEmail').value = customer.email || '';
    document.getElementById('infoCccd').value = customer.cccd || '';
    document.getElementById('infoDob').value = customer.dob || '';
    document.getElementById('infoGender').value = customer.gender || '';
    document.getElementById('infoAddress').value = customer.address || '';
}

function displayOrderHistory(orders) {
    const tbody = document.getElementById('ordersTableBody');
    
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="6">Chưa có lịch sử mua hàng</td></tr>';
        document.getElementById('totalOrders').textContent = '0';
        document.getElementById('totalSpent').textContent = '0đ';
        return;
    }

    // Calculate statistics
    const totalOrders = orders.length;
    const totalSpent = orders.reduce((sum, order) => sum + (parseFloat(order.totalAmount) || 0), 0);
    
    document.getElementById('totalOrders').textContent = totalOrders;
    document.getElementById('totalSpent').textContent = formatCurrency(totalSpent);

    // Display paginated orders
    displayOrdersPage(orders, currentPage);
}

function displayOrdersPage(orders, page) {
    const tbody = document.getElementById('ordersTableBody');
    const startIdx = (page - 1) * itemsPerPage;
    const endIdx = startIdx + itemsPerPage;
    const pageOrders = orders.slice(startIdx, endIdx);

    tbody.innerHTML = pageOrders.map(order => `
        <tr>
            <td>${formatDateTime(order.createdAt)}</td>
            <td>${order.invoiceNumber || '-'}</td>
            <td>TRƯỜNG CHINH</td>
            <td><span class="status-badge ${getStatusClass(order.status)}">${getStatusText(order.status)}</span></td>
            <td>${formatCurrency(order.totalAmount)}</td>
            <td>${order.note || '-'}</td>
        </tr>
    `).join('');

    // Update pagination
    const totalPages = Math.ceil(orders.length / itemsPerPage);
    if (totalPages > 1) {
        document.getElementById('paginationControls').style.display = 'flex';
        document.getElementById('pageInfo').textContent = `${page}/${totalPages} kết quả`;
        document.getElementById('prevPage').disabled = page === 1;
        document.getElementById('nextPage').disabled = page === totalPages;
    } else {
        document.getElementById('paginationControls').style.display = 'none';
    }
}

function changePage(direction) {
    const totalPages = Math.ceil(currentOrders.length / itemsPerPage);
    const newPage = currentPage + direction;
    
    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        displayOrdersPage(currentOrders, currentPage);
    }
}

function enableEditMode() {
    // Enable all input fields in info tab
    const inputs = document.querySelectorAll('#info input:not([readonly]), #info select');
    inputs.forEach(input => input.disabled = false);
    
    // Switch to info tab
    switchTab('info');
    
    // Show save button
    document.getElementById('saveInfoBtn').style.display = 'block';
}

async function saveCustomerInfo() {
    const customerId = currentCustomer.id;
    const token = localStorage.getItem('token');
    
    const updatedData = {
        name: document.getElementById('infoCustomerName').value,
        phone: document.getElementById('infoPhone').value,
        email: document.getElementById('infoEmail').value || null,
        cccd: document.getElementById('infoCccd').value || null,
        dob: document.getElementById('infoDob').value || null,
        gender: document.getElementById('infoGender').value || null,
        address: document.getElementById('infoAddress').value || null
    };

    try {
        showLoading(true);
        
        const response = await fetch(`${API_BASE_URL}/api/customers/${customerId}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedData)
        });

        if (!response.ok) {
            throw new Error('Failed to update customer');
        }

        const updated = await response.json();
        currentCustomer = updated;
        displayCustomerData(updated);
        
        showSuccess('Cập nhật thông tin khách hàng thành công');
        
    } catch (error) {
        console.error('Error updating customer:', error);
        showError('Không thể cập nhật thông tin khách hàng');
    } finally {
        showLoading(false);
    }
}

// Utility Functions
function getCustomerIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}

function formatCurrency(amount) {
    if (!amount) return '0đ';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount).replace('₫', 'đ');
}

function formatDate(dateString) {
    if (!dateString) return null;
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN');
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN') + ' - ' + date.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });
}
 

function getTierName(tier) {
    const tierMap = {
        'DONG': 'Bronze',
        'BAC': 'Silver',
        'VANG': 'Gold',
        'BACH_KIM': 'Platinum',
        'KIM_CUONG': 'Diamond'
    };
    return tierMap[tier] || 'Bronze';
}

function getTierColor(tier) {
    const colorMap = {
        'DONG': '#cd7f32',
        'BAC': '#c0c0c0',
        'VANG': '#ffd700',
        'BACH_KIM': '#e5e4e2',
        'KIM_CUONG': '#b9f2ff'
    };
    return colorMap[tier] || '#667eea';
}

function getStatusClass(status) {
    const s = String(status || '').toUpperCase();
    const statusMap = {
        'PENDING': 'pending',
        'CONFIRMED': 'pending',
        'PACKING': 'pending',
        'PROCESSING': 'pending',
        'SHIPPING': 'pending',
        'SHIPPED': 'pending',
        'PAID': 'success',
        'COMPLETED': 'success',
        'DELIVERED': 'success',
        'RECEIVED': 'success',
        'CANCELLED': 'cancelled',
        'RETURNED': 'cancelled'
    };
    return statusMap[s] || 'success';
}

function getStatusText(status) {
    const s = String(status || '').toUpperCase();
    const textMap = {
        'PENDING': 'Chờ xác nhận',
        'CONFIRMED': 'Đã xác nhận',
        'PACKING': 'Đang đóng gói',
        'PROCESSING': 'Đang đóng gói',
        'SHIPPING': 'Đang giao hàng',
        'SHIPPED': 'Đang giao hàng',
        'PAID': 'Đã thanh toán (Chờ đóng gói)',
        'COMPLETED': 'Đã thanh toán',
        'DELIVERED': 'Giao hàng thành công',
        'RECEIVED': 'Hoàn tất',
        'CANCELLED': 'Đã hủy',
        'RETURNED': 'Đã trả hàng'
    };
    return textMap[s] || 'Đã thanh toán';
}

function showLoading(show) {
    // You can implement a loading spinner here
    if (show) {
        document.body.style.cursor = 'wait';
    } else {
        document.body.style.cursor = 'default';
    }
}

function showError(message) {
    alert('Lỗi: ' + message);
}

function showSuccess(message) {
    alert(message);
}
