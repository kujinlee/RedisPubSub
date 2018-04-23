package hello;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@SpringBootApplication
public class Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	@Bean
	RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
			MessageListenerAdapter listenerAdapter) {

		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage2");
	}

	@Bean
	Receiver receiver(CountDownLatch latch) {
		return new Receiver(latch);
	}

	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(5);
	}

	@Bean
	StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

	public static void main(String[] args) throws InterruptedException {

		ApplicationContext ctx = SpringApplication.run(Application.class, args);

		StringRedisTemplate template = ctx.getBean(StringRedisTemplate.class);
		CountDownLatch latch = ctx.getBean(CountDownLatch.class);

		LOGGER.info("Sending message 1 ...");
		template.convertAndSend("chat", "Hello from Redis 1!");
		LOGGER.info("CountDownlatch count:"+latch.getCount());
		LOGGER.info("Sending message 2 ...");
		template.convertAndSend("chat", "Hello from Redis 2!");
		LOGGER.info("CountDownlatch count:"+latch.getCount());
		
		LOGGER.info("Sending message 3 ...");
		template.convertAndSend("chat", "Hello from Redis 3!");
		LOGGER.info("CountDownlatch count:"+latch.getCount());
		
		LOGGER.info("Sending message 4 ...");
		template.convertAndSend("chat", "Hello from Redis 4!");
		LOGGER.info("CountDownlatch count:"+latch.getCount());
		
		LOGGER.info("Sending message 5 ...");
		template.convertAndSend("chat", "Hello from Redis 5!");
		LOGGER.info("CountDownlatch count:"+latch.getCount());

		latch.await();

		System.exit(0);
	}
}