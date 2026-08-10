package com.shippex.security;

import com.shippex.service.security.UserDetailsServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestUri = request.getRequestURI();

        final String authHeader =
                request.getHeader("Authorization");

        log.info(
                "JWT filter: URI={}, Authorization header present={}",
                requestUri,
                authHeader != null
        );

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            log.info(
                    "JWT filter: No valid Bearer header for URI={}",
                    requestUri
            );

            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {

            String username =
                    jwtService.extractUsername(jwt);

            log.info(
                    "JWT filter: username extracted={}, URI={}",
                    username,
                    requestUri
            );

            if (username != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(username);

                log.info(
                        "JWT filter: user loaded={}, authorities={}",
                        userDetails.getUsername(),
                        userDetails.getAuthorities()
                );

                boolean valid =
                        jwtService.isTokenValid(
                                jwt,
                                (CustomUserDetails) userDetails
                        );

                log.info(
                        "JWT filter: validation result for {} = {}",
                        username,
                        valid
                );

                if (valid) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);

                    log.info(
                            "JWT filter: SecurityContext authenticated. " +
                                    "username={}, authorities={}",
                            username,
                            userDetails.getAuthorities()
                    );

                } else {

                    log.warn(
                            "JWT filter: JWT validation FAILED for username={}",
                            username
                    );
                }
            }

        } catch (JwtException exception) {

            log.warn(
                    "JWT filter: Invalid JWT. URI={}, reason={}",
                    requestUri,
                    exception.getMessage()
            );

        } catch (Exception exception) {

            log.error(
                    "JWT filter: Unexpected error. URI={}",
                    requestUri,
                    exception
            );
        }

        filterChain.doFilter(request, response);
    }
}