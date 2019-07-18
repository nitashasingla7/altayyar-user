package com.wellogic.appointments.web;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Defines the configuration for the test application context, ensures that only select packages are scanned for the
 * Spring components while select classes are excluded from being loaded. This application context configuration may be
 * used in REST/MVC tests that mock DB access,etc.
 *
 * @author smitha
 */
@Profile("test")
@ComponentScan(basePackages = { "com.wellogic.appointments.services", "com.wellogic.appointments.web",
    "com.wellogic.appointments.phoenix", "com.wellogic.appointments.common", "com.wellogic.appointments.core",
    "com.wellogic.appointments.common", "com.wellogic.appointments.client.kirona" })
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class TestWebAppConfig
{
    public static void main(String[] args)
    {
        SpringApplication.run(TestWebAppConfig.class, args);
    }

}