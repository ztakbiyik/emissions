package com.example.emissions.model;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name= "PARSEDEMISSION")
public class ParsedEmission {

    @XmlElement
    private String category;
    @XmlElement
    private String year;
    @XmlElement
    private String scenario;
    @XmlElement
    private String gasUnit;
    @XmlElement
    private String NK;
    @XmlElement
    private String value;

}
