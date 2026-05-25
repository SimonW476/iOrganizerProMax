package com.library.backend.service;

import com.library.backend.entities.ApplicationUser;
import com.library.backend.entities.UserRepository;
import com.library.security.Role;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepo;
    private final AuthenticationContext authContext;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, AuthenticationContext authContext, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.authContext = authContext;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(String username, String rawPassword) {
        String hashedPassword = passwordEncoder.encode(rawPassword);
        ApplicationUser newUser = new ApplicationUser(username, hashedPassword);
        userRepo.save(newUser);
    }

    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    public boolean isAdmin() {
        return authContext.getAuthenticatedUser(ApplicationUser.class)
                .map(user -> user.getRoles().contains(Role.ADMIN)).orElse(false);
    }

    // New helper method for TaskService to use
    public Optional<ApplicationUser> getAuthenticatedUser() {
        return authContext.getAuthenticatedUser(ApplicationUser.class);
    }

    @Override
    public @Nonnull UserDetails loadUserByUsername(@Nonnull String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}