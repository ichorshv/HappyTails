package com.happytails.adoption.users.api;

public interface CurrentUserService {

  Long getCurrentUserId();

  String getCurrentEmail();

  Role getCurrentRole();
}
