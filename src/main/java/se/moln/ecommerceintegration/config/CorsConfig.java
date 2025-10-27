package se.moln.ecommerceintegration.config;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// NOTE: CORS is centrally configured in SecurityConfig.corsConfigurationSource().
// Leaving this class without @Configuration to avoid duplicate/competing CORS setups.
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // Intentionally no-op: handled by SecurityConfig
    }
}
