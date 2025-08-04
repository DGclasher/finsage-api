package org.finsage.api.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.finsage.api.entities.AppUser;
import org.finsage.api.entities.AuthProvider;
import org.finsage.api.repositories.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final AppUserRepository userRepository;
    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    Dotenv dotenv = Dotenv.load();
    private final String CLIENT_URL = dotenv.get("CLIENT_URL", "http://localhost:3000");

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        AppUser user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    AppUser newUser = new AppUser();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProvider(AuthProvider.GOOGLE);
                    newUser.setPasswordHash(null);
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getEmail(), user.getId().toString());
        response.sendRedirect(CLIENT_URL + "/login?token=" + token);
    }
}
