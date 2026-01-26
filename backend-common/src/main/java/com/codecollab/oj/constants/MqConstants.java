package com.codecollab.oj.constants;

public class MqConstants {
    private MqConstants(){};
    public static final String JUDGE_QUEUE = "code_judge_queue";
    public static final String JUDGE_EXCHANGE_NAME = "code_judge_direcchange";
    public static final String ROUTING_KEY = "code_judge_routing_key";
    public static final String JUDGE_DLX_EXCHANGE_NAME = "code_judge_dlx_direcchange";
    public static final String DLX_ROUTING_KEY = "code_judge_dlx_routing_key";
    public static final String JUDGE_DLX_QUEUE = "code_judge_dlx_queue";



}
