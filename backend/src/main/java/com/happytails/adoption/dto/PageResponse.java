package com.happytails.adoption.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, long totalElements, int totalPages) {}
