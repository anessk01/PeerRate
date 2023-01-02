package com.dsproject.opinions.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Queue;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class Config {
    
    @Bean
    public Queue queue(){
        return new ActiveMQQueue("queueA");
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory(){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("tcp://localhost:61616");
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate(){
        return new JmsTemplate(activeMQConnectionFactory());
    }
}