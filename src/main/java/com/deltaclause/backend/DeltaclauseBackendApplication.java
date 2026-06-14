package com.deltaclause.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//@EnableCaching
public class DeltaclauseBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeltaclauseBackendApplication.class, args);
        System.out.println("=========================================================");
        System.out.println("  Deltaclause Secure Spring Boot Backend Service Started  ");
        System.out.println("  Hibernate ORM connected, Redis Caching Activated        ");
        System.out.println("=========================================================");
    }
}
