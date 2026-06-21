package com.project.fooddelivery.controller;

import com.project.fooddelivery.dto.CityResponse;
import com.project.fooddelivery.dto.CreateCityRequest;
import com.project.fooddelivery.dto.CreateDeliveryPartnerRequest;
import com.project.fooddelivery.dto.CreateRestaurantRequest;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/cities")
    @ResponseStatus(HttpStatus.CREATED)
    public CityResponse createCity(@Valid @RequestBody CreateCityRequest request) {
        return adminService.createCity(request);
    }

    @PostMapping("/restaurants")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return adminService.createRestaurant(request);
    }

    @PostMapping("/partners")
    @ResponseStatus(HttpStatus.CREATED)
    public String createPartner(@Valid @RequestBody CreateDeliveryPartnerRequest request) {
        return adminService.createDeliveryPartner(request);
    }
}
