package com.nalbam.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SampleApplication {

    public static void main(final String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

}
