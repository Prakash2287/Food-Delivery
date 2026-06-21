package com.project.fooddelivery.service;

import com.project.fooddelivery.dto.MenuItemResponse;
import com.project.fooddelivery.dto.RestaurantResponse;
import com.project.fooddelivery.repository.MenuItemRepository;
import com.project.fooddelivery.repository.RestaurantRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final ApiMapper apiMapper;

    public CatalogService(RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository, ApiMapper apiMapper) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.apiMapper = apiMapper;
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> findRestaurantsByCity(String cityName) {
        return restaurantRepository.findByCity_NameIgnoreCase(cityName).stream()
            .map(apiMapper::toRestaurantResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findMenuByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurant_IdAndAvailableTrue(restaurantId).stream()
            .map(apiMapper::toMenuItemResponse)
            .toList();
    }
}
