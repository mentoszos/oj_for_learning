package com.codecollab.oj.sanbox.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DockerExitCodeConstants {

    private DockerExitCodeConstants() {
    }

    // ===== 状态定义 =====

    public static final int SUCCESS = 0;
    public static final int CE_OR_RE = 1;
    public static final int MLE = 137;

    // 可选兜底
    public static final int UNKNOWN = -1;


}
