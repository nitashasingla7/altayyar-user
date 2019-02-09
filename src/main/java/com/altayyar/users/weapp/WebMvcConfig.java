package com.altayyar.users.weapp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Web configuration class to override content negotiation handling for REST Apis. This class overrides the content
 * negotiation method of {@link WebMvcConfigurationSupport) to add support for json, xml and csv type response based on
 * the path extension.
 * 
 * @author nitasha
 *
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "com.altayyar" })
public class WebMvcConfig extends WebMvcConfigurationSupport
{

    /**
     * This method overrides content negotiation to enable Path extension for content type, ignores header and disable
     * parameter type. It also makes default content type as json. 
     * 
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer)
    {
        // set path extension to true
        configurer.favorPathExtension(true).
        // set favor parameter to false
        favorParameter(false).
        //ignore the accept headers
        ignoreAcceptHeader(true).
        defaultContentType(MediaType.APPLICATION_XML)
        .mediaType("xml", MediaType.APPLICATION_XML).mediaType("json", MediaType.APPLICATION_JSON)
        .mediaType("csv", new MediaType("text/csv"));
    }

}