package br.com.grupo99.osservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OsServiceApplication.class, args);
    }
}
