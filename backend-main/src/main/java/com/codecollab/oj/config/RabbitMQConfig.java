package com.codecollab.oj.config;

import com.codecollab.oj.constants.MqConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> args = new HashMap<>();
        // 绑定死信交换机：消息失败后转发到哪
        args.put("x-dead-letter-exchange", MqConstants.JUDGE_DLX_EXCHANGE_NAME);
        // 绑定死信路由键：转发时带上的 RoutingKey
        args.put("x-dead-letter-routing-key", MqConstants.DLX_ROUTING_KEY);
        return new Queue(MqConstants.JUDGE_QUEUE, true,false,false,args);
    }

    @Bean
    public DirectExchange judgeExchange() {
        return new DirectExchange(JUDGE_EXCHANGE_NAME);
    }
    /**
     * 4. 声明死信交换机 (DLX)
     */
    @Bean
    public DirectExchange judgeDlxExchange() {
        return new DirectExchange(MqConstants.JUDGE_DLX_EXCHANGE_NAME);
    }

    /**
     * 5. 声明死信队列 (这是最后的垃圾桶)
     */
    @Bean
    public Queue judgeDlxQueue() {
        return new Queue(MqConstants.JUDGE_DLX_QUEUE, true);
    }

    /**
     * 6. 死信队列与死信交换机的绑定
     */
    @Bean
    public Binding bindingDlx() {
        return BindingBuilder.bind(judgeDlxQueue()).to(judgeDlxExchange()).with(MqConstants.DLX_ROUTING_KEY);
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

