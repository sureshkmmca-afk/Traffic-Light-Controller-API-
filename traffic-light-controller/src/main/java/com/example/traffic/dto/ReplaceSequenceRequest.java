
package com.example.traffic.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReplaceSequenceRequest {
    @NotEmpty
    public List<PhaseDto> phases;
}
