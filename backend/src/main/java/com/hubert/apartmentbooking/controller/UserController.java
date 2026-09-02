package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.request.ChangePasswordRequest;
import com.hubert.apartmentbooking.dto.request.UpdateProfileRequest;
import com.hubert.apartmentbooking.dto.response.UserProfileResponse;
import com.hubert.apartmentbooking.exception.CurrentPasswordIncorrectException;
import com.hubert.apartmentbooking.exception.PasswordMismatchException;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(Constants.USERS_PATH)
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(Constants.ME_ENDPOINT)
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        return toResponse(findCurrentUser(authentication));
    }

    @PutMapping(Constants.ME_ENDPOINT + "/profile")
    public UserProfileResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        User user = findCurrentUser(authentication);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        userRepository.save(user);
        return toResponse(user);
    }

    @PutMapping(Constants.ME_ENDPOINT + "/password")
    public void changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
        User user = findCurrentUser(authentication);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new CurrentPasswordIncorrectException(Constants.CURRENT_PASSWORD_INCORRECT);
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new PasswordMismatchException(Constants.PASSWORDS_DO_NOT_MATCH);
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User findCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException(Constants.USER_NOT_FOUND));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(user.getFullName(), user.getEmail(), user.getPhone());
    }
}