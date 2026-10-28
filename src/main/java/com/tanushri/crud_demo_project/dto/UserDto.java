package com.tanushri.crud_demo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor 
@AllArgsConstructor   
public class UserDto {

    private Long id;
    private String name;
    private int age;
}
