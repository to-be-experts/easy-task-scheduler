package com.xxl.job.executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xuxueli 2018-10-28 00:38:13
 */
@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
public class ExecutorApplication {

	public static void main(String[] args) {
        SpringApplication.run(ExecutorApplication.class, args);
	}

}