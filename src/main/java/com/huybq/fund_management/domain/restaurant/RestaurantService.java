package com.huybq.fund_management.domain.restaurant;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantResponseDTO createRestaurant(RestaurantRequestDTO restaurantRequest) {
        if (restaurantRepository.existsByLink(restaurantRequest.getLink())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Link quán đã tồn tại");
        }

        Restaurant restaurant = RestaurantMapper.toEntity(restaurantRequest);
        restaurant = restaurantRepository.save(restaurant);
        return RestaurantMapper.toResponseDTO(restaurant);
    }

    public List<RestaurantResponseDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAllByIsBlacklistedFalse();
        return restaurants.stream()
                .map(RestaurantMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<RestaurantResponseDTO> getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantMapper::toResponseDTO);
    }

    public Optional<RestaurantResponseDTO> updateRestaurant(Long id, RestaurantRequestDTO restaurantRequest) {
        return restaurantRepository.findById(id)
                .map(existing -> {
                    existing.setName(restaurantRequest.getName());
                    existing.setLink(restaurantRequest.getLink());
                    Restaurant updated = restaurantRepository.save(existing);
                    return RestaurantMapper.toResponseDTO(updated);
                });
    }

    public boolean deleteRestaurant(Long id) {
        if (restaurantRepository.existsById(id)) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }
}