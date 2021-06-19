package org.greeneyed.tplanner;

import org.greeneyed.summer.config.enablers.EnableSummer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackageClasses = Application.class)
@EnableSummer(slf4j_filter = true, log4j = true)
@EnableJpaRepositories
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}