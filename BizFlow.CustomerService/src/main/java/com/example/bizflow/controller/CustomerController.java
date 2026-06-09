package com.example.bizflow.controller;

import com.example.bizflow.entity.Customer;
import com.example.bizflow.repository.CustomerRepository;
import com.example.bizflow.integration.SalesClient;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final SalesClient salesClient;
    @Value("${app.jwt.secret:my-secret-key-for-jwt-token-generation-and-verification}")
    private String jwtSecret;

    public CustomerController(CustomerRepository customerRepository,
            SalesClient salesClient) {
        this.customerRepository = customerRepository;
        this.salesClient = salesClient;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<Customer> customers = customerRepository.findAll();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching customers: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> getCustomerById(@PathVariable @NonNull Long id) {
        try {
            return customerRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching customer: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> createCustomer(@RequestBody @NonNull CustomerCreateRequest request) {
        try {
            String name = trimToNull(request.name);
            String phone = trimToNull(request.phone);
            if (name == null) {
                return ResponseEntity.badRequest().body("Name is required.");
            }
            if (phone == null) {
                return ResponseEntity.badRequest().body("Phone is required.");
            }

            String address = trimToNull(request.address);
            if (address != null && !validateAddress(address)) {
                return ResponseEntity.badRequest().body("Invalid address format. Address must include house number, street, ward/district, and city/province.");
            }

            Customer customer = new Customer(name, phone);
            customer.setEmail(trimToNull(request.email));
            customer.setAddress(normalizeAddressForStorage(address));
            customer.setCccd(trimToNull(request.cccd));
            customer.setGender(trimToNull(request.gender));
            if (trimToNull(request.dob) != null) {
                customer.setDob(LocalDate.parse(request.dob.trim()));
            }

            Customer saved = customerRepository.save(customer);
            return ResponseEntity.ok(saved);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Invalid dob format. Use yyyy-MM-dd.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating customer: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<Object> getCustomerOrderHistory(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(salesClient.getCustomerOrderHistory(id));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCustomerByUserId(
            @PathVariable @NonNull Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            HttpServletRequest request) {
        try {
            // If the caller is a CUSTOMER role, allow only when the requested userId matches the JWT's userId.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isCustomer = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CUSTOMER".equalsIgnoreCase(a.getAuthority()));
            if (isCustomer) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
                }
                String token = authHeader.substring(7);
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    Number tokenUserIdNum = claims.get("userId", Number.class);
                    Long tokenUserId = tokenUserIdNum == null ? null : tokenUserIdNum.longValue();
                    if (tokenUserId == null || !tokenUserId.equals(userId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
                    }
                } catch (Exception ex) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden");
                }
            }

            var customerOpt = customerRepository.findByUserId(userId);
            if (customerOpt.isEmpty()) {
                String fallbackUsername = null;
                Claims tokenClaims = null;
                if (username != null && !username.isBlank()) {
                    fallbackUsername = username.trim();
                } else {
                    String authHeader = request.getHeader("Authorization");
                    String token = null;
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                    if (token != null) {
                        try {
                            tokenClaims = Jwts.parserBuilder()
                                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                                    .build()
                                    .parseClaimsJws(token)
                                    .getBody();
                            fallbackUsername = tokenClaims.getSubject();
                        } catch (Exception ignore) {
                        }
                    }
                }
                if (fallbackUsername != null && !fallbackUsername.isBlank()) {
                    String fallback = fallbackUsername.trim();
                    if (fallback.contains("@")) {
                        customerOpt = customerRepository.findByEmailIgnoreCase(fallback);
                    }
                    if (customerOpt.isEmpty()) {
                        customerOpt = customerRepository.findByUsernameIgnoreCase(fallback);
                    }
                }
                if (customerOpt.isEmpty() && request.getParameter("phone") != null && !phone.isBlank()) {
                    String normalizedPhone = normalizePhone(phone.trim());
                    if (normalizedPhone != null && !normalizedPhone.isBlank()) {
                        customerOpt = customerRepository.findByPhone(normalizedPhone);
                    }
                }
                if (customerOpt.isEmpty() && tokenClaims != null) {
                    String emailClaim = tokenClaims.get("email", String.class);
                    if (emailClaim != null && !emailClaim.isBlank()) {
                        customerOpt = customerRepository.findByEmailIgnoreCase(emailClaim.trim());
                    }
                }
                if (customerOpt.isEmpty() && tokenClaims != null) {
                    String phoneClaim = tokenClaims.get("phoneNumber", String.class);
                    if (phoneClaim != null && !phoneClaim.isBlank()) {
                        String normalizedPhone = phoneClaim.replaceAll("\\D+", "");
                        customerOpt = customerRepository.findByPhone(normalizedPhone);
                    }
                }
            }
            return customerOpt.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching customer by userId: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER', 'ADMIN')")
    public ResponseEntity<?> updateCustomer(@PathVariable @NonNull Long id, @RequestBody @NonNull CustomerUpdateRequest request) {
        try {
            return customerRepository.findById(id)
                    .map(customer -> {
                        // Update basic info
                        if (request.name != null && !request.name.trim().isEmpty()) {
                            customer.setName(request.name.trim());
                        }
                        if (request.phone != null && !request.phone.trim().isEmpty()) {
                            customer.setPhone(request.phone.trim());
                        }
                        
                        // Update optional fields
                        String newAddress = trimToNull(request.address);
                        if (newAddress != null && !validateAddress(newAddress)) {
                            return ResponseEntity.badRequest().body("Invalid address format. Address must include house number, street, ward/district, and city/province.");
                        }
                        customer.setEmail(trimToNull(request.email));
                        customer.setAddress(normalizeAddressForStorage(newAddress));
                        customer.setCccd(trimToNull(request.cccd));
                        customer.setGender(trimToNull(request.gender));
                        
                        // Update date of birth
                        if (trimToNull(request.dob) != null) {
                            try {
                                customer.setDob(LocalDate.parse(request.dob.trim()));
                            } catch (Exception e) {
                                // Keep existing date if parse fails
                            }
                        }
                        
                        Customer updated = customerRepository.save(customer);
                        return ResponseEntity.ok(updated);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating customer: " + e.getMessage());
        }
    }

    @PostMapping("/upsertByUser")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upsertCustomerByUser(@RequestBody UpsertByUserRequest request) {
        if (request == null || request.userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }

        java.util.Optional<Customer> found = customerRepository.findByUserId(request.userId);
        if (found.isEmpty() && request.phone != null && !request.phone.isBlank()) {
            String normalizedPhone = normalizePhone(request.phone.trim());
            if (normalizedPhone != null && !normalizedPhone.isBlank()) {
                found = customerRepository.findByPhone(normalizedPhone);
            }
        }
        if (found.isEmpty() && request.username != null && !request.username.isBlank()) {
            found = customerRepository.findByUsernameIgnoreCase(request.username.trim());
        }
        if (found.isEmpty() && request.email != null && !request.email.isBlank()) {
            found = customerRepository.findByEmailIgnoreCase(request.email.trim());
        }
        if (found.isEmpty() && request.name != null && !request.name.isBlank()) {
            found = customerRepository.findByNameIgnoreCase(request.name.trim());
        }

        return found
                .map(existing -> {
                    boolean changed = false;
                    if (request.name != null && !request.name.isBlank()) {
                        existing.setName(request.name.trim());
                        changed = true;
                    }
                    if (request.email != null) {
                        existing.setEmail(trimToNull(request.email));
                        changed = true;
                    }
                    if (request.address != null) {
                        String newAddress = trimToNull(request.address);
                        if (newAddress != null && !validateAddress(newAddress)) {
                            return ResponseEntity.badRequest().body("Invalid address format. Address must include house number, street, ward/district, and city/province.");
                        }
                        existing.setAddress(normalizeAddressForStorage(newAddress));
                        changed = true;
                    }
                    if (request.phone != null && !request.phone.isBlank()) {
                        String normalizedPhone = normalizePhone(request.phone.trim());
                        if (normalizedPhone != null && !normalizedPhone.isBlank()
                                && (existing.getPhone() == null || !existing.getPhone().equals(normalizedPhone))) {
                            existing.setPhone(normalizedPhone);
                            changed = true;
                        }
                    }
                    if (existing.getUserId() == null || !existing.getUserId().equals(request.userId)) {
                        existing.setUserId(request.userId);
                        changed = true;
                    }
                    if (request.username != null && !request.username.isBlank()
                            && (existing.getUsername() == null || !existing.getUsername().equals(request.username.trim()))) {
                        existing.setUsername(request.username.trim());
                        changed = true;
                    }
                    if (changed) customerRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> {
                    String name = request.name != null && !request.name.isBlank() ? request.name.trim() : "Khách hàng";
                    String phone = null;
                    if (request.phone != null && !request.phone.isBlank()) {
                        phone = normalizePhone(request.phone.trim());
                    }
                    Customer created = new Customer(name, phone);
                    created.setUserId(request.userId);
                    created.setUsername(trimToNull(request.username));
                    created.setEmail(trimToNull(request.email));
                    String newAddress = trimToNull(request.address);
                    if (newAddress != null && !validateAddress(newAddress)) {
                        return ResponseEntity.badRequest().body("Invalid address format. Address must include house number, street, ward/district, and city/province.");
                    }
                    created.setAddress(normalizeAddressForStorage(newAddress));
                    Customer saved = customerRepository.save(created);
                    return ResponseEntity.ok(saved);
                });
    }

    private static class CustomerCreateRequest {

        public String name;
        public String phone;
        public String email;
        public String address;
        public String cccd;
        public String dob;
        public String gender;
    }

    private static class CustomerUpdateRequest {

        public String name;
        public String phone;
        public String email;
        public String address;
        public String cccd;
        public String dob;
        public String gender;
    }

    private static class UpsertByUserRequest {
        public Long userId;
        public String username;
        public String name;
        public String email;
        public String address;
        public String phone;
    }

    private static String trimToNull(String str) {
        if (str == null) return null;
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
    }

    private boolean validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return true; // Allow empty address
        }
        String normalizedAddress = normalizeAddressForStorage(address).toLowerCase();
        if (!normalizedAddress.matches(".*\\d+.*")) {
            return false;
        }
        if (!normalizedAddress.contains(",")) {
            return false;
        }
        if (!(normalizedAddress.contains("hồ chí minh") || normalizedAddress.contains("ho chi minh") || normalizedAddress.contains("tp.hcm") || normalizedAddress.contains("hcm"))) {
            return false;
        }
        if (normalizedAddress.contains("thuận an") && normalizedAddress.contains("hồ chí minh")) {
            return false;
        }
        return true;
    }

    private String normalizeAddressForStorage(String address) {
        if (address == null) {
            return null;
        }
        String normalized = address.trim().replaceAll("\\s*,\\s*", ", ");
        if (normalized.matches("(?i).*(Thuận An).*") && normalized.matches("(?i).*(Hồ Chí Minh|Ho Chi Minh|TP.HCM|HCM).*")) {
            normalized = normalized.replaceAll("(?i),?\\s*Thuận An"," ");
        }
        String lowerNormalized = normalized.toLowerCase();
        if (lowerNormalized.contains("khu phố 23") && lowerNormalized.contains("tân thới nhất")) {
            normalized = normalized.replaceAll("(?i),?\\s*Khu phố 23"," ");
        }
        normalized = normalized.replaceAll("\\s*,\\s*", ", ").replaceAll(",{2,}", ",").replaceAll("\\s+", " ").trim();
        normalized = normalized.replaceAll(", $", "");
        return normalized;
    }


}
