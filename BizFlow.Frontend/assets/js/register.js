const REGISTER_API_BASE = resolveApiBase();

function resolveApiBase() {
  const configured = window.API_BASE_URL || window.API_BASE;
  if (configured) {
    return configured;
  }
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    return 'http://localhost:8000/api';
  }
  return `${window.location.origin}/api`;
}

async function postRegister(payload) {
  const primaryUrl = `${REGISTER_API_BASE}/auth/register`;
  let response;
  try {
    response = await fetch(primaryUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
  } catch (error) {
    throw new Error(`Không kết nối được tới API đăng ký: ${error.message}`);
  }
  if (response.status !== 404) {
    return response;
  }

  const fallbackUrl = 'http://localhost:8000/api/auth/register';
  if (primaryUrl === fallbackUrl) {
    return response;
  }

  return fetch(fallbackUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
}

function showRegisterMessage(id, message, visible) {
  const el = document.getElementById(id);
  if (!el) {
    return;
  }
  el.textContent = message;
  el.style.display = visible ? 'block' : 'none';
}

function togglePasswordField(buttonId, inputId) {
  const button = document.getElementById(buttonId);
  const input = document.getElementById(inputId);
  if (!button || !input) {
    return;
  }
  const eyeIcon = button.querySelector('.eye-icon');
  const eyeSlashIcon = button.querySelector('.eye-slash-icon');
  button.addEventListener('click', () => {
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    if (eyeIcon && eyeSlashIcon) {
      eyeIcon.style.display = isPassword ? 'none' : 'block';
      eyeSlashIcon.style.display = isPassword ? 'block' : 'none';
    }
  });
}

window.addEventListener('DOMContentLoaded', () => {
  const token = sessionStorage.getItem('accessToken');
  const role = sessionStorage.getItem('role');
  if (token && role) {
    switch (role) {
      case 'ADMIN':
        window.location.href = '/pages/admin-home.html';
        return;
      case 'OWNER':
        window.location.href = '/pages/owner-dashboard.html';
        return;
      case 'MANAGER':
        window.location.href = '/pages/dashboard.html';
        return;
      case 'EMPLOYEE':
        window.location.href = '/pages/employee-dashboard.html';
        return;
      default:
        break;
    }
  }

  togglePasswordField('togglePassword', 'password');
  togglePasswordField('toggleConfirmPassword', 'confirmPassword');

  const form = document.getElementById('registerForm');
  if (!form) {
    return;
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    showRegisterMessage('registerError', '', false);
    showRegisterMessage('registerSuccess', '', false);

    const data = Object.fromEntries(new FormData(form).entries());
    const password = (data.password || '').trim();
    const confirmPassword = (data.confirmPassword || '').trim();
    const username = (data.username || '').trim();
    const email = (data.email || '').trim();
    const fullName = (data.fullName || '').trim();
    const phoneNumber = (data.phoneNumber || '').trim();

    if (!username || !email || !fullName || !phoneNumber || !password) {
      showRegisterMessage('registerError', 'Vui lòng nhập đầy đủ thông tin bắt buộc (bao gồm số điện thoại).', true);
      return;
    }

    if (password.length < 6) {
      showRegisterMessage('registerError', 'Mật khẩu phải có ít nhất 6 ký tự.', true);
      return;
    }

    if (password !== confirmPassword) {
      showRegisterMessage('registerError', 'Mật khẩu xác nhận không khớp.', true);
      return;
    }

    try {
      const response = await postRegister({
        username,
        password,
        email,
        fullName,
        phoneNumber,
        role: 'EMPLOYEE'
      });

      const payload = await response.json().catch(() => ({}));
      if (!response.ok) {
        throw new Error(payload.error || 'Không thể tạo tài khoản.');
      }

      showRegisterMessage('registerSuccess', 'Tạo tài khoản thành công. Bạn có thể đăng nhập ngay bây giờ.', true);
      form.reset();
      setTimeout(() => {
        window.location.href = '/pages/login.html';
      }, 1400);
    } catch (error) {
      showRegisterMessage('registerError', error.message || 'Có lỗi xảy ra khi đăng ký.', true);
    }
  });
});