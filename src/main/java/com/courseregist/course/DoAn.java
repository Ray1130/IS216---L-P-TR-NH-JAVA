package com.courseregist.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DoAn {

	public static void main(String[] args) {
		SpringApplication.run(DoAn.class, args);
	}

}