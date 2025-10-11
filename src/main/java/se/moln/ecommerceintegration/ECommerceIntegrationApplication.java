package se.moln.ecommerceintegration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.moln.ecommerceintegration.model.Role;
import se.moln.ecommerceintegration.model.User;
import se.moln.ecommerceintegration.repository.UserRepository;
@SpringBootApplication
public class ECommerceIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceIntegrationApplication.class, args);
    }

    @Bean
    CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            users.findByEmail("admin@example.com").ifPresentOrElse(
                    existing -> {},
                    () -> {
                        User admin = User.newUser("admin@example.com", encoder.encode("Password123!"), "Admin", "User");
                        admin.setRole(Role.ADMIN);
                        users.save(admin);
                    }
            );
        };
    }
}
