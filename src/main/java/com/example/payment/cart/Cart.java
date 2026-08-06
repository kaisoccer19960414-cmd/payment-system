package com.example.payment.cart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Cart implements Serializable {

    private final List<CartItem> items = new ArrayList<>();

    public void add(CartItem item) {
        items.add(item);
    }

    public void remove(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalPrice() {
        return items.stream()
                .mapToInt(i -> i.getPrice() * i.getQuantity())
                .sum();
    }

    public void clear() {
        items.clear();
    }
}