// Customer List JavaScript
const API_BASE_URL = window.location.hostname === 'localhost' 
    ? 'http://localhost:8000' 
    : 'http://gateway:8000';

// State
let allCustomers = [];
let filteredCustomers = [];
let currentPage = 1;
const itemsPerPage = 20;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadCustomers();
    setupEventListeners();
});

function setupEventListeners() {
    // Search on Enter key
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            searchCustomers();
        }
    });
}

async function loadCustomers() {
    const token = localStorage.getItem('token');
    
    try {
        showLoading(true);
        
        const response = await fetch(`${API_BASE_URL}/api/customers`, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        allCustomers = await response.json();
        filteredCustomers = [...allCustomers];
        
        displayCustomers();
        updateStats();
        
    } catch (error) {
        console.error('Error loading customers:', error);
        showError('Không thể tải danh sách khách hàng');
    } finally {
        showLoading(false);
    }
}

function displayCustomers() {
    const tbody = document.getElementById('customersTableBody');
    
    if (!filteredCustomers || filteredCustomers.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="8">Không tìm thấy khách hàng</td></tr>';
        document.getElementById('paginationControls').style.display = 'none';
        return;
    }

    // Paginate
    const startIdx = (currentPage - 1) * itemsPerPage;
    const endIdx = startIdx + itemsPerPage;
    const pageCustomers = filteredCustomers.slice(startIdx, endIdx);

    tbody.innerHTML = pageCustomers.map(customer => `
        <tr onclick="viewCustomerDetails(${customer.id})">
            <td>KH${String(customer.id).padStart(9, '0')}</td>
            <td>${customer.name || '-'}</td>
            <td>${customer.phone || '-'}</td>
            <td>${customer.email || '-'}</td>
            <td>${getTierBadge(customer.tier)}</td>
            <td>${customer.totalPoints || 0}</td>
            <td>${customer.address || '-'}</td>
            <td onclick="event.stopPropagation()">
                <div class="action-btns">
                    <button class="action-btn view" onclick="viewCustomerDetails(${customer.id})">Xem</button>
                </div>
            </td>
        </tr>
    `).join('');

    // Update pagination
    updatePagination();
}

function updatePagination() {
    const totalPages = Math.ceil(filteredCustomers.length / itemsPerPage);
    
    if (totalPages > 1) {
        document.getElementById('paginationControls').style.display = 'flex';
        document.getElementById('pageInfo').textContent = `Trang ${currentPage}/${totalPages}`;
        document.getElementById('prevPage').disabled = currentPage === 1;
        document.getElementById('nextPage').disabled = currentPage === totalPages;
    } else {
        document.getElementById('paginationControls').style.display = 'none';
    }
}

function changePage(direction) {
    const totalPages = Math.ceil(filteredCustomers.length / itemsPerPage);
    const newPage = currentPage + direction;
    
    if (newPage >= 1 && newPage <= totalPages) {
        currentPage = newPage;
        displayCustomers();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

function searchCustomers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase().trim();
    
    if (!searchTerm) {
        filteredCustomers = [...allCustomers];
    } else {
        filteredCustomers = allCustomers.filter(customer => {
            const customerId = `KH${String(customer.id).padStart(9, '0')}`.toLowerCase();
            const name = (customer.name || '').toLowerCase();
            const phone = (customer.phone || '').toLowerCase();
            const email = (customer.email || '').toLowerCase();
            
            return customerId.includes(searchTerm) ||
                   name.includes(searchTerm) ||
                   phone.includes(searchTerm) ||
                   email.includes(searchTerm);
        });
    }
    
    currentPage = 1;
    displayCustomers();
}

function filterCustomers() {
    const tierFilter = document.getElementById('tierFilter').value;
    const searchTerm = document.getElementById('searchInput').value.toLowerCase().trim();
    
    filteredCustomers = allCustomers.filter(customer => {
        // Apply tier filter
        const tierMatch = !tierFilter || customer.tier === tierFilter;
        
        // Apply search filter
        let searchMatch = true;
        if (searchTerm) {
            const customerId = `KH${String(customer.id).padStart(9, '0')}`.toLowerCase();
            const name = (customer.name || '').toLowerCase();
            const phone = (customer.phone || '').toLowerCase();
            const email = (customer.email || '').toLowerCase();
            
            searchMatch = customerId.includes(searchTerm) ||
                         name.includes(searchTerm) ||
                         phone.includes(searchTerm) ||
                         email.includes(searchTerm);
        }
        
        return tierMatch && searchMatch;
    });
    
    currentPage = 1;
    displayCustomers();
}

function updateStats() {
    // Total customers
    document.getElementById('totalCustomers').textContent = allCustomers.length;
    
    // New customers this month (simplified - count all for now)
    document.getElementById('newCustomers').textContent = allCustomers.length;
    
    // VIP customers (Gold and above)
    const vipCount = allCustomers.filter(c => 
        c.tier === 'VANG' || c.tier === 'BACH_KIM' || c.tier === 'KIM_CUONG'
    ).length;
    document.getElementById('vipCustomers').textContent = vipCount;
}

function viewCustomerDetails(customerId) {
    window.location.href = `customer-details.html?id=${customerId}`;
}

function openAddCustomerModal() {
    alert('Chức năng thêm khách hàng - Tính năng đang phát triển');
}

// Utility Functions
function getTierBadge(tier) {
    const tierInfo = {
        'DONG': { name: 'Member', class: 'dong' },
        'BAC': { name: 'Silver', class: 'bac' },
        'VANG': { name: 'Gold', class: 'vang' },
        'BACH_KIM': { name: 'Platinum', class: 'bach-kim' },
        'KIM_CUONG': { name: 'Diamond', class: 'kim-cuong' }
    };
    
    const info = tierInfo[tier] || tierInfo['DONG'];
    return `<span class="tier-badge ${info.class}">${info.name}</span>`;
}

function showLoading(show) {
    const tbody = document.getElementById('customersTableBody');
    if (show) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="8">Đang tải dữ liệu...</td></tr>';
        document.body.style.cursor = 'wait';
    } else {
        document.body.style.cursor = 'default';
    }
}

function showError(message) {
    alert('Lỗi: ' + message);
}
