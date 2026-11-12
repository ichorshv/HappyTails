package com.happytails.adoption.service;

import com.happytails.adoption.enums.Role;

public interface CurrentUserService {

  Long getCurrentUserId();

  String getCurrentEmail();

  Role getCurrentRole();
}
