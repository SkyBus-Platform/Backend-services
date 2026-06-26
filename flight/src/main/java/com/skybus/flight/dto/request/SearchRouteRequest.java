package com.skybus.flight.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SearchRouteRequest(
        @NotBlank @Size(min = 3, max = 3) String    origin,
        @NotBlank @Size(min = 3, max = 3) String    destination,
        @NotNull                          LocalDate  date,
        @Pattern(regexp = "price|duration") String  optimize,   // default: price
        @Min(1) @Max(4)                   Integer   maxHops     // default: 3
) {
    public String optimize()  { return optimize  != null ? optimize  : "price"; }
    public @Min(1) @Max(4) Integer maxHops()   { return maxHops   != null ? maxHops   : 3;       }
}