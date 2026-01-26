package com.codecollab.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.MqMessageLog;
import com.codecollab.oj.service.MqMessageLogService;
import com.codecollab.oj.mapper.MqMessageLogMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【mq_message_log】的数据库操作Service实现
* @createDate 2026-01-26 16:53:59
*/
@Service
public class MqMessageLogServiceImpl extends ServiceImpl<MqMessageLogMapper, MqMessageLog>
    implements MqMessageLogService{

}




