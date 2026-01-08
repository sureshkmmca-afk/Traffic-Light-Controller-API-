
package com.example.traffic.dto;

import com.example.traffic.model.Direction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class PhaseDto {
    @NotBlank
    public String name;
    @NotEmpty
    public Set<Direction> greens;
    @Min(0)
    public long greenMs;
    @Min(0)
    public long yellowMs;
    @Min(0)
    public long allRedMs;
}
