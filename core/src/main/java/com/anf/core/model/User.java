package com.anf.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private String firstName;
    private String lastName;
    private String country;
    private int age;
}
