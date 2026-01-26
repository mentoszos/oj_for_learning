package com.codecollab.oj.model.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class UserInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String username;
    private List<String> roles;
    private List<String> perms;
}
