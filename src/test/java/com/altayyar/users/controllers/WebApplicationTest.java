package com.altayyar.users.controllers;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Defines the configuration for the test application context, ensures that only select packages are scanned for the
 * Spring components while select classes are excluded from being loaded. This application context configuration may be
 * used in REST/MVC tests that mock DB access,etc.
 *
 * @author nitasha
 */
@RunWith(SpringRunner.class)
@AutoConfigureDataJpa
@ComponentScan(basePackages = { "com.altayyar" })
@Profile("test")
@Ignore
public class WebApplicationTest
{

    public static void main(String[] args)
    {
        SpringApplication.run(WebApplicationTest.class, args);
    }
}
