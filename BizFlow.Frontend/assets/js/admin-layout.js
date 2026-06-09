(function () {
    const roleRedirects = {
        OWNER: '/pages/owner-dashboard.html',
        EMPLOYEE: '/pages/employee-dashboard.html'
    };

    function enforceAuth() {
        const token = sessionStorage.getItem('accessToken');
        const role = sessionStorage.getItem('role');
        if (!token) {
            window.location.href = '/pages/login.html';
            return;
        }
        if (role && role !== 'ADMIN' && roleRedirects[role]) {
            window.location.href = roleRedirects[role];
        }
    }

    function initials(name) {
        return name
            .split(' ')
            .map(part => part[0])
            .filter(Boolean)
            .join('')
            .slice(0, 2)
            .toUpperCase() || 'AD';
    }

    function renderHeader() {
        const username = sessionStorage.getItem('username') || 'Admin';
        const greetingEl = document.getElementById('userGreeting');
        const avatarEl = document.getElementById('userAvatar');
        const sidebarTime = document.getElementById('sidebarTime');
        if (greetingEl) {
            greetingEl.textContent = username;
        }
        if (avatarEl) {
            avatarEl.textContent = initials(username);
        }
        if (sidebarTime) {
            sidebarTime.textContent = new Date().toLocaleString('vi-VN', { hour12: false });
        }
    }

    function setActiveNav() {
        const currentPage = document.body.dataset.adminPage;
        document.querySelectorAll('.nav-item').forEach(item => {
            const target = item.dataset.page;
            item.classList.toggle('active', target && target === currentPage);
        });
    }

    function logout() {
        if (window.confirm('Bạn có chắc muốn đăng xuất?')) {
            sessionStorage.clear();
            window.location.href = '/pages/login.html';
        }
    }

    const adminLayout = {
        enforceAuth,
        renderHeader,
        setActiveNav,
        logout,
        initials
    };

    window.adminLayout = adminLayout;
})();
