package com.shippex.service.impl;


import com.shippex.constants.RedisConstants;
import com.shippex.dto.notification.NotificationRequest;
import com.shippex.dto.otp.GenerateOtpRequest;
import com.shippex.dto.otp.VerifyOtpRequest;
import com.shippex.exception.OtpException;
import com.shippex.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisService redisService;
    private final WhatsAppService whatsAppService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates an OTP, stores it in Redis and sends it via WhatsApp.
     */
    public void generateOtp(GenerateOtpRequest request) {

        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        String otpKey = RedisConstants.getOtpKey(phoneNumber);
        String generateRetryKey = RedisConstants.getGenerateRetryKey(phoneNumber);
        String verifiedKey = RedisConstants.getVerifiedKey(phoneNumber);

        try {

            Integer retryCount = redisService.get(generateRetryKey, Integer.class);

            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount >= RedisConstants.MAX_OTP_GENERATION_RETRIES) {

                long ttl = redisService.getRemainingTTL(generateRetryKey);

                log.warn(
                        "OTP generation limit exceeded for phoneNumber={}, remainingTTL={} seconds",
                        phoneNumber,
                        ttl
                );

                throw new OtpException(
                        "Maximum OTP requests reached. Please try again after "
                                + ttl + " seconds."
                );
            }

            String otp = generateSecureOtp();

            redisService.set(
                    otpKey,
                    otp,
                    RedisConstants.OTP_TTL
            );

            /*
             * Reset verification flag whenever
             * a new OTP is generated.
             */
            redisService.delete(verifiedKey);

            int updatedRetryCount =
                    redisService.incrementRetryCount(
                            generateRetryKey,
                            RedisConstants.OTP_TTL
                    );

            log.info(
                    "OTP generated successfully. phoneNumber={}, retryCount={}",
                    phoneNumber,
                    updatedRetryCount
            );

            whatsAppService.sendTextMessage(new NotificationRequest(phoneNumber,
                    "Your verification OTP is: "
                            + otp
                            + "\n\nThis OTP is valid for 5 minutes.")
            );

            log.info(
                    "OTP sent successfully to {}",
                    phoneNumber
            );

        }
        catch (OtpException ex) {

            throw ex;

        }
        catch (Exception ex) {

            log.error(
                    "Error while generating OTP for {}",
                    phoneNumber,
                    ex
            );

            throw new OtpException(
                    "Unable to generate OTP.",
                    ex
            );
        }
    }


    /**
     * Verifies the OTP entered by the user.
     */
    public void verifyOtp(VerifyOtpRequest request) {

        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        String otpKey = RedisConstants.getOtpKey(phoneNumber);
        String verifyRetryKey = RedisConstants.getVerifyRetryKey(phoneNumber);
        String verifiedKey = RedisConstants.getVerifiedKey(phoneNumber);

        try {

            Integer retryCount = redisService.get(
                    verifyRetryKey,
                    Integer.class
            );

            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount >= RedisConstants.MAX_OTP_VERIFICATION_RETRIES) {

                long ttl = redisService.getRemainingTTL(verifyRetryKey);

                log.warn(
                        "Maximum OTP verification attempts exceeded. phoneNumber={}, remainingTTL={} seconds",
                        phoneNumber,
                        ttl
                );

                throw new OtpException(
                        "Too many invalid OTP attempts. Please try again after "
                                + ttl +
                                " seconds."
                );
            }

            String savedOtp = redisService.get(
                    otpKey,
                    String.class
            );

            if (savedOtp == null) {

                log.warn(
                        "OTP expired for phoneNumber={}",
                        phoneNumber
                );

                throw new OtpException(
                        "OTP has expired. Please generate a new OTP."
                );
            }

            if (!savedOtp.equals(request.getOtp())) {

                int updatedRetryCount =
                        redisService.incrementRetryCount(
                                verifyRetryKey,
                                RedisConstants.OTP_TTL
                        );

                log.warn(
                        "Invalid OTP entered. phoneNumber={}, retryCount={}",
                        phoneNumber,
                        updatedRetryCount
                );

                throw new OtpException(
                        "Invalid OTP."
                );
            }

            /*
             * OTP verified successfully.
             * Store verification status for 1 hour.
             */
            redisService.set(
                    verifiedKey,
                    Boolean.TRUE,
                    RedisConstants.OTP_VERIFIED_TTL
            );

            /*
             * Reset verification retry count.
             */
            redisService.delete(verifyRetryKey);

            log.info(
                    "OTP verified successfully for {}",
                    phoneNumber
            );

        }
        catch (OtpException ex) {

            throw ex;

        }
        catch (Exception ex) {

            log.error(
                    "Unexpected error while verifying OTP for {}",
                    phoneNumber,
                    ex
            );

            throw new OtpException(
                    "Unable to verify OTP.",
                    ex
            );
        }
    }

    /**
     * Checks whether a phone number has already been verified.
     * This method is intended to be used by createAppUser().
     */
    public boolean isOtpVerified(String phoneNumber) {

        try {

            phoneNumber = normalizePhoneNumber(phoneNumber);

            String verifiedKey =
                    RedisConstants.getVerifiedKey(phoneNumber);

            Boolean verified =
                    redisService.get(
                            verifiedKey,
                            Boolean.class
                    );

            return Boolean.TRUE.equals(verified);

        } catch (Exception ex) {

            log.error(
                    "Error while checking OTP verification status for {}",
                    phoneNumber,
                    ex
            );

            return false;
        }
    }

    /**
     * Generates a cryptographically secure numeric OTP.
     */
    private String generateSecureOtp() {

        int otpLength = RedisConstants.OTP_LENGTH;

        int lowerBound = (int) Math.pow(10, otpLength - 1);
        int upperBound = (int) Math.pow(10, otpLength);

        int otp = SECURE_RANDOM.nextInt(upperBound - lowerBound) + lowerBound;

        return String.valueOf(otp);
    }

    /**
     * Normalizes phone numbers before using them as Redis keys.
     *
     * Current assumptions:
     * - Removes spaces, hyphens and brackets.
     * - If number starts with "91" (without +), prefixes '+'.
     * - If number is a 10-digit Indian mobile number, prefixes +91.
     *
     * Examples:
     * 9876543210        -> +919876543210
     * 919876543210      -> +919876543210
     * +919876543210     -> +919876543210
     */
    private String normalizePhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new OtpException("Phone number cannot be empty.");
        }

        String normalized = phoneNumber
                .trim()
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "");

        if (normalized.startsWith("+")) {
            return normalized;
        }

        if (normalized.matches("^91\\d{10}$")) {
            return "+" + normalized;
        }

        if (normalized.matches("^[6-9]\\d{9}$")) {
            return "+91" + normalized;
        }

        return normalized;
    }

}