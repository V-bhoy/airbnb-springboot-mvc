package com.vb.projects.airBnbApp.dto;

import com.vb.projects.airBnbApp.entity.User;
import com.vb.projects.airBnbApp.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
