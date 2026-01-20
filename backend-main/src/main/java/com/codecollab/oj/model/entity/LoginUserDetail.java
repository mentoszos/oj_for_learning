package com.codecollab.oj.model.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


public class LoginUserDetail extends User {
    private final Long userId;
    public LoginUserDetail(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
    public Long getUserId(){return userId;}
}
