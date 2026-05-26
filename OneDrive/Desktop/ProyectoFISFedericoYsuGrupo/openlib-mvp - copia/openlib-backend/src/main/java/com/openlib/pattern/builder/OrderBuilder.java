package com.openlib.pattern.builder;

import com.openlib.domain.Order;
import com.openlib.domain.OrderItem;
import com.openlib.domain.OrderStatus;
import com.openlib.domain.PaymentMethod;
import com.openlib.domain.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderBuilder {

    private User buyer;
    private OrderStatus status = OrderStatus.PENDING;
    private PaymentMethod paymentMethod = PaymentMethod.FREE;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String billingFullName;
    private String billingEmail;
    private String billingAddress;
    private String billingCity;
    private String billingCountry;
    private String notes;
    private final List<OrderItem> items = new ArrayList<>();

    private OrderBuilder() {
    }

    public static OrderBuilder create() {
        return new OrderBuilder();
    }

    public OrderBuilder buyer(User buyer) {
        this.buyer = buyer;
        return this;
    }

    public OrderBuilder status(OrderStatus status) {
        if (status != null) {
            this.status = status;
        }
        return this;
    }

    public OrderBuilder paymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod != null) {
            this.paymentMethod = paymentMethod;
        }
        return this;
    }

    public OrderBuilder totalAmount(BigDecimal totalAmount) {
        if (totalAmount != null) {
            this.totalAmount = totalAmount;
        }
        return this;
    }

    public OrderBuilder billingFullName(String billingFullName) {
        this.billingFullName = billingFullName;
        return this;
    }

    public OrderBuilder billingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
        return this;
    }

    public OrderBuilder billingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
        return this;
    }

    public OrderBuilder billingCity(String billingCity) {
        this.billingCity = billingCity;
        return this;
    }

    public OrderBuilder billingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
        return this;
    }

    public OrderBuilder notes(String notes) {
        this.notes = notes;
        return this;
    }

    public OrderBuilder addItem(OrderItem item) {
        if (item != null) {
            this.items.add(item);
        }
        return this;
    }

    public Order build() {
        if (buyer == null) {
            throw new IllegalStateException("El comprador es obligatorio");
        }

        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(status);
        order.setPaymentMethod(paymentMethod);
        order.setTotalAmount(totalAmount);
        order.setBillingFullName(billingFullName);
        order.setBillingEmail(billingEmail);
        order.setBillingAddress(billingAddress);
        order.setBillingCity(billingCity);
        order.setBillingCountry(billingCountry);
        order.setNotes(notes);
        order.setItems(items);
        return order;
    }
}
