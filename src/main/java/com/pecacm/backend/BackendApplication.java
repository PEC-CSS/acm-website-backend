package com.pecacm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// TODO: Check if flyway script will be needed
// Might help jenkins pipeline. Root cause for these
@SpringBootApplication
@EntityScan({"com.pecacm.backend.*"})
@EnableJpaRepositories(value = {"com.pecacm.backend.*"})
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
