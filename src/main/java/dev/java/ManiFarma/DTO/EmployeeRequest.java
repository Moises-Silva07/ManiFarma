package dev.java.ManiFarma.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EmployeeRequest {

    private String name;
    private String role;
    private Double salary;
    private String shift;
}