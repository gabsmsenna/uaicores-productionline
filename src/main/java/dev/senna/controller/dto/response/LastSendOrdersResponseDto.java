package dev.senna.controller.dto.response;

import java.time.LocalDate;
import java.util.List;

public record LastSendOrdersResponseDto(
        String clientName,
        List<ListItemProductionLineResponse> items,
        LocalDate sendDate
) {
}
