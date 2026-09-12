package com.sky.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
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

    /** JSON 消息转换器：生产端与消费端共用同一份配置 */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.sky", "java.util", "java.math");
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    /** 生产端：把消息序列化成 JSON */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }

    /**
     * 消费端：@RabbitListener 默认使用 SimpleMessageConverter（JDK 序列化），
     * 与生产端的 JSON 转换器不匹配，消费者会拿到 byte[] 并抛 MessageConversionException，
     * 订单永远落不了库。必须把同一个 JSON 转换器显式装配到监听容器工厂上。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        return factory;
    }
}
