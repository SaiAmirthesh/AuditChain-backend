package com.SaiAmirthesh.AuditChain.controller;

import com.SaiAmirthesh.AuditChain.JwtUtil;
import com.SaiAmirthesh.AuditChain.entity.User;
import com.SaiAmirthesh.AuditChain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final com.SaiAmirthesh.AuditChain.repository.AccountRepository accountRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          com.SaiAmirthesh.AuditChain.repository.AccountRepository accountRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody com.SaiAmirthesh.AuditChain.dto.RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()) != null) {
            return "user already exists";
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // bcrypt
        user.setRole(request.getRole());

        userRepository.save(user);

        if ("user".equalsIgnoreCase(request.getRole())) {
            com.SaiAmirthesh.AuditChain.entity.Account account = new com.SaiAmirthesh.AuditChain.entity.Account();
            account.setAccountNumber(request.getUsername()); 
            account.setHolderName(request.getUsername());
            account.setBalance(10000.00); 
            account.setUpdatedBy("SYSTEM_REGISTRATION");
            accountRepository.save(account);
        }

        return "user registered successfully";
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody com.SaiAmirthesh.AuditChain.dto.LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername());
        Map<String, String> response = new HashMap<>();

        if (user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

            response.put("token", token);
            response.put("role", user.getRole());
            response.put("username", user.getUsername());

            return response;
        }

        response.put("error", "invalid credentials");
        return response;
    }

    @GetMapping("/encode")
    public String encode(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}