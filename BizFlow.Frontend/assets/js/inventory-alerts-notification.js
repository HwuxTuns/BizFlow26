// BizFlow Inventory Alert Notification System
// Tự động check và hiển thị popup thông báo tồn kho & tồn kệ

(function() {
    const API_BASE = 'http://localhost:8084';
    let alertCheckInterval = null;

    // Check if user is logged in and has permission
    function canCheckAlerts() {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        return token && (role === 'OWNER' || role === 'ADMIN');
    }

    // Fetch alerts from API
    async function fetchAlerts() {
        if (!canCheckAlerts()) return [];

        try {
            const token = localStorage.getItem('token');
            const lastChecked = localStorage.getItem('inventory_last_checked');
            
            let url = `${API_BASE}/api/inventory/alerts`;
            if (lastChecked) {
                url += `?lastChecked=${lastChecked}`;
            }

            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('Failed to fetch alerts');
            }

            const alerts = await response.json();
            
            // Update localStorage
            const today = new Date().toISOString().split('T')[0];
            localStorage.setItem('inventory_last_checked', today);

            return alerts;
        } catch (error) {
            console.error('[Alerts] Error fetching alerts:', error);
            return [];
        }
    }

    // Update badge count in header
    function updateBadge(count, hasDanger) {
        const badge = document.getElementById('notificationBadge');
        const badgeCount = document.getElementById('badgeCount');

        if (!badge || !badgeCount) return;

        if (count > 0) {
            badgeCount.textContent = count > 99 ? '99+' : count;
            badgeCount.style.display = 'block';
            
            if (hasDanger) {
                badge.classList.add('has-danger');
            } else {
                badge.classList.remove('has-danger');
            }
        } else {
            badgeCount.style.display = 'none';
            badge.classList.remove('has-danger');
        }
    }

    // Show SweetAlert2 popup for danger alerts
    function showDangerAlertsPopup(dangerAlerts) {
        if (dangerAlerts.length === 0) return;

        // Check if already shown today
        const lastShown = localStorage.getItem('danger_alert_shown_date');
        const today = new Date().toISOString().split('T')[0];
        
        if (lastShown === today) {
            return; // Already shown today, don't annoy user
        }

        const alertsHTML = dangerAlerts.map(alert => {
            const icon = alert.type === 'SHELF' ? '📦' : '🏪';
            const location = alert.type === 'SHELF' ? 'Kệ' : 'Kho';
            return `
                <div style="text-align: left; padding: 10px; background: #fef2f2; border-left: 4px solid #ef4444; border-radius: 6px; margin-bottom: 10px;">
                    <div style="font-weight: 600; margin-bottom: 4px;">
                        ${icon} ${alert.productName}
                    </div>
                    <div style="font-size: 13px; color: #6b7280;">
                        📍 ${location} • Còn lại: <strong>${alert.quantity}</strong>
                    </div>
                </div>
            `;
        }).join('');

        Swal.fire({
            title: '🚨 Cảnh báo Nguy hiểm!',
            html: `
                <div style="text-align: left;">
                    <p style="margin-bottom: 16px; color: #6b7280;">
                        Có <strong>${dangerAlerts.length}</strong> sản phẩm cần bổ sung gấp:
                    </p>
                    ${alertsHTML}
                </div>
            `,
            icon: 'error',
            confirmButtonText: 'Xem chi tiết',
            confirmButtonColor: '#ef4444',
            showCancelButton: true,
            cancelButtonText: 'Đóng',
            cancelButtonColor: '#6b7280',
            width: '600px'
        }).then((result) => {
            if (result.isConfirmed) {
                window.location.href = 'owner-inventory-alerts.html';
            }
        });

        // Mark as shown today
        localStorage.setItem('danger_alert_shown_date', today);
    }

    // Main function to check and display alerts
    async function checkAndDisplayAlerts() {
        const alerts = await fetchAlerts();
        
        if (alerts.length === 0) {
            updateBadge(0, false);
            return;
        }

        // Count by level
        const dangerAlerts = alerts.filter(a => a.level === 'DANGER');
        const warningAlerts = alerts.filter(a => a.level === 'WARNING');

        // Update badge
        updateBadge(alerts.length, dangerAlerts.length > 0);

        // Show popup only for danger alerts
        if (dangerAlerts.length > 0) {
            // Small delay to ensure page is fully loaded
            setTimeout(() => {
                showDangerAlertsPopup(dangerAlerts);
            }, 1000);
        }
    }

    // Navigate to alerts page
    window.goToAlerts = function() {
        window.location.href = 'owner-inventory-alerts.html';
    };

    // Initialize on page load
    function init() {
        if (!canCheckAlerts()) {
            console.log('[Alerts] User not authorized to check alerts');
            return;
        }

        // Check immediately
        checkAndDisplayAlerts();

        // Check every 5 minutes
        alertCheckInterval = setInterval(checkAndDisplayAlerts, 5 * 60 * 1000);
    }

    // Auto-initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Cleanup on page unload
    window.addEventListener('beforeunload', () => {
        if (alertCheckInterval) {
            clearInterval(alertCheckInterval);
        }
    });
})();
