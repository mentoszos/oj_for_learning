package com.codecollab.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
}
