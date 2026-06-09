// Shared app menu + logout for POS pages.
(() => {
    const APP_TILES = [
        { key: 'pos', label: 'Ban hang', icon: 'BH', color: 'app-blue', href: '/pages/employee-dashboard.html' },
        { key: 'promotions', label: 'Khuyen mai', icon: 'KM', color: 'app-green', href: '/pages/promotions.html' },
        { key: 'invoices', label: 'DS hóa đơn', icon: 'HD', color: 'app-pink', href: '/pages/invoice-list.html' },
        { key: 'return-request', label: 'Đổi trả hàng', icon: 'ĐT', color: 'app-red', href: '/pages/return-request.html' },
        { key: 'warranty-request', label: 'Bảo hành', icon: 'BH', color: 'app-teal', href: '/pages/warranty-request.html' },
        { key: 'print', label: 'Máy in - Mẫu in', icon: 'IN', color: 'app-orange', href: '/pages/print-templates.html' },
        { key: 'daily-report', label: 'Bao cao theo ngay', icon: 'BC', color: 'app-teal', href: '/pages/daily-report.html' },
        { key: 'access-log', label: 'Nhat ky truy cap', icon: 'NK', color: 'app-gray', href: '/pages/access-log.html' },
        { key: 'management', label: 'Trang quan ly', icon: 'QL', color: 'app-indigo', href: '/pages/management.html' },
        { key: 'guide', label: 'Huong dan', icon: 'HD', color: 'app-purple', href: '/pages/guide.html' },
        { key: 'locations', label: 'Vi tri ban hang', icon: 'VT', color: 'app-red', href: '/pages/shop-locations.html' },
        { key: 'intro', label: 'Gioi thieu', icon: 'GT', color: 'app-orange', href: '/pages/introduction.html' }
    ];

    function createIconButton(id, title, svg) {
        const btn = document.createElement('button');
        btn.id = id;
        btn.className = 'icon-btn';
        btn.title = title;
        btn.setAttribute('aria-label', title);
        btn.type = 'button';
        btn.innerHTML = svg;
        return btn;
    }

    function ensureHeaderButtons() {
        const headerActions = document.querySelector('.pos-header .header-actions');
        if (!headerActions) return { logoutBtn: null, appMenuBtn: null };

        let logoutBtn = document.getElementById('logoutBtn');
        if (!logoutBtn) {
            logoutBtn = createIconButton(
                'logoutBtn',
                'Đăng xuất',
                `<svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
                    <path d="M10 6H5v12h5" />
                    <path d="M14 16l4-4-4-4" />
                    <path d="M18 12H9" />
                </svg>`
            );
            headerActions.appendChild(logoutBtn);
        }

        let appMenuBtn = document.getElementById('appMenuBtn');
        if (!appMenuBtn) {
            appMenuBtn = createIconButton(
                'appMenuBtn',
                'Ứng dụng',
                `<svg viewBox="0 0 24 24" class="icon-svg" aria-hidden="true">
                    <circle cx="6" cy="6" r="1.5" />
                    <circle cx="12" cy="6" r="1.5" />
                    <circle cx="18" cy="6" r="1.5" />
                    <circle cx="6" cy="12" r="1.5" />
                    <circle cx="12" cy="12" r="1.5" />
                    <circle cx="18" cy="12" r="1.5" />
                </svg>`
            );
            headerActions.appendChild(appMenuBtn);
        }

        return { logoutBtn, appMenuBtn };
    }

    function buildMenuGrid() {
        return APP_TILES.map(tile => `
            <button class="app-tile" data-app="${tile.key}" data-href="${tile.href}">
                <span class="app-icon ${tile.color}">${tile.icon}</span>
                <span>${tile.label}</span>
            </button>
        `).join('');
    }

    function ensureAppMenuModal() {
        let modal = document.getElementById('appMenuModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.className = 'modal side';
            modal.id = 'appMenuModal';
            modal.setAttribute('aria-hidden', 'true');
            modal.innerHTML = `
                <div class="modal-content app-menu">
                    <div class="modal-header">
                        <h3>Ứng dụng</h3>
                        <button id="closeAppMenu" class="icon-btn small" type="button" aria-label="Đóng">×</button>
                    </div>
                    <div class="app-menu-grid">
                        ${buildMenuGrid()}
                    </div>
                </div>
            `;
            document.body.appendChild(modal);
        } else {
            const grid = modal.querySelector('.app-menu-grid');
            if (grid && !grid.dataset.boundAppShell) {
                grid.innerHTML = buildMenuGrid();
            }
        }
        return modal;
    }

    function bindAppMenu(modal, openBtn) {
        if (!modal || !openBtn) return;
        if (!openBtn.dataset.boundAppShell) {
            openBtn.addEventListener('click', () => {
                modal.classList.add('show');
                modal.setAttribute('aria-hidden', 'false');
            });
            openBtn.dataset.boundAppShell = 'true';
        }

        const closeBtn = modal.querySelector('#closeAppMenu');
        if (closeBtn && !closeBtn.dataset.boundAppShell) {
            closeBtn.addEventListener('click', () => {
                modal.classList.remove('show');
                modal.setAttribute('aria-hidden', 'true');
            });
            closeBtn.dataset.boundAppShell = 'true';
        }

        if (!modal.dataset.boundAppShell) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.classList.remove('show');
                    modal.setAttribute('aria-hidden', 'true');
                }
            });
            modal.dataset.boundAppShell = 'true';
        }

        const grid = modal.querySelector('.app-menu-grid');
        if (grid && !grid.dataset.boundAppShell) {
            grid.addEventListener('click', (e) => {
                const tile = e.target.closest('.app-tile[data-href]');
                if (!tile) return;
                const href = tile.dataset.href;
                if (href) {
                    window.location.href = href;
                }
            });
            grid.dataset.boundAppShell = 'true';
        }
    }

    function bindLogout(btn) {
        if (!btn || btn.dataset.boundAppShell) return;
        btn.addEventListener('click', () => {
            if (confirm('Đăng xuất?')) {
                sessionStorage.clear();
                window.location.href = '/pages/login.html';
            }
        });
        btn.dataset.boundAppShell = 'true';
    }

    document.addEventListener('DOMContentLoaded', () => {
        const { logoutBtn, appMenuBtn } = ensureHeaderButtons();
        const modal = ensureAppMenuModal();
        bindAppMenu(modal, appMenuBtn);
        bindLogout(logoutBtn);
        document.querySelectorAll('.logo-mark').forEach((logo) => {
            if (logo.dataset.boundAppShell) return;
            logo.style.cursor = 'pointer';
            logo.addEventListener('click', () => {
                window.location.href = '/pages/employee-dashboard.html';
            });
            logo.dataset.boundAppShell = 'true';
        });
    });
})();


