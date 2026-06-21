package com.project.fooddelivery.config;

import com.project.fooddelivery.domain.City;
import com.project.fooddelivery.domain.DeliveryPartner;
import com.project.fooddelivery.domain.MenuItem;
import com.project.fooddelivery.domain.Restaurant;
import com.project.fooddelivery.repository.CityRepository;
import com.project.fooddelivery.repository.DeliveryPartnerRepository;
import com.project.fooddelivery.repository.MenuItemRepository;
import com.project.fooddelivery.repository.RestaurantRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(
        CityRepository cityRepository,
        RestaurantRepository restaurantRepository,
        MenuItemRepository menuItemRepository,
        DeliveryPartnerRepository deliveryPartnerRepository
    ) {
        return args -> {
            if (cityRepository.count() > 0) {
                return;
            }

            City delhi = cityRepository.save(new City("Delhi"));
            City mumbai = cityRepository.save(new City("Mumbai"));

            Restaurant spiceHub = restaurantRepository.save(new Restaurant("Spice Hub", "owner1", delhi));
            Restaurant pizzaTown = restaurantRepository.save(new Restaurant("Pizza Town", "owner2", mumbai));

            menuItemRepository.save(new MenuItem("Paneer Roll", new BigDecimal("120.00"), 10, spiceHub));
            menuItemRepository.save(new MenuItem("Veg Biryani", new BigDecimal("220.00"), 8, spiceHub));
            menuItemRepository.save(new MenuItem("Margherita Pizza", new BigDecimal("300.00"), 12, pizzaTown));
            menuItemRepository.save(new MenuItem("Garlic Bread", new BigDecimal("140.00"), 15, pizzaTown));

            deliveryPartnerRepository.save(new DeliveryPartner("partner1", "Rahul Rider", "Delhi"));
            deliveryPartnerRepository.save(new DeliveryPartner("partner2", "Neha Rider", "Mumbai"));
        };
    }
}
