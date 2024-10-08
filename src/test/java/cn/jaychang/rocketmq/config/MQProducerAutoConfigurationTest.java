package cn.jaychang.rocketmq.config;

import cn.jaychang.rocketmq.base.AbstractMQProducer;
import cn.jaychang.rocketmq.annotation.MQProducer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import static org.junit.Assert.*;

public class MQProducerAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	private void prepareApplicationContextEmpty() {
		this.context = new AnnotationConfigApplicationContext();
		this.context.register(MQBaseAutoConfiguration.class, MQProducerAutoConfiguration.class);
		this.context.refresh();
	}

	private void prepareApplicationContextMissingConfigure() {
		this.context = new AnnotationConfigApplicationContext();
		this.context.register(TestProducer.class);
		MQProducerAutoConfiguration.setProducer(null);
		this.context.register(MQProducerAutoConfiguration.class);
		this.context.refresh();
	}

	private void prepareApplicationContextMissingProducerGroupConfigure() {
		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.rocketmq.name-server-address:127.0.0.1:9876").applyTo(this.context);
		this.context.register(TestProducer.class);
		MQProducerAutoConfiguration.setProducer(null);
		this.context.register(MQProducerAutoConfiguration.class);
		this.context.refresh();
	}

	private void prepareApplicationContextWithoutParent() {
		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.rocketmq.name-server-address:127.0.0.1:9876").applyTo(this.context);
		TestPropertyValues.of("rocketmq.producer-group:test-producer-group").applyTo(this.context);
		this.context.register(TestProducerNoParent.class);
		this.context.register(MQProducerAutoConfiguration.class);
		this.context.refresh();
	}

	private void prepareApplicationContext() {
		this.context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.rocketmq.name-server-address:127.0.0.1:9876").applyTo(this.context);
		TestPropertyValues.of("rocketmq.producer-group:test-producer-group").applyTo(this.context);
		this.context.register(TestProducer.class);
		this.context.register(MQBaseAutoConfiguration.class, MQProducerAutoConfiguration.class);
		this.context.refresh();
	}

	@After
	public void close() {
		this.context.close();
	}


	@Test
	public void testEmpty() {
		prepareApplicationContextEmpty();
		assertNull(context.getBean(DefaultMQProducer.class));
	}

	@Test(expected = RuntimeException.class)
	public void testMissingConfigure() {
		prepareApplicationContextMissingConfigure();
	}

	@Test(expected = RuntimeException.class)
	public void testMissingPGConfigure() {
		prepareApplicationContextMissingProducerGroupConfigure();
	}

	@Test(expected = RuntimeException.class)
	public void testMissingParent() {
		prepareApplicationContextWithoutParent();
	}

	@Test
	public void testProducerConfiguration() throws Exception {
		prepareApplicationContext();
		DefaultMQProducer dp = context.getBean(DefaultMQProducer.class);
		assertNotNull(dp);
		assertEquals(dp.getProducerGroup(), "test-producer-group");
		assertEquals(dp.getNamesrvAddr(), "127.0.0.1:9876");
	}


	@Component
	@MQProducer
	static class TestProducer extends AbstractMQProducer {
	}

	@Component
	@MQProducer
	static class TestProducerNoParent {
	}


}
