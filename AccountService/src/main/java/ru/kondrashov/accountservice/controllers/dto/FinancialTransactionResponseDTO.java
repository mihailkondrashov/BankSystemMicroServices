package ru.kondrashov.accountservice.controllers.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@Data
public class FinancialTransactionResponseDTO {

    private UUID id;

    @NotNull(message = "Message should not null")
    private Currency currency;

    @Min(message = "Amount should be more than zero", value = 0)
    private BigDecimal amount;

    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private LocalDateTime time = LocalDateTime.now();

    @NotEmpty(message = "Message should not empty")
    private String message;

    public FinancialTransactionResponseDTO(UUID id, BigDecimal amount, LocalDateTime time, String message, Currency currency) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.time = time;
        this.message = message;

    }
}
