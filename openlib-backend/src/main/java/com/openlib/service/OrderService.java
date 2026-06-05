package com.openlib.service;

import com.openlib.domain.*;
import com.openlib.dto.request.OrderRequest;
import com.openlib.dto.response.*;
import com.openlib.exception.BusinessException;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.pattern.builder.OrderBuilder;
import com.openlib.pattern.builder.OrderItemBuilder;
import com.openlib.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository    orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository     userRepository;
    private final BookRepository     bookRepository;
    private final DownloadRepository downloadRepository;
    private final EmailService       emailService;

    // ── Checkout ──────────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse checkout(OrderRequest req, Long buyerId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(buyerId);
        if (cartItems.isEmpty()) {
            throw new BusinessException("El carrito está vacío");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", buyerId));

        OrderBuilder orderBuilder = OrderBuilder.create()
                .buyer(buyer)
                .status(OrderStatus.PENDING)
                .paymentMethod(req.paymentMethod())
                .billingFullName(req.billingFullName())
                .billingEmail(req.billingEmail())
                .billingAddress(req.billingAddress())
                .billingCity(req.billingCity())
                .billingCountry(req.billingCountry())
                .notes(req.notes());

        BigDecimal total = BigDecimal.ZERO;
        Order order = orderBuilder.build();

        for (CartItem ci : cartItems) {
            Book book = ci.getBook();

            boolean alreadyOwned = downloadRepository.existsByUserIdAndBookId(buyerId, book.getId());
            if (alreadyOwned) {
                throw new BusinessException("Ya tienes el libro: " + book.getTitle());
            }

            if (book.getStatus() != BookStatus.APPROVED) {
                throw new BusinessException("El libro '" + book.getTitle() + "' no está disponible");
            }

            OrderItem item = OrderItemBuilder.create()
                    .order(order)
                    .book(book)
                    .price(book.getPrice())
                    .build();

            order.getItems().add(item);
            total = total.add(book.getPrice());
        }

        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);

        // Los Downloads se crearán cuando el admin confirme la orden (cambie a COMPLETED)
        // Limpiar carrito
        cartItemRepository.deleteByUserId(buyerId);

        try {
            emailService.sendOrderConfirmation(saved);
        } catch (Exception ex) {
            // No bloquear el flujo de checkout si el correo falla
            System.err.println("Error enviando confirmación de pedido: " + ex.getMessage());
        }

        return toResponse(saved);
    }

    // ── Historial ─────────────────────────────────────────────────────────────

    public Page<OrderResponse> getHistory(Long buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByBuyerId(buyerId, pageable).map(this::toResponse);
    }

    public OrderResponse getById(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new ResourceNotFoundException("Orden", orderId);
        }
        return toResponse(order);
    }

    // ── Cambiar Estado de Órdenes ─────────────────────────────────────────────

    @Transactional
    public OrderResponse changeOrderStatus(Long orderId, Long buyerId, String newStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        
        // Validar que el buyer es el dueño
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new BusinessException("No tienes permisos para cambiar esta orden");
        }
        
        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            
            // Si cambia a COMPLETED: crear Downloads
            if (newStatus == OrderStatus.COMPLETED && order.getStatus() != OrderStatus.COMPLETED) {
                for (OrderItem item : order.getItems()) {
                    boolean alreadyExists = downloadRepository.existsByUserIdAndBookId(buyerId, item.getBook().getId());
                    if (!alreadyExists) {
                        Download dl = Download.builder()
                                .user(order.getBuyer())
                                .book(item.getBook())
                                .orderItem(item)
                                .build();
                        downloadRepository.save(dl);
                    }
                }
            }
            
            // Si cambia a CANCELLED: eliminar Downloads de esta orden
            if (newStatus == OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    downloadRepository.deleteByUserIdAndBookId(buyerId, item.getBook().getId());
                }
            }
            
            order.setStatus(newStatus);
            orderRepository.save(order);
            return toResponse(order);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de orden inválido: " + newStatusStr);
        }
    }

    // ── Admin: Obtener todas las órdenes ──────────────────────────────────────

    public Page<OrderResponse> getAllOrdersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse adminChangeOrderStatus(Long orderId, String newStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", orderId));
        
        try {
            OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
            Long buyerId = order.getBuyer().getId();
            
            // Si cambia a COMPLETED: crear Downloads
            if (newStatus == OrderStatus.COMPLETED && order.getStatus() != OrderStatus.COMPLETED) {
                for (OrderItem item : order.getItems()) {
                    boolean alreadyExists = downloadRepository.existsByUserIdAndBookId(buyerId, item.getBook().getId());
                    if (!alreadyExists) {
                        Download dl = Download.builder()
                                .user(order.getBuyer())
                                .book(item.getBook())
                                .orderItem(item)
                                .build();
                        downloadRepository.save(dl);
                    }
                }
            }
            
            // Si cambia a CANCELLED: eliminar Downloads de esta orden
            if (newStatus == OrderStatus.CANCELLED) {
                for (OrderItem item : order.getItems()) {
                    downloadRepository.deleteByUserIdAndBookId(buyerId, item.getBook().getId());
                }
            }
            
            order.setStatus(newStatus);
            orderRepository.save(order);
            return toResponse(order);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado de orden inválido: " + newStatusStr);
        }
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(), i.getBook().getId(),
                        i.getBook().getTitle(), i.getBook().getCoverUrl(),
                        i.getPrice()))
                .toList();

        return new OrderResponse(
            o.getId(), o.getStatus(), o.getPaymentMethod(), o.getTotalAmount(),
            o.getBillingFullName(), o.getBillingEmail(), o.getBillingAddress(),
            o.getBillingCity(), o.getBillingCountry(), items, o.getCreatedAt()
        );
    }
}
