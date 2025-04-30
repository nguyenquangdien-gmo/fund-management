package com.huybq.fund_management.domain.order_feedback;

import com.huybq.fund_management.domain.order.Order;
import com.huybq.fund_management.domain.order.OrderRepository;
import com.huybq.fund_management.domain.order_item.OrderItem;
import com.huybq.fund_management.domain.order_item.OrderItemRepository;
import com.huybq.fund_management.domain.restaurant.Restaurant;
import com.huybq.fund_management.domain.restaurant.RestaurantRepository;
import com.huybq.fund_management.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderItemVoteService {

    private final OrderItemRepository orderItemRepository;
    private final OrderItemVoteRepository voteRepository;
    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;

    public OrderItemVoteResponseDTO createVote(User user, OrderItemVoteRequestDTO request) {
        // Tìm OrderItem theo ID từ request
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found"));

        // Lấy Order từ OrderItem
        Order order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        // Lấy Restaurant từ Order
        Restaurant restaurant = restaurantRepository.findById(order.getRestaurant().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        // Kiểm tra nếu đã có vote từ user đối với OrderItem này
        Optional<OrderItemVote> existingVote = voteRepository.findByUserAndOrderItem(user, orderItem);

        OrderItemVote vote;
        int previousRating = 0;
        boolean isNewVote = false;

        if (existingVote.isPresent()) {
            vote = existingVote.get();
            previousRating = vote.getRating();  // Lưu rating cũ
            vote.setRating(request.getRating());
            vote.setNote(request.getNote());
            vote = voteRepository.save(vote);
        } else {
            vote = OrderItemVoteMapper.toEntity(request, orderItem, user);
            vote = voteRepository.save(vote);
            isNewVote = true;
        }

        int ratingChange = vote.getRating() - previousRating;

        if (isNewVote) {
            updateRestaurantVotesAndStars(restaurant, ratingChange, true);
        } else {
            updateRestaurantVotesAndStars(restaurant, ratingChange, false);
        }

        return OrderItemVoteMapper.toResponseDTO(vote);
    }

    private void updateRestaurantVotesAndStars(Restaurant restaurant, int ratingChange, boolean isNewVote) {
        // Tính tổng số votes và stars mới
        int newTotalVotes = restaurant.getTotalVotes() + (isNewVote ? 1 : 0);
        int newTotalStars = restaurant.getTotalStars() + ratingChange;

        restaurant.setTotalVotes(newTotalVotes);
        restaurant.setTotalStars(newTotalStars);

        restaurantRepository.save(restaurant);
    }
}
