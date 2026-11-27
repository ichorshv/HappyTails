package com.happytails.adoption.service;

import com.happytails.adoption.enums.AnimalStatus;

public record AnimalStatusChanged(Long animalId, AnimalStatus oldStatus, AnimalStatus newStatus) {}
