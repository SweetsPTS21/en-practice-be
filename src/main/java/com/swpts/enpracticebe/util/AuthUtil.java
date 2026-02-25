package com.swpts.enpracticebe.util;

import com.swpts.enpracticebe.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.UUID;

@Component
@AllArgsConstructor
public class AuthUtil {
    private final JwtUtil jwtUtil;

    public UUID getUserId() {
        UUID userId = getPrincipalFromSecurityContext();
        if (Objects.isNull(userId)) {
            return getUsernameFromJwtInRequest();
        }

        return userId;
    }

    private UUID getPrincipalFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UUID u) {
            return u;
        }
        return null;
    }

    private UUID getUsernameFromJwtInRequest() {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) return null;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUserId(token);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
