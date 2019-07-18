package com.wellogic.appointments.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellogic.appointments.WebApplicationTest;
import com.wellogic.appointments.client.kirona.service.KironaServiceInitializer;
import com.wellogic.appointments.common.model.Appointment;
import com.wellogic.appointments.common.model.AppointmentStatus;
import com.wellogic.appointments.common.model.AppointmentStatusChangeRequest;
import com.wellogic.appointments.core.services.AppointmentDetailService;
import com.wellogic.appointments.phoenix.PhoenixAuditor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test REST End points related to User Appointments functionality. This uses Spring Web MVC Test Framework.
 *
 * @author Smitha
 */
@WebMvcTest(UserAppointmentControllerIT.class)
public class UserAppointmentControllerIT extends WebApplicationTest
{
    @MockBean
    private KironaServiceInitializer kironaServiceInitializer;
    @MockBean
    private AppointmentDetailService appointmentDetailService;
    @MockBean
    private PhoenixAuditor phoenixAuditor;

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void getUserAppointmentsTest_With_Calendar_Role() throws Exception
    {
        Appointment appointment = new Appointment();
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(appointment);

        given(this.appointmentDetailService.getByUserIdAndDates(any())).willReturn(appointments);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String loggedInOrgId = "loggedInOrgId";

        ObjectMapper objectMapper = new ObjectMapper();
        this.mvc.perform(get("/users/" + userId + "/appointments/").param("beginDate", beginDate)
                .param("endDate", endDate).param("loggedInOrgId", loggedInOrgId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(objectMapper.writeValueAsString(appointments)));
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void getUserAppointmentsTest_WithSuperAdmin_Role() throws Exception
    {
        Appointment appointment;
        appointment = new Appointment();
        List<Appointment> appointments = new ArrayList<>();
        appointments.add(appointment);

        given(this.appointmentDetailService.getByUserIdAndDates(any())).willReturn(appointments);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String loggedInOrgId = "loggedInOrgId";

        ObjectMapper objectMapper = new ObjectMapper();
        this.mvc.perform(get("/users/" + userId + "/appointments/").param("beginDate", beginDate)
                .param("endDate", endDate).param("loggedInOrgId", loggedInOrgId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(objectMapper.writeValueAsString(appointments)));
    }

    @Test
    @WithMockUser(roles = "BAD_ROLE")
    public void getUserAppointmentsTest_Forbidden_Role() throws Exception
    {
        given(this.appointmentDetailService.getByUserIdAndDates(any())).willReturn(null);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String loggedInOrgId = "loggedInOrgId";

        this.mvc.perform(get("/users/" + userId + "/appointments/").param("beginDate", beginDate)
                .param("endDate", endDate).param("loggedInOrgId", loggedInOrgId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isForbidden());
    }

    @Test
    public void getUserAppointmentsTest_user_unAuthorized() throws Exception
    {
        given(this.appointmentDetailService.getByUserIdAndDates(any())).willReturn(null);

        String userId = "123";
        String beginDate = "2017-02-28T04:00:00.000+02:00";
        String endDate = "2017-04-28T04:00:00.000+02:00";
        String loggedInOrgId = "loggedInOrgId";

        this.mvc.perform(get("/users/" + userId + "/appointments/").param("beginDate", beginDate)
                .param("endDate", endDate).param("loggedInOrgId", loggedInOrgId).accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void testUpdateUserAppointmentsTest_WithSuperAdmin_Role() throws Exception
    {
        AppointmentStatusChangeRequest request = new AppointmentStatusChangeRequest();
        request.setName("testName");
        request.setStatus(AppointmentStatus.ACCEPTED);
        request.setComment("comment");

        String userId = "123";
        String appointmentId = "appId";
        ObjectMapper mapper = new ObjectMapper();

        this.mvc.perform(put("/users/" + userId + "/appointments/" + appointmentId)
                .content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<AppointmentStatusChangeRequest> argumentCaptor = ArgumentCaptor
                .forClass(AppointmentStatusChangeRequest.class);
        verify(appointmentDetailService, times(1)).updateAppointmentStatuses(argumentCaptor.capture(), eq(userId));

        AppointmentStatusChangeRequest passedRequest = argumentCaptor.getValue();
        Assert.assertEquals(passedRequest.getName(), request.getName());
        Assert.assertEquals(passedRequest.getComment(), passedRequest.getComment());
        Assert.assertEquals(passedRequest.getStatus(), request.getStatus());
    }

    @Test
    public void testUpdateUserAppointmentsTest_user_unAuthorized() throws Exception
    {
        String userId = "123";
        String appointmentId = "appId";

        this.mvc.perform(
                put("/users/" + userId + "/appointments/" + appointmentId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void testUpdateUserAppointmentsTest_WithEmptyRequest() throws Exception
    {
        String userId = "123";
        String appointmentId = "appId";

        this.mvc.perform(
                put("/users/" + userId + "/appointments/" + appointmentId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

}
