package com.happytails.adoption.mapper;

import com.happytails.adoption.dto.UserResponse;
import com.happytails.adoption.entity.User;

public final class UserMapper {

  private UserMapper() {}

  public static UserResponse toResponse(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
  }
}
