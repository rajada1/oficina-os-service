package br.com.grupo99.osservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableJpaRepositories(basePackages = "br.com.grupo99.osservice.infrastructure.persistence")
public class OsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsServiceApplication.class, args);
    }
}
