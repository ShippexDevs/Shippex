package com.shippex.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "admin_user")
public class AdminUser extends BaseUser{
    public AdminUser(String name, String username, String password) {
        super(name, username, password);
    }
}
