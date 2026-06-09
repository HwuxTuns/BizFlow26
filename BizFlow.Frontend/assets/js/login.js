// Login page JS
const API_BASE = resolveApiBase();
const ALLOWED_ROLES = ['ADMIN','OWNER','EMPLOYEE', 'MANAGER'];

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

async function postLogin(username, password) {
  const primaryUrl = `${API_BASE}/auth/login`;
  let response;
  try {
    response = await fetch(primaryUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
  } catch (error) {
    throw new Error(`Không kết nối được tới API đăng nhập: ${error.message}`);
  }

  if (response.status !== 404) {
    return response;
  }

  const fallbackUrl = 'http://localhost:8000/api/auth/login';
  if (primaryUrl === fallbackUrl) {
    return response;
  }

  return fetch(fallbackUrl, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
}

function showLoginError(message){
  const el=document.getElementById('loginError');
  if(el){el.textContent=message;el.style.display='block';}
}

function redirectByRole(role){
  switch(role){
    case 'ADMIN': window.location.href='/pages/admin-home.html'; break;
    case 'OWNER': window.location.href='/pages/owner-dashboard.html'; break;
    case 'MANAGER': window.location.href='/pages/dashboard.html'; break;
    case 'EMPLOYEE': window.location.href='/pages/employee-dashboard.html'; break;
    default: showLoginError('Bạn không có quyền truy cập ứng dụng này. Vui lòng kiểm tra lại.');
  }
}

window.addEventListener('DOMContentLoaded',()=>{
  const token=sessionStorage.getItem('accessToken');
  const role=sessionStorage.getItem('role');
  const username=sessionStorage.getItem('username');
  if(token && role && ALLOWED_ROLES.includes(role)){
    redirectByRole(role);
  }

  // Toggle password visibility
  const togglePasswordBtn = document.getElementById('togglePassword');
  const passwordInput = document.getElementById('password');
  const eyeIcon = document.querySelector('.eye-icon');
  const eyeSlashIcon = document.querySelector('.eye-slash-icon');
  
  if(togglePasswordBtn && passwordInput) {
    togglePasswordBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const isPassword = passwordInput.type === 'password';
      passwordInput.type = isPassword ? 'text' : 'password';
      eyeIcon.style.display = isPassword ? 'none' : 'block';
      eyeSlashIcon.style.display = isPassword ? 'block' : 'none';
    });
  }

  const form=document.getElementById('loginForm');
  if(form){
    form.addEventListener('submit',async(e)=>{
      e.preventDefault();
      const username=document.getElementById('username').value.trim();
      const password=document.getElementById('password').value.trim();
      if(!username||!password){showLoginError('Vui lòng nhập đầy đủ thông tin');return;}
      try{
        const res = await postLogin(username, password);
        if(res.ok){
          const data=await res.json();
          sessionStorage.setItem('accessToken',data.accessToken);
          sessionStorage.setItem('refreshToken',data.refreshToken);
          sessionStorage.setItem('username',data.username);
          sessionStorage.setItem('role',data.role);
          sessionStorage.setItem('userId',data.userId);
          if(ALLOWED_ROLES.includes(data.role)){
            redirectByRole(data.role);
          }else{
            showLoginError('Bạn không có quyền truy cập ứng dụng này. Vui lòng kiểm tra lại.');
          }
        }else{
          // Phân biệt lỗi kết nối (gateway/backend) và lỗi sai thông tin
          if(res.status >= 500 || res.status === 0){
            showLoginError('Lỗi kết nối máy chủ .Vui lòng thử lại sau.');
          }else{
            showLoginError('Tên đăng nhập, email, SĐT hoặc mật khẩu không đúng');
          }
        }
      }catch(err){
        showLoginError('Lỗi kết nối: '+err.message);
      }
    });
  }
});
