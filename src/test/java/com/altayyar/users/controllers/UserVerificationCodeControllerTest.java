package com.altayyar.users.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.altayyar.users.services.UserVerificationCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for {@link UserVerificationCodeController}
 * 
 * @author nitasha
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WebApplicationTest.class)
@WebMvcTest(UserVerificationCodeControllerTest.class)
@ActiveProfiles({ "test", "springDataJpa" })
@AutoConfigureDataJpa
public class UserVerificationCodeControllerTest
{
    @Autowired
    protected MockMvc mvc;
    @MockBean
    private UserVerificationCodeService userVerificationCodeService;

    @Test
    public void testCreateUserVerificationCode() throws Exception
    {
        String userId = "test123";
        String message = "successful";
        Integer resultCode = 1;
        ObjectMapper objectMapper = new ObjectMapper();

        Response response = new Response(message, resultCode);

        this.mvc.perform(post("/verification_code/" + userId + ".json").accept(MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_XML)).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }

    @Test
    public void testVerifyCode() throws Exception
    {
        String userId = "test123";
        String code = "ABG789HX";

        Integer resultCode = 1;
        ObjectMapper objectMapper = new ObjectMapper();

        VerifyCodeResponse response = new VerifyCodeResponse();
        response.setResultCode(resultCode);

        this.mvc.perform(get("/verification_code/" + userId + "/" + code + ".json").accept(MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_XML)).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(response)));
    }
}
