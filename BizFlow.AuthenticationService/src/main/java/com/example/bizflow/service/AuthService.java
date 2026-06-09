package com.example.bizflow.service;

import com.example.bizflow.dto.LoginRequest;
import com.example.bizflow.dto.LoginResponse;
import com.example.bizflow.entity.User;
import com.example.bizflow.repository.UserRepository;
import com.example.bizflow.util.JwtUtil;
import com.example.bizflow.util.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Xác thực người dùng và tạo tokens
     * 
     * @param request LoginRequest chứa username và password
     * @return LoginResponse chứa access token, refresh token và thông tin user
     * @throws RuntimeException nếu username không tồn tại hoặc password sai
     */
    public LoginResponse authenticate(LoginRequest request) {
        // Hỗ trợ đăng nhập bằng username, email hoặc số điện thoại
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .or(() -> userRepository.findByPhoneNumber(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác"));

        // Kiểm tra tài khoản có bị vô hiệu hóa
        if (!user.getEnabled()) {
            throw new RuntimeException("Tài khoản của bạn đã bị vô hiệu hóa");
        }

        // Kiểm tra password
        String storedHash = user.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            storedHash = user.getPassword();
        }

        boolean match = passwordEncoder.matches(request.getPassword(), storedHash);
        if (!match) {
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        // Tạo tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Trả về response
        return new LoginResponse(accessToken, refreshToken, user.getRole().name(), user.getId(), user.getUsername());
    }

    /**
     * Xác thực token JWT
     * 
     * @param token JWT token
     * @return true nếu token hợp lệ, false nếu không
     */
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    /**
     * Lấy username từ token
     * 
     * @param token JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }

    /**
     * Làm mới access token bằng refresh token.
     * Kiểm tra refresh token hợp lệ, user còn active, rồi cấp access token mới.
     *
     * @param refreshToken refresh token từ client
     * @return LoginResponse chứa access token mới + refresh token cũ
     * @throws RuntimeException nếu refresh token không hợp lệ hoặc user bị vô hiệu hóa
     */
    public LoginResponse refreshAccessToken(String refreshToken) {
        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Lấy username từ refresh token
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        if (username == null) {
            throw new RuntimeException("Refresh token không hợp lệ");
        }

        // Tìm user trong DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Kiểm tra tài khoản còn active
        if (!user.getEnabled()) {
            throw new RuntimeException("Tài khoản của bạn đã bị vô hiệu hóa");
        }

        // Tạo access token mới (giữ nguyên refresh token cũ)
        String newAccessToken = jwtUtil.generateAccessToken(user);

        return new LoginResponse(newAccessToken, refreshToken, user.getRole().name(), user.getId(), user.getUsername());
    }
}
