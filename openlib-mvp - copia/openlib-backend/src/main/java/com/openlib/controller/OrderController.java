package com.openlib.controller;

import com.openlib.domain.User;
import com.openlib.dto.request.OrderRequest;
import com.openlib.dto.response.OrderResponse;
import com.openlib.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes / Checkout")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear orden desde el carrito (checkout)")
    public OrderResponse checkout(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody OrderRequest req) {
        return orderService.checkout(req, user.getId());
    }

    @GetMapping
    @Operation(summary = "Historial de órdenes del usuario")
    public Page<OrderResponse> history(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getHistory(user.getId(), page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una orden")
    public OrderResponse getById(@AuthenticationPrincipal User user,
                                 @PathVariable Long id) {
        return orderService.getById(id, user.getId());
    }
}
