package com.griddb.volcanic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VolcanicMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(VolcanicMonitorApplication.class, args);
    }

}
