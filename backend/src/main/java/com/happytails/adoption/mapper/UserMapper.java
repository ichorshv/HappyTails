package com.happytails.adoption.mapper;

import com.happytails.adoption.dto.AdminUserResponse;
import com.happytails.adoption.dto.UserResponse;
import com.happytails.adoption.entity.User;

public final class UserMapper {

  private UserMapper() {}

  public static UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFullName(),
        user.getRole(),
        user.getCity(),
        user.getMunicipality());
  }

  public static AdminUserResponse toAdminResponse(User user) {
    return new AdminUserResponse(
        user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive());
  }
}
