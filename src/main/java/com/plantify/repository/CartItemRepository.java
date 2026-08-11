package com.plantify.repository;

import com.plantify.entity.CartItem;
import com.plantify.entity.Product;
import com.plantify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
    void deleteByProduct(Product product);
    Integer countByUser(User user);
}
