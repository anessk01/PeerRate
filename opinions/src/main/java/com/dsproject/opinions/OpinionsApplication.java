package com.dsproject.opinions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class OpinionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpinionsApplication.class, args);
    }

}
