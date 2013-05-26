package com.useful.ems;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UsefulEmsPublisherTest {

	@Test
	public void test() {
	      ApplicationContext context = 
	              new ClassPathXmlApplicationContext("config.xml");
	}

}
