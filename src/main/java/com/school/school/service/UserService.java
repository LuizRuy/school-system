package com.school.school.service;

import com.school.school.infra.security.SecurityUtil;
import com.school.school.model.User;
import com.school.school.model.dto.UserRequest;
import com.school.school.model.dto.UserResponse;
import com.school.school.model.dto.UserUpdate;
import com.school.school.model.enums.Role;
import com.school.school.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new RuntimeException("User with email already exists");
        }

        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);

        userRepository.save(user);

    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email).
                orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));
    }

    public User findById(Long id) {
        return  userRepository.findById(id).
                orElseThrow(() -> new RuntimeException("User with id " + id + " not found"));
    }

   public UserResponse update(UserUpdate dto) {

        Long userId = SecurityUtil.getUserId();

        User user = findById(userId);

       user.setFirstName(dto.getFirstName());
       user.setLastName(dto.getLastName());
       user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);


        return new UserResponse(
                dto.getEmail(),
                dto.getFirstName(),
                dto.getLastName()
        );
   }

   public void delete(Long id){

        User existingUser  = findById(id);
        userRepository.delete(existingUser);
   }
}
