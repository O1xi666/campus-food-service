package com.sky.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    /** 秒杀订单队列 */
    @Bean
    public Queue flashOrderQueue() {
        Map<String, Object> args = new HashMap<>();
        // 死信转发：消息被拒绝或过期后转到死信交换机
        args.put("x-dead-letter-exchange", "order.dlx.exchange");
        args.put("x-dead-letter-routing-key", "order.dlx");
        return new Queue("order.flash.queue", true, false, false, args);
    }

    /** 死信交换机 */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("order.dlx.exchange");
    }

    /** 死信队列 */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("order.dlx.queue", true);
    }

    /** 绑定死信队列到死信交换机 */
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("order.dlx");
    }

    /** JSON 消息转换器，确保对象正确序列化/反序列化 */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
