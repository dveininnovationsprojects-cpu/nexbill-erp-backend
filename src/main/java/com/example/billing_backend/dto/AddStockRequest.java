package com.example.billing_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddStockRequest {
    private Long productId;

    // INGA THAAN MAATHI IRUKKEN: Integer -> Double
    // Idhu thaan 1.5 KG mathiri values-ah API-la irundhu correct-ah vaangum
    private Double quantity;
}