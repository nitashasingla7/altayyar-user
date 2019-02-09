package com.altayyar.users.weapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * The main executable class that launches the stand-alone web application with an embedded web container. Uses the
 * configuration defined in {@code application.properties} file. As of now this application uses spring boot default
 * configuration.
 * 
 * {@link SpringBootApplication @SpringBootApplication} provides a convenient way to enable the standard Spring Boot
 * auto-configuration and component scanning via a single annotation. The separate {@link ComponentScan @ComponentScan}
 * {@link EnableJpaRepositories @EnableJpaRepositories}, and {@link EntityScan @EntityScan} annotations are added to
 * ensure that the necessary packages are scanned for Spring components, including any JPA entities and Spring Data JPA
 * repositories. If not provided, Spring would only look in this package and its sub-packages.
 *
 * @author nitasha
 */
@SpringBootApplication
@EnableJpaRepositories("com.altayyar")
@EntityScan("com.altayyar")
@ComponentScan(basePackages = { "com.altayyar" })
public class AlTayyarUsersApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(AlTayyarUsersApplication.class, args);
    }
}
