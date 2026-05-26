package com.openlib.pattern.builder;

import com.openlib.domain.Book;
import com.openlib.domain.Order;
import com.openlib.domain.OrderItem;

import java.math.BigDecimal;

public class OrderItemBuilder {

    private Order order;
    private Book book;
    private BigDecimal price;

    private OrderItemBuilder() {
    }

    public static OrderItemBuilder create() {
        return new OrderItemBuilder();
    }

    public OrderItemBuilder order(Order order) {
        this.order = order;
        return this;
    }

    public OrderItemBuilder book(Book book) {
        this.book = book;
        return this;
    }

    public OrderItemBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public OrderItem build() {
        if (order == null) {
            throw new IllegalStateException("La orden es obligatoria");
        }
        if (book == null) {
            throw new IllegalStateException("El libro es obligatorio");
        }
        if (price == null) {
            throw new IllegalStateException("El precio del item es obligatorio");
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setBook(book);
        item.setPrice(price);
        return item;
    }
}
