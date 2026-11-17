package com.happytails.adoption.dto;

import com.happytails.adoption.enums.Role;
import java.io.Serializable;

/** The logged-in user kept in the session. Holds no password. */
public record AuthUser(Long id, String email, Role role) implements Serializable {}
