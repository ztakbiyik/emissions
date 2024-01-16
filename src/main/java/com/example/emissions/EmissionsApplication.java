package com.example.emissions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaRepositories
public class EmissionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmissionsApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                //registry.addMapping("/*").allowedOrigins("http://localhost:8998");
                registry.addMapping("/users").allowedOrigins("http://localhost:8998");
                registry.addMapping("/emissionsapi").allowedOrigins("http://localhost:8998");
            }
        };
    }

}

