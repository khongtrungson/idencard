package com.idencard.auth.server;

import com.idencard.auth.application.service.ClientService;
import com.idencard.auth.application.service.UserService;
import com.idencard.auth.domain.port.out.ClientRepositoryPort;
import com.idencard.auth.domain.port.out.PasswordEncoderPort;
import com.idencard.auth.domain.port.out.UserRepositoryPort;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.idencard.auth.server",
        "com.idencard.auth.infrastructure"
})
@EntityScan(basePackages = "com.idencard.auth.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.idencard.auth.infrastructure.persistence.repository")
public class IdenCardAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdenCardAuthServerApplication.class, args);
    }

    @Bean
    public UserService userService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        return new UserService(userRepository, passwordEncoder);
    }

    @Bean
    public ClientService clientService(ClientRepositoryPort clientRepository) {
        return new ClientService(clientRepository);
    }
}
