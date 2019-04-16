package com.example.feign.controller;

import com.example.feign.service.ApiService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

	@Resource
	private ApiService apiService;

	@RequestMapping("index")
	public String index() {
		return apiService.index();
	}
}