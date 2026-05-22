package com.example.personalfinance.controller;

import com.example.personalfinance.common.ApiResponse;
import com.example.personalfinance.dto.LoginDTO;
import com.example.personalfinance.dto.RegisterDTO;
import com.example.personalfinance.service.IAppUserService;
import com.example.personalfinance.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAppUserService appUserService;

    public AuthController(IAppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterDTO request) {
        LoginVO result = appUserService.register(request);
        return ApiResponse.success("register successfully", result);
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO request) {
        LoginVO result = appUserService.login(request);
        return ApiResponse.success("login successfully", result);
    }
}
