package com.happytails.adoption.dto;

import java.util.List;

public record FreeSlotsResponse(Long animalId, int slotMinutes, List<SlotDto> slots) {}
