package com.shippex.bootstarp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bootstrap.super-admin")
public class SuperAdminProperties {
    private String name;
    private String username;
    private String email;
    private String password;
}
