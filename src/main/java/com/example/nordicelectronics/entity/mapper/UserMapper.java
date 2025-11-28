package com.example.nordicelectronics.entity.mapper;

import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.dto.user.UserDTO;
import com.example.nordicelectronics.entity.dto.user.UserResponseDTO;

public class UserMapper {

    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .isAdmin(user.isAdmin())
                .build();
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}

