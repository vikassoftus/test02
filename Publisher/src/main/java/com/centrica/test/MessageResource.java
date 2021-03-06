package com.centrica.test;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
public class MessageResource {
	
	final static String QUEUE_NAME = "hello";
	
	@Autowired
	private RabbitTemplate channel;

	@Bean
	Queue queue() {
		return new Queue(QUEUE_NAME,false);
	}
	
	@Bean
	TopicExchange exchange() {
		return new TopicExchange("centrica-exchange");
	}
	
	@Bean
	Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with(QUEUE_NAME);
	}
    
	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory ) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(QUEUE_NAME);
		return container;
	}
	
	
    @RequestMapping(method = RequestMethod.GET, value = "/sendMessage/{message}",produces = "application/json")
    @ResponseBody
    public ResponseEntity<String> sendMessage(@PathVariable String message) {
    	System.out.println(message);
        Message<String> msg = MessageBuilder.withPayload(message).build();
        try {
        	channel.convertAndSend("hello", msg);
        }catch(Exception e) {
        	return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }
}
