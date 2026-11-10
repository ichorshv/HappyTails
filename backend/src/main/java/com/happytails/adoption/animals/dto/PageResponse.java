package com.happytails.adoption.animals.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, long totalElements, int totalPages) {}
