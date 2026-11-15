package com.idencard.auth.application.service;

import com.idencard.auth.domain.exception.UserAlreadyExistsException;
import com.idencard.auth.domain.exception.UserNotFoundException;
import com.idencard.auth.domain.model.User;
import com.idencard.auth.domain.port.in.LoadUserByUsernameUseCase;
import com.idencard.auth.domain.port.in.RegisterUserUseCase;
import com.idencard.auth.domain.port.out.PasswordEncoderPort;
import com.idencard.auth.domain.port.out.UserRepositoryPort;

import java.util.Objects;

public class UserService implements RegisterUserUseCase, LoadUserByUsernameUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public UserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepositoryPort must not be null");
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "PasswordEncoderPort must not be null");
    }

    @Override
    public User register(String username, String rawPassword, String email, String fullName) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User newUser = User.createNew(username, encodedPassword, email, fullName);
        return userRepository.save(newUser);
    }

    @Override
    public User loadByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
}
