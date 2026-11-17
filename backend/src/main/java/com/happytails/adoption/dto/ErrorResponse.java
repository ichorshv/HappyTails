package com.happytails.adoption.dto;

import java.util.List;

public record ErrorResponse(int status, String code, String message, List<String> details) {}
