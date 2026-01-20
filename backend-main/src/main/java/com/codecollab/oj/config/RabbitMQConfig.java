package com.codecollab.oj.config;

import com.codecollab.oj.constants.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.codecollab.oj.constants.MqConstants.JUDGE_EXCHANGE_NAME;
import static com.codecollab.oj.constants.MqConstants.ROUTING_KEY;


/**
 * RabbitMQ配置
 */
@Configuration
public class RabbitMQConfig {
//    public static final String JUDGE_QUEUE = "code_judge_queue";
//    public static final String JUDGE_EXCHANGE_NAME = "code_judge_direxchange";
//    public static final String ROUTING_KEY = "code_judge_routing_key";
    @Bean
    public Queue judgeQueue() {
        return new Queue(MqConstants.JUDGE_QUEUE, true);
    }

    @Bean
    public DirectExchange judgeExchange() {
        return new DirectExchange(JUDGE_EXCHANGE_NAME);
    }

    @Bean
    public Binding bindingTask(){
        return BindingBuilder.bind(judgeQueue()).to(judgeExchange()).with(ROUTING_KEY);
    }
    @Bean
    public MessageConverter messageConverter() {

        return new Jackson2JsonMessageConverter();

    }
}

