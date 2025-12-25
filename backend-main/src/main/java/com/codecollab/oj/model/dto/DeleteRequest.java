package com.codecollab.oj.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Long> ids;
}
