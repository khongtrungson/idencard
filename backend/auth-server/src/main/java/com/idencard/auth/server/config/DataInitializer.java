package com.idencard.auth.server.config;

import com.idencard.auth.application.service.UserService;
import com.idencard.auth.domain.model.ClientApp;
import com.idencard.auth.domain.model.Role;
import com.idencard.auth.domain.model.User;
import com.idencard.auth.domain.port.out.ClientRepositoryPort;
import com.idencard.auth.domain.port.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepositoryPort userRepository;
    private final UserService userService;
    private final ClientRepositoryPort clientRepository;

    public DataInitializer(UserRepositoryPort userRepository, UserService userService, ClientRepositoryPort clientRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.clientRepository = clientRepository;
    }

    @Override
    public void run(String... args) {
        initDefaultClients();
        initDefaultUsers();
    }

    private void initDefaultClients() {
        String androidClientId = "idencard-android";
        if (!clientRepository.existsByClientId(androidClientId)) {
            ClientApp androidClient = ClientApp.createPublicClient(
                    androidClientId,
                    "IdenCard Android Mobile Client",
                    Set.of("com.idencard.android://oauth2redirect"),
                    Set.of("openid", "profile", "email", "card:read", "card:write")
            );
            clientRepository.save(androidClient);
            log.info("Initialized default OAuth2 PKCE Client: {}", androidClientId);
        }
    }

    private void initDefaultUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = userService.register("admin", "admin123", "admin@idencard.com", "System Administrator");
            admin.addRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            log.info("Created default user: admin / admin123");
        }

        if (!userRepository.existsByUsername("cardholder")) {
            User cardholder = userService.register("cardholder", "card123", "holder@idencard.com", "Nguyễn Văn A (Cardholder)");
            log.info("Created default user: cardholder / card123");
        }
    }
}
