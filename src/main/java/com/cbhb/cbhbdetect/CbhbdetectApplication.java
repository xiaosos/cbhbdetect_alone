package com.cbhb.cbhbdetect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.cbhb")
@MapperScan("com.cbhb.dao.mapper")
public class CbhbdetectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CbhbdetectApplication.class, args);
	}

}
