package se.moln.ecommerceintegration.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword, String firstName, String lastName) {
        if (users.existsByEmail(email)) throw new IllegalStateException("Email is already registered");
        String hash = passwordEncoder.encode(rawPassword);
        return users.save(User.newUser(email, hash, firstName, lastName));
    }
}