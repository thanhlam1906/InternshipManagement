package com.example.internshipmanagement.config;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Integer userId;
    private final String username;
    private final String password;
    private final String fullName;
    private final Role role;
    private final boolean active;

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getRole(),
                user.getIsActive()
        );
    }

    /**
     * Get the currently authenticated user details, with full null-safety and type-checking.
     *
     * @return the current CustomUserDetails
     * @throws AccessDeniedException if not authenticated or principal is not CustomUserDetails
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        throw new AccessDeniedException("Invalid authentication type");
    }

    /**
     * Get the user ID of the currently authenticated user.
     *
     * @return the current user's ID
     * @throws AccessDeniedException if not authenticated or principal is not CustomUserDetails
     */
    public static Integer getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // User entity does not have an account-expiry field
    }

    @Override
    public boolean isAccountNonLocked() {
        return active; // Treat inactive users as locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // User entity does not have a credentials-expiry field
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
