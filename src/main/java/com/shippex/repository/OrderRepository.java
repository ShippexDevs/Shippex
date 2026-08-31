package com.shippex.repository;

import com.shippex.model.Order;
import com.shippex.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(LocalDateTime createdAt);
    List<Order> findByUpdatedAtGreaterThanEqual(LocalDateTime updatedAt);
    @Query("{ 'createdAt': { '$gte': ?0, '$lt': ?1 } }")
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime until);

    @Query(value = "{ 'createdAt': { '$gte': ?0, '$lt': ?1 } }", count = true)
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime until);
    List<Order> findByStatusNot(OrderStatus status);
}
