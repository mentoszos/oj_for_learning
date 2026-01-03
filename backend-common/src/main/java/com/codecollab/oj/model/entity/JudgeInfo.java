package com.codecollab.oj.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class  JudgeInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<CheckPoint> checkPoints;
    private Integer totalPass;
    private Integer total;

}
