package com.vb.projects.airBnbApp.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ContactInfo {
    private String address;
    private String phone;
    private String email;
    private String location;
}
