package com.codecollab.oj.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeleteRequest {
    private List<Long> ids;
}
