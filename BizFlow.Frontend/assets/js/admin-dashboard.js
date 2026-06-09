const API_BASE = '/api';
let currentEditingUserId = null;
let currentEditingBranchId = null;
let allUsers = [];
let allBranches = [];

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
                    <button type="button" class="close" id="appPopupClose" aria-label="Đóng">×</button>
                </div>
                <div id="appPopupMessage" class="app-popup-message"></div>
                <div class="app-popup-actions">
                    <button type="button" class="btn-primary" id="appPopupOk">Đóng</button>
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

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    loadUserInfo();
    setupNavigation();
    setupModals();
    loadDashboard();
    loadUsers();
    loadBranches();
});

// Load user info
function loadUserInfo() {
    const username = sessionStorage.getItem('username') || 'Admin';
    const role = sessionStorage.getItem('role') || 'ADMIN';
    document.getElementById('userName').textContent = username;
    document.getElementById('userRole').textContent = getRoleDisplayName(role);
}

// Get role display name
function getRoleDisplayName(role) {
    const roles = {
        'ADMIN': 'Quản trị viên',
        'OWNER': 'Chủ cửa hàng',
        'MANAGER': 'Quản lý',
        'EMPLOYEE': 'Nhân viên'
    };
    return roles[role] || role;
}

// Setup navigation
function setupNavigation() {
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
        item.addEventListener('click', (e) => {
            if (item.classList.contains('logout')) return;
            e.preventDefault();
            switchPage(item.dataset.page);
            document.querySelectorAll('.nav-item[data-page]').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
        });
    });
}

// Switch pages
function switchPage(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(page + '-page').classList.add('active');
}

// Setup modals
function setupModals() {
    document.getElementById('btnAddUser').addEventListener('click', () => {
        currentEditingUserId = null;
        document.getElementById('userForm').reset();
        document.getElementById('userModalTitle').textContent = 'Thêm Người dùng';
        openUserModal();
    });

    document.getElementById('btnAddBranch').addEventListener('click', () => {
        currentEditingBranchId = null;
        document.getElementById('branchForm').reset();
        document.getElementById('branchModalTitle').textContent = 'Thêm Chi nhánh';
        openBranchModal();
    });

    document.getElementById('userForm').addEventListener('submit', saveUser);
    document.getElementById('branchForm').addEventListener('submit', saveBranch);

    // Modal close buttons
    document.querySelectorAll('.modal .close').forEach(closeBtn => {
        closeBtn.addEventListener('click', (e) => {
            e.target.closest('.modal').classList.remove('show');
        });
    });

    // Close modal when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal')) {
            e.target.classList.remove('show');
        }
    });
}

// Load dashboard stats
async function loadDashboard() {
    try {
        const usersRes = await fetch(`${API_BASE}/users`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const users = await usersRes.json();

        const branchesRes = await fetch(`${API_BASE}/branches`, {
            headers: { 'Authorization': `Bearer ${sessionStorage.getItem('accessToken')}` }
        });
        const branches = await branchesRes.json();

        const owners = users.filter(u => u.role === 'OWNER').length;
        const employees = users.filter(u => u.role === 'EMPLOYEE').length;

        document.getElementById('totalUsers').textContent = users.length;
        document.getElementById('totalBranches').textContent = branches.length;
        document.getElementById('totalOwners').textContent = owners;
        document.getElementById('totalEmployees').textContent = employees;
    } catch (error) {
    }
}

// Load users
async function loadUsers() {
    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch(`${API_BASE}/users`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        allUsers = await response.json();
        renderUsersTable();
        populateOwnerSelect();
    } catch (error) {
    }
}

// Render users table
function renderUsersTable() {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = allUsers.map(user => `
        <tr>
            <td>${user.username}</td>
            <td>${user.email}</td>
            <td>${user.fullName || '-'}</td>
            <td>${getRoleDisplayName(user.role)}</td>
            <td>${user.branch ? user.branch.name : '-'}</td>
            <td>
                <button class="btn-edit" onclick="editUser(${user.id})">Sửa</button>
                <button class="btn-danger" onclick="deleteUser(${user.id})">Xóa</button>
            </td>
        </tr>
    `).join('');
}

// Edit user
function editUser(id) {
    const user = allUsers.find(u => u.id === id);
    if (!user) return;

    currentEditingUserId = id;
    document.getElementById('username').value = user.username;
    document.getElementById('email').value = user.email;
    document.getElementById('fullName').value = user.fullName || '';
    document.getElementById('phoneNumber').value = user.phoneNumber || '';
    document.getElementById('role').value = user.role;
    document.getElementById('branchId').value = user.branch ? user.branch.id : '';
    document.getElementById('password').required = false;
    document.getElementById('userModalTitle').textContent = 'Sửa Người dùng';
    openUserModal();
}

// Delete user
async function deleteUser(id) {
    if (!confirm('Bạn chắc chắn muốn xóa người dùng này?')) return;
    try {
        const token = sessionStorage.getItem('accessToken');
        await fetch(`${API_BASE}/users/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        loadUsers();
    } catch (error) {
        showPopup('Lỗi xóa người dùng: ' + error.message, { type: 'error' });
    }
}

// Save user
async function saveUser(e) {
    e.preventDefault();
    const data = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        fullName: document.getElementById('fullName').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        role: document.getElementById('role').value,
        branchId: document.getElementById('branchId').value || null
    };

    if (!currentEditingUserId && document.getElementById('password').value) {
        data.password = document.getElementById('password').value;
    }

    try {
        const token = sessionStorage.getItem('accessToken');
        const method = currentEditingUserId ? 'PUT' : 'POST';
        const url = currentEditingUserId ? `${API_BASE}/users/${currentEditingUserId}` : `${API_BASE}/users`;

        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            closeUserModal();
            loadUsers();
            loadDashboard();
        } else {
            showPopup('Lỗi: ' + await response.text(), { type: 'error' });
        }
    } catch (error) {
        showPopup('Lỗi: ' + error.message, { type: 'error' });
    }
}

// Load branches
async function loadBranches() {
    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch(`${API_BASE}/branches`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        allBranches = await response.json();
        renderBranchesGrid();
    } catch (error) {
    }
}

// Render branches grid
function renderBranchesGrid() {
    const grid = document.getElementById('branchesGrid');
    grid.innerHTML = allBranches.map(branch => `
        <div class="branch-card">
            <h3>${branch.name}</h3>
            <p><strong>Địa chỉ:</strong> ${branch.address || '-'}</p>
            <p><strong>Điện thoại:</strong> ${branch.phone || '-'}</p>
            <p><strong>Email:</strong> ${branch.email || '-'}</p>
            <div class="owner">${branch.owner ? `Chủ: ${branch.owner.username}` : 'Chưa có chủ'}</div>
            <div class="actions">
                <button class="btn-edit" onclick="editBranch(${branch.id})">Sửa</button>
                <button class="btn-danger" onclick="deleteBranch(${branch.id})">Xóa</button>
            </div>
        </div>
    `).join('');
}

// Edit branch
function editBranch(id) {
    const branch = allBranches.find(b => b.id === id);
    if (!branch) return;

    currentEditingBranchId = id;
    document.getElementById('branchName').value = branch.name;
    document.getElementById('branchAddress').value = branch.address || '';
    document.getElementById('branchPhone').value = branch.phone || '';
    document.getElementById('branchEmail').value = branch.email || '';
    document.getElementById('ownerId').value = branch.owner ? branch.owner.id : '';
    document.getElementById('branchModalTitle').textContent = 'Sửa Chi nhánh';
    openBranchModal();
}

// Delete branch
async function deleteBranch(id) {
    if (!confirm('Bạn chắc chắn muốn xóa chi nhánh này?')) return;
    try {
        const token = sessionStorage.getItem('accessToken');
        await fetch(`${API_BASE}/branches/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        loadBranches();
    } catch (error) {
        showPopup('Lỗi xóa chi nhánh: ' + error.message, { type: 'error' });
    }
}

// Save branch
async function saveBranch(e) {
    e.preventDefault();
    const data = {
        name: document.getElementById('branchName').value,
        address: document.getElementById('branchAddress').value,
        phone: document.getElementById('branchPhone').value,
        email: document.getElementById('branchEmail').value,
        ownerId: document.getElementById('ownerId').value || null
    };

    try {
        const token = sessionStorage.getItem('accessToken');
        const method = currentEditingBranchId ? 'PUT' : 'POST';
        const url = currentEditingBranchId ? `${API_BASE}/branches/${currentEditingBranchId}` : `${API_BASE}/branches`;

        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            closeBranchModal();
            loadBranches();
            loadDashboard();
        } else {
            showPopup('Lỗi: ' + await response.text(), { type: 'error' });
        }
    } catch (error) {
        showPopup('Lỗi: ' + error.message, { type: 'error' });
    }
}

// Populate owner select
function populateOwnerSelect() {
    const select = document.getElementById('ownerId');
    select.innerHTML = '<option value="">-- Chọn chủ cửa hàng --</option>';
    allUsers.filter(u => u.role === 'OWNER').forEach(user => {
        const option = document.createElement('option');
        option.value = user.id;
        option.textContent = user.username;
        select.appendChild(option);
    });
}

// Modal functions
function openUserModal() {
    document.getElementById('userModal').classList.add('show');
}

function closeUserModal() {
    document.getElementById('userModal').classList.remove('show');
}

function openBranchModal() {
    document.getElementById('branchModal').classList.add('show');
}

function closeBranchModal() {
    document.getElementById('branchModal').classList.remove('show');
}

// Populate branch select in user form
async function loadBranchesForSelect() {
    try {
        const token = sessionStorage.getItem('accessToken');
        const response = await fetch(`${API_BASE}/branches`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const branches = await response.json();
        const select = document.getElementById('branchId');
        select.innerHTML = '<option value="">-- Chọn chi nhánh --</option>';
        branches.forEach(branch => {
            const option = document.createElement('option');
            option.value = branch.id;
            option.textContent = branch.name;
            select.appendChild(option);
        });
    } catch (error) {
    }
}

// Load branches for select when opening user modal
document.addEventListener('DOMContentLoaded', () => {
    loadBranchesForSelect();
});

