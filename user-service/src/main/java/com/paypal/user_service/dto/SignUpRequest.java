package com.paypal.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//default constructor
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    private String name;
    private String email;
    private String password;
    private String adminKey;


}
