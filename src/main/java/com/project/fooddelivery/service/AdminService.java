package com.project.fooddelivery.service;

import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.DeliveryPartner;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.dto.CityResponse;
import com.project.fooddelivery.dto.CreateCityRequest;
import com.project.fooddelivery.dto.CreateDeliveryPartnerRequest;
import com.project.fooddelivery.dto.CreateRestaurantRequest;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.exception.BusinessException;
import com.project.fooddelivery.exception.NotFoundException;
import com.project.fooddelivery.repository.CityRepository;
import com.project.fooddelivery.repository.DeliveryPartnerRepository;
import com.project.fooddelivery.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final CityRepository cityRepository;
    private final RestaurantRepository restaurantRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final ApiMapper apiMapper;

    public AdminService(
        CityRepository cityRepository,
        RestaurantRepository restaurantRepository,
        DeliveryPartnerRepository deliveryPartnerRepository,
        ApiMapper apiMapper
    ) {
        this.cityRepository = cityRepository;
        this.restaurantRepository = restaurantRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.apiMapper = apiMapper;
    }

    @Transactional
    public CityResponse createCity(CreateCityRequest request) {
        cityRepository.findByNameIgnoreCase(request.name()).ifPresent(city -> {
            throw new BusinessException("City already exists");
        });
        return apiMapper.toCityResponse(cityRepository.save(new City(request.name())));
    }

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        City city = cityRepository.findById(request.cityId())
            .orElseThrow(() -> new NotFoundException("City not found"));
        Restaurant restaurant = restaurantRepository.save(
            new Restaurant(request.name(), request.ownerUsername(), city)
        );
        return apiMapper.toRestaurantResponse(restaurant);
    }

    @Transactional
    public String createDeliveryPartner(CreateDeliveryPartnerRequest request) {
        deliveryPartnerRepository.findByUsername(request.username()).ifPresent(partner -> {
            throw new BusinessException("Delivery partner username already exists");
        });
        deliveryPartnerRepository.save(
            new DeliveryPartner(request.username(), request.fullName(), request.cityName())
        );
        return "Delivery partner created";
    }
}
