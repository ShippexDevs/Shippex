package com.shippex.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("otp")
public class Otp {

    @Id
    private String username;

    /**
     * BCrypt hash of the OTP
     */
    private String otpHash;

    /**
     * Number of failed verification attempts
     */
    private int failedAttempts;

    /**
     * TTL in seconds
     */
    @TimeToLive
    private Long ttl;

}