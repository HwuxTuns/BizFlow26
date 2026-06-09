// API Base URL
const API_BASE_URL = '/api';

// DOM Elements
const loginForm = document.getElementById('loginForm');
const usernameInput = document.getElementById('username');
const passwordInput = document.getElementById('password');
const rememberMeCheckbox = document.getElementById('rememberMe');
const togglePasswordBtn = document.getElementById('togglePassword');
const loginBtn = document.getElementById('loginBtn');
const spinner = document.getElementById('spinner');
const btnText = document.querySelector('.btn-text');
const errorAlert = document.getElementById('errorAlert');
const errorMessage = document.getElementById('errorMessage');
const successAlert = document.getElementById('successAlert');
const successMessage = document.getElementById('successMessage');
const usernameError = document.getElementById('usernameError');
const passwordError = document.getElementById('passwordError');

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    loadSavedCredentials();
    checkAuthStatus();
});

loginForm.addEventListener('submit', handleLogin);
togglePasswordBtn.addEventListener('click', togglePasswordVisibility);
usernameInput.addEventListener('input', clearUsernameError);
passwordInput.addEventListener('input', clearPasswordError);

/**
 * Xử lý sự kiện đăng nhập
 */
async function handleLogin(e) {
    e.preventDefault();
    
    // Xóa thông báo cũ
    hideErrorAlert();
    hideSuccessAlert();
    
    // Kiểm tra tính hợp lệ của input
    if (!validateForm()) {
        return;
    }
    
    // Lấy giá trị từ form
    const username = usernameInput.value.trim();
    const password = passwordInput.value;
    
    // Vô hiệu hóa nút submit và hiển thị loading
    setLoadingState(true);
    
    try {
        // Gọi API đăng nhập
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            // Đăng nhập thành công
            showSuccessAlert('Đăng nhập thành công!');
            
            // Lưu tokens vào sessionStorage (riêng biệt mỗi tab)
            sessionStorage.setItem('accessToken', data.accessToken);
            sessionStorage.setItem('refreshToken', data.refreshToken);
            sessionStorage.setItem('userId', data.userId);
            sessionStorage.setItem('username', data.username);
            sessionStorage.setItem('role', data.role);
            
            // Lưu thông tin đăng nhập nếu chọn "Ghi nhớ đăng nhập"
            if (rememberMeCheckbox.checked) {
                localStorage.setItem('rememberedUsername', username);
            } else {
                localStorage.removeItem('rememberedUsername');
            }
            
            // Chuyển hướng sau 1.5 giây (dựa vào role)
            setTimeout(() => {
                const role = sessionStorage.getItem('role');
                if (role === 'ADMIN') {
                    window.location.href = '/pages/admin-dashboard.html';
                } else if (role === 'OWNER') {
                    window.location.href = '/pages/owner-dashboard.html';
                } else {
                    window.location.href = '/pages/employee-dashboard.html';
                }
            }, 1500);
        } else {
            // Đăng nhập thất bại
            showErrorAlert(data.error || 'Đăng nhập thất bại');
        }
    } catch (error) {
        console.error('Login error:', error);
        showErrorAlert('Lỗi kết nối đến máy chủ. Vui lòng thử lại.');
    } finally {
        setLoadingState(false);
    }
}

/**
 * Kiểm tra tính hợp lệ của form
 */
function validateForm() {
    let isValid = true;
    
    // Kiểm tra username
    if (!usernameInput.value.trim()) {
        showUsernameError('Username không được bỏ trống');
        isValid = false;
    } else if (usernameInput.value.trim().length < 3) {
        showUsernameError('Username phải có ít nhất 3 ký tự');
        isValid = false;
    }
    
    // Kiểm tra password
    if (!passwordInput.value) {
        showPasswordError('Password không được bỏ trống');
        isValid = false;
    } else if (passwordInput.value.length < 6) {
        showPasswordError('Password phải có ít nhất 6 ký tự');
        isValid = false;
    }
    
    return isValid;
}

/**
 * Hiển thị/ẩn mật khẩu
 */
function togglePasswordVisibility() {
    const type = passwordInput.getAttribute('type');
    const newType = type === 'password' ? 'text' : 'password';
    passwordInput.setAttribute('type', newType);
    
    // Đổi icon
    const eyeIcon = togglePasswordBtn.querySelector('.eye-icon');
    eyeIcon.textContent = newType === 'password' ? '👁️' : '👁️‍🗨️';
}

/**
 * Cập nhật trạng thái loading của nút submit
 */
function setLoadingState(isLoading) {
    if (isLoading) {
        loginBtn.disabled = true;
        spinner.style.display = 'inline-block';
        btnText.textContent = 'Đang đăng nhập...';
    } else {
        loginBtn.disabled = false;
        spinner.style.display = 'none';
        btnText.textContent = 'Đăng Nhập';
    }
}

/**
 * Hiển thị thông báo lỗi
 */
function showErrorAlert(message) {
    errorMessage.textContent = message;
    errorAlert.style.display = 'flex';
    
    // Tự động ẩn sau 5 giây
    setTimeout(hideErrorAlert, 5000);
}

/**
 * Ẩn thông báo lỗi
 */
function hideErrorAlert() {
    errorAlert.style.display = 'none';
}

/**
 * Hiển thị thông báo thành công
 */
function showSuccessAlert(message) {
    successMessage.textContent = message;
    successAlert.style.display = 'flex';
}

/**
 * Ẩn thông báo thành công
 */
function hideSuccessAlert() {
    successAlert.style.display = 'none';
}

/**
 * Hiển thị lỗi username
 */
function showUsernameError(message) {
    usernameError.textContent = message;
    usernameError.classList.add('show');
    usernameInput.style.borderColor = '#e74c3c';
}

/**
 * Xóa lỗi username
 */
function clearUsernameError() {
    usernameError.classList.remove('show');
    usernameError.textContent = '';
    usernameInput.style.borderColor = '#e0e0e0';
}

/**
 * Hiển thị lỗi password
 */
function showPasswordError(message) {
    passwordError.textContent = message;
    passwordError.classList.add('show');
    passwordInput.style.borderColor = '#e74c3c';
}

/**
 * Xóa lỗi password
 */
function clearPasswordError() {
    passwordError.classList.remove('show');
    passwordError.textContent = '';
    passwordInput.style.borderColor = '#e0e0e0';
}

/**
 * Lưu thông tin đăng nhập đã lưu
 */
function loadSavedCredentials() {
    const rememberedUsername = localStorage.getItem('rememberedUsername');
    if (rememberedUsername) {
        usernameInput.value = rememberedUsername;
        rememberMeCheckbox.checked = true;
        // Focus vào password field
        passwordInput.focus();
    }
}

/**
 * Kiểm tra trạng thái xác thực
 * Nếu user đã đăng nhập rồi, chuyển hướng đến dashboard
 */
function checkAuthStatus() {
    const accessToken = sessionStorage.getItem('accessToken');
    if (accessToken) {
        // User đã đăng nhập, chuyển hướng đến dashboard
        // window.location.href = '/dashboard.html';
    }
}

// Hỗ trợ phím Enter để đăng nhập
passwordInput.addEventListener('keypress', function(event) {
    if (event.key === 'Enter') {
        loginForm.dispatchEvent(new Event('submit'));
    }
});

