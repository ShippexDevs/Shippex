package com.shippex.repository;

import com.shippex.model.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

public interface AppUserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    @Query(value = "{ 'createdAt': { '$gte': ?0, '$lt': ?1 } }", count = true)
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime until);
    List<AppUser> findByCreatedAtGreaterThanEqual(LocalDateTime createdAt);
    List<AppUser> findByLastUpdatedAtGreaterThanEqual(LocalDateTime lastUpdatedAt);
}
