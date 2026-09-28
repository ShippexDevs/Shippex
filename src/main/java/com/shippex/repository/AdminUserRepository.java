package com.shippex.repository;

import com.shippex.constants.AccountStatus;
import com.shippex.model.AdminUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.List;

public interface AdminUserRepository extends MongoRepository<AdminUser,String> {
    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<AdminUser> findByAccountStatus(AccountStatus accountStatus);
}
