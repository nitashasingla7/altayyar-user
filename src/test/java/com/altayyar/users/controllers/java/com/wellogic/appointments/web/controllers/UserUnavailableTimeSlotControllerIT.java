package com.wellogic.appointments.web.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellogic.appointments.WebApplicationTest;
import com.wellogic.appointments.client.kirona.service.KironaServiceInitializer;
import com.wellogic.appointments.common.model.ApplicationConstants;
import com.wellogic.appointments.common.model.UserUnavailableTimeSlot;
import com.wellogic.appointments.core.services.UserUnavailabilityTimeService;
import com.wellogic.appointments.phoenix.PhoenixAuditor;

/**
 * Test class for {@link UserUnavailableTimeSlotController}.
 * 
 * @author nitasha
 *
 */
@WebMvcTest(UserUnavailableTimeSlotControllerIT.class)
public class UserUnavailableTimeSlotControllerIT extends WebApplicationTest
{
    @MockBean
    private UserUnavailabilityTimeService userUnavailabilityTimeService;
    @MockBean
    private KironaServiceInitializer kironaServiceInitializer;
    @MockBean
    private PhoenixAuditor phoenixAuditor;
    private static final String url = "/users/{userId}/unavailabilities";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void getUserUnavailableTimeSlot_With_SuperAdmin_Role() throws Exception
    {
        UserUnavailableTimeSlot unavailableTimeSlot = new UserUnavailableTimeSlot();
        List<UserUnavailableTimeSlot> unavailableTimeSlots = new ArrayList<>();
        unavailableTimeSlots.add(unavailableTimeSlot);

        given(this.userUnavailabilityTimeService.getUnavailableTimeSlotsForUser(any()))
                .willReturn(unavailableTimeSlots);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String getUrl = url.replace("{userId}", userId);

        ObjectMapper objectMapper = new ObjectMapper();
        this.mvc.perform(
                get(getUrl).param("beginDate", beginDate).param("endDate", endDate).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(unavailableTimeSlots)));
    }

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testGetUserUnavailableTimeSlot_With_Calender_Role() throws Exception
    {
        UserUnavailableTimeSlot unavailableTimeSlot = new UserUnavailableTimeSlot();
        List<UserUnavailableTimeSlot> unavailableTimeSlots = new ArrayList<>();
        unavailableTimeSlots.add(unavailableTimeSlot);

        given(this.userUnavailabilityTimeService.getUnavailableTimeSlotsForUser(any()))
                .willReturn(unavailableTimeSlots);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String getUrl = url.replace("{userId}", userId);

        ObjectMapper objectMapper = new ObjectMapper();
        this.mvc.perform(
                get(getUrl).param("beginDate", beginDate).param("endDate", endDate).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(unavailableTimeSlots)));
    }

    @Test
    @WithMockUser(roles = "FORBIDDEN_ROLE")
    public void testGetUserUnavailableTimeSlot_With_Forbidden_Role() throws Exception
    {
        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String getUrl = url.replace("{userId}", userId);

        this.mvc.perform(
                get(getUrl).param("beginDate", beginDate).param("endDate", endDate).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetUserUnavailableTimeSlot_With_User_UnAuthorized() throws Exception
    {
        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String getUrl = url.replace("{userId}", userId);

        this.mvc.perform(
                get(getUrl).param("beginDate", beginDate).param("endDate", endDate).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void testCreateUserUnavailableTimeSlot_With_SuperAdmin_Role() throws Exception
    {
        String userId = "123";
        String postUrl = url.replace("{userId}", userId);

        UserUnavailableTimeSlot request = userUnavailableTimeSlotRequest(userId);

        this.mvc.perform(
                post(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<UserUnavailableTimeSlot> argumentCaptor = ArgumentCaptor.forClass(UserUnavailableTimeSlot.class);
        verify(userUnavailabilityTimeService, times(1)).createUnavailableTimeSlotForUser(argumentCaptor.capture());

        UserUnavailableTimeSlot passedRequest = argumentCaptor.getValue();
        Assert.assertEquals(passedRequest.getUserId(), request.getUserId());
        Assert.assertEquals(passedRequest.getUnavailableReasonCode(), passedRequest.getUnavailableReasonCode());
        Assert.assertEquals(passedRequest.getStartDate(), request.getStartDate());
        Assert.assertEquals(passedRequest.getEndDate(), request.getEndDate());
        Assert.assertEquals(passedRequest.getAddTimeSlot(), request.getAddTimeSlot());
    }

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testCreateUserUnavailableTimeSlot_With_Empty_Payload() throws Exception
    {
        String userId = "1233";
        String postUrl = url.replace("{userId}", userId);

        this.mvc.perform(post(postUrl).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void testDeleteUserUnavailableTimeSlot_With_SuperAdmin_Role() throws Exception
    {
        String userId = "123";
        String postUrl = url.replace("{userId}", userId);

        UserUnavailableTimeSlot request = userUnavailableTimeSlotRequest(userId);

        this.mvc.perform(
                delete(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<UserUnavailableTimeSlot> argumentCaptor = ArgumentCaptor.forClass(UserUnavailableTimeSlot.class);
        verify(userUnavailabilityTimeService, times(1)).deleteUnavailableTimeSlotForUser(argumentCaptor.capture());

        UserUnavailableTimeSlot passedRequest = argumentCaptor.getValue();
        Assert.assertEquals(passedRequest.getUserId(), request.getUserId());
        Assert.assertEquals(passedRequest.getUnavailableReasonCode(), passedRequest.getUnavailableReasonCode());
        Assert.assertEquals(passedRequest.getStartDate(), request.getStartDate());
        Assert.assertEquals(passedRequest.getEndDate(), request.getEndDate());
    }
    
    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testDeleteUserUnavailableTimeSlot_With_Empty_Payload() throws Exception
    {
        String userId = "1233";
        String postUrl = url.replace("{userId}", userId);

        this.mvc.perform(delete(postUrl).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError());
    }

    private UserUnavailableTimeSlot userUnavailableTimeSlotRequest(String userId) throws ParseException
    {
        SimpleDateFormat kironaSpecificDateFormat = new SimpleDateFormat(
                ApplicationConstants.KIRONA_SPECIFIC_DATE_FORMAT);
        String startDateString = "2017-01-11T01:01:01";  // "yyyy-MM-dd'T'HH:mm:ss";
        Date startDate = kironaSpecificDateFormat.parse(startDateString);
        String endDateString = "2017-01-12T01:01:01";
        Date endDate = kironaSpecificDateFormat.parse(endDateString);

        UserUnavailableTimeSlot request = new UserUnavailableTimeSlot();
        request.setUserId(userId);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setAddTimeSlot(true);
        request.setUnavailableReasonCode("testReason");
        return request;
    }

}
