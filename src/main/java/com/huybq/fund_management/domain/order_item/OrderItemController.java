package com.huybq.fund_management.domain.order_item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.huybq.fund_management.domain.user.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${server.version}/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponseDTO> createItem(
            @RequestBody OrderItemRequestDTO orderItem,
            @AuthenticationPrincipal User user
    ) {
        OrderItemResponseDTO response = orderItemService.createItem(user.getId(), orderItem);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<OrderItemResponseDTO> getItemById(@PathVariable Long itemId) {
        OrderItemResponseDTO item = orderItemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @PutMapping("{itemId}")
    public ResponseEntity<OrderItemResponseDTO> updateItem(
            @PathVariable Long itemId,
            @RequestBody OrderItemRequestDTO orderItem,
            @AuthenticationPrincipal User user
    ) {
        OrderItemResponseDTO response = orderItemService.updateItem(itemId, user.getId(), orderItem);
        return ResponseEntity.ok(response);
    }

}

