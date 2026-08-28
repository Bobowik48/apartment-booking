package com.hubert.apartmentbooking.controller;

import com.hubert.apartmentbooking.constants.Constants;
import com.hubert.apartmentbooking.dto.response.UserProfileResponse;
import com.hubert.apartmentbooking.model.User;
import com.hubert.apartmentbooking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping(Constants.USERS_PATH)
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(Constants.ME_ENDPOINT)
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NoSuchElementException(Constants.USER_NOT_FOUND));
        return new UserProfileResponse(user.getFullName(), user.getEmail(), user.getPhone());
    }
}