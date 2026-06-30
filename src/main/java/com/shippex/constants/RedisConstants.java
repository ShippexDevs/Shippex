package com.shippex.constants;

import java.time.Duration;

public final class RedisConstants {

    private RedisConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Redis Key Prefixes
     */
    public static final String OTP_PREFIX = "OTP:";
    public static final String OTP_GENERATE_RETRY_PREFIX = "OTP_GENERATE_RETRY:";
    public static final String OTP_VERIFY_RETRY_PREFIX = "OTP_VERIFY_RETRY:";
    public static final String OTP_VERIFIED_PREFIX = "OTP_VERIFIED:";

    /**
     * OTP Configuration
     */
    public static final int OTP_LENGTH = 6;

    /**
     * Maximum OTP generation requests allowed.
     */
    public static final int MAX_OTP_GENERATION_RETRIES = 5;

    /**
     * Maximum invalid OTP verification attempts allowed.
     */
    public static final int MAX_OTP_VERIFICATION_RETRIES = 5;

    /**
     * OTP validity.
     */
    public static final Duration OTP_TTL = Duration.ofMinutes(15);

    /**
     * Verified flag validity.
     */
    public static final Duration OTP_VERIFIED_TTL = Duration.ofHours(1);

    /**
     * Redis Key Builders
     */
    public static String getOtpKey(String phoneNumber) {
        return OTP_PREFIX + phoneNumber;
    }

    public static String getGenerateRetryKey(String phoneNumber) {
        return OTP_GENERATE_RETRY_PREFIX + phoneNumber;
    }

    public static String getVerifyRetryKey(String phoneNumber) {
        return OTP_VERIFY_RETRY_PREFIX + phoneNumber;
    }

    public static String getVerifiedKey(String phoneNumber) {
        return OTP_VERIFIED_PREFIX + phoneNumber;
    }
}