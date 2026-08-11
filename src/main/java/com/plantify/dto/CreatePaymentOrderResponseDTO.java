package com.plantify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentOrderResponseDTO {
    private String razorpayOrderId;
    private Long amount; // in paise
    private String currency; // "INR"
    private String keyId; // Public Key ID only
    private String internalOrderId;
}
