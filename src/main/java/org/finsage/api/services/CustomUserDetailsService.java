package org.finsage.api.services;

import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.AuthProvider;
import org.finsage.api.repositories.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User.UserBuilder builder = User.withUsername(user.getEmail())
                .roles("USER");
        if (user.getProvider() == AuthProvider.LOCAL) {
            if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
                throw new IllegalStateException("Local user does not have a password");
            }
            builder.password(user.getPasswordHash());
        } else {
            builder.password("N/A");
        }
        return builder.build();
    }
}
