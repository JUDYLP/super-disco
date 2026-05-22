package com.example.personalfinance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryQueryDTO {
    private String type;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
