package com.school.school.service;

import com.school.school.infra.exception.BusinessException;
import com.school.school.infra.exception.EntityAlreadyExistsException;
import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.User;
import com.school.school.model.dto.user.*;
import com.school.school.model.enums.Role;
import com.school.school.model.enums.Status;
import com.school.school.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void save(UserRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EntityAlreadyExistsException("User with email already exists");
        }

        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());

        User user = new User();
        user.setEmail(userRequest.getEmail());
        user.setPassword(hashedPassword);
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);
        user.setStatus(Status.ENABLED);

        userRepository.save(user);

    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).
                orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));
    }

    public User findById(Long id) {
        return  userRepository.findById(id).
                orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));
    }

   public UserResponse update(UserUpdate dto, UserAuthenticated userAuthenticated) {
        Long userId = userAuthenticated.getUser().getId();
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

   public void disable(Long id){
        User existingUser  = findById(id);
        existingUser.setStatus(Status.DISABLED);

        userRepository.save(existingUser);
   }

   public void changePassword(ChangePasswordRequest request, UserAuthenticated userAuthenticated) {
        Long userId = userAuthenticated.getUser().getId();

        User user = findById(userId);

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new BusinessException("Old password is incorrect");
        }

        if(!Objects.equals(request.getNewPassword(), request.getConfirmNewPassword())){
            throw new BusinessException("New password and confirm new password do not match");
        }

        String hashedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedNewPassword);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
   }

   public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.getStatus().toString()
                ))
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = findById(id);
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getStatus().toString()
        );
    }

}
