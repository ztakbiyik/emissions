package com.example.emissions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
public class Emission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String gasUnits;
    private String description;
    private String predictedValue;
    private String actualValue;

    public Emission (){
        setCategory("");
        setGasUnits("");
        setDescription("");
        setPredictedValue("");
        setActualValue("");
    }



}
