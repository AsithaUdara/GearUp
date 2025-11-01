package com;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.gearup.automobileservice.DemoApplication;

// Don't attempt to auto-configure a real DataSource during unit tests in this monorepo.
@SpringBootTest(classes = DemoApplication.class,
	properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
