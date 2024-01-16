package com.example.emissions.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParsedEmissionPrediction {

    @JsonProperty("Category")
    private String category;

    @JsonProperty("Gas Units")
    private String gasUnits;

    @JsonProperty("Value")
    private String value;

}
