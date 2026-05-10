package com.social.controller;

import com.social.dto.LoginDTO;
import com.social.dto.RegisterDTO;
import com.social.entity.User;
import com.social.service.UserService;
import com.social.utils.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO.getUsername(), registerDTO.getPassword());
            return Result.success("注册成功", user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (token != null) {
            return Result.success("登录成功", token);
        }
        return Result.error(401, "用户名或密码错误");
    }
}