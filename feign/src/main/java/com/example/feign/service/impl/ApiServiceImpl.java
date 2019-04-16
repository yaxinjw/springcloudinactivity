package com.example.feign.service.impl;

import com.example.feign.service.ApiService;
import org.springframework.stereotype.Component;

@Component
public class ApiServiceImpl implements ApiService {

    @Override
    public String index() {
        return "服务发生故障！";
    }
}