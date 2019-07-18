package com.wellogic.appointments.web.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellogic.appointments.WebApplicationTest;
import com.wellogic.appointments.client.kirona.service.KironaServiceInitializer;
import com.wellogic.appointments.common.model.ApplicationConstants;
import com.wellogic.appointments.common.model.PatientAppointmentRequest;
import com.wellogic.appointments.core.services.PatientAppointmentRequestService;

/**
 * Test class for {@link AppointmentRequestController}.
 * 
 * @author rakeshnair
 *
 */
@WebMvcTest(AppointmentRequestControllerIT.class)
public class AppointmentRequestControllerIT extends WebApplicationTest
{
    @MockBean
    private PatientAppointmentRequestService patientAppointmentRequestService;
    @MockBean
    private KironaServiceInitializer kironaServiceInitializer;
    private static final String url = "/patients/{mpi}/appointment-requests";
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testCreatePatientAppointmentRequest_With_Calender_Role() throws Exception
    {
        String mpi = "4545";
        String postUrl = url.replace("{mpi}", mpi);
        PatientAppointmentRequest request = buildPatientAppointmentRequest(mpi);

        this.mvc.perform(
                post(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<PatientAppointmentRequest> argumentCaptor = ArgumentCaptor
                .forClass(PatientAppointmentRequest.class);
        verify(patientAppointmentRequestService, times(1)).createNewAppointmentRequest(argumentCaptor.capture());

        PatientAppointmentRequest passedRequest = argumentCaptor.getValue();
        Assert.assertEquals(request, passedRequest);
        Assert.assertEquals(request.getPatientMpi(), passedRequest.getPatientMpi());
        Assert.assertEquals(request.getCustomerId(), passedRequest.getCustomerId());
        Assert.assertEquals(request.getUserId(), passedRequest.getUserId());
        Assert.assertEquals(request.getReason(), passedRequest.getReason());
        Assert.assertEquals(request.getStartDate(), passedRequest.getStartDate());
        Assert.assertEquals(request.getEndDate(), passedRequest.getEndDate());
    }

    @Test
    @WithMockUser(roles = "SuperAdmin_All")
    public void testCreatePatientAppointmentRequest_With_SuperAdmin_All_Role() throws Exception
    {
        String mpi = "4545";
        String postUrl = url.replace("{mpi}", mpi);
        PatientAppointmentRequest request = buildPatientAppointmentRequest(mpi);

        this.mvc.perform(
                post(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<PatientAppointmentRequest> argumentCaptor = ArgumentCaptor
                .forClass(PatientAppointmentRequest.class);
        verify(patientAppointmentRequestService, times(1)).createNewAppointmentRequest(argumentCaptor.capture());

        PatientAppointmentRequest passedRequest = argumentCaptor.getValue();
        Assert.assertEquals(request, passedRequest);
        Assert.assertEquals(request.getPatientMpi(), passedRequest.getPatientMpi());
        Assert.assertEquals(request.getCustomerId(), passedRequest.getCustomerId());
        Assert.assertEquals(request.getUserId(), passedRequest.getUserId());
        Assert.assertEquals(request.getReason(), passedRequest.getReason());
        Assert.assertEquals(request.getStartDate(), passedRequest.getStartDate());
        Assert.assertEquals(request.getEndDate(), passedRequest.getEndDate());
    }

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testCreatePatientAppointmentRequest_With_Empty_Payload() throws Exception
    {
        String mpi = "4545";
        String postUrl = url.replace("{mpi}", mpi);

        this.mvc.perform(post(postUrl).contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError());

    }

    @Test
    @WithMockUser(roles = "FORBIDDEN_ROLE")
    public void testCreatePatientAppointmentRequest_With_Forbidden_Role() throws Exception
    {
        String mpi = "4545";
        String postUrl = url.replace("{mpi}", mpi);
        PatientAppointmentRequest request = buildPatientAppointmentRequest(mpi);

        this.mvc.perform(
                post(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreatePatientAppointmentRequest_With_User_UnAuthorized() throws Exception
    {
        String mpi = "4545";
        String postUrl = url.replace("{mpi}", mpi);
        PatientAppointmentRequest request = buildPatientAppointmentRequest(mpi);

        this.mvc.perform(
                post(postUrl).content(mapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private PatientAppointmentRequest buildPatientAppointmentRequest(String mpi) throws ParseException
    {
        SimpleDateFormat kironaSpecificDateFormat = new SimpleDateFormat(
                ApplicationConstants.KIRONA_SPECIFIC_DATE_FORMAT);
        String startDateString = "2017-01-11T01:01:01";  // "yyyy-MM-dd'T'HH:mm:ss";
        Date startDate = kironaSpecificDateFormat.parse(startDateString);
        String endDateString = "2017-01-12T01:01:01";
        Date endDate = kironaSpecificDateFormat.parse(endDateString);

        PatientAppointmentRequest request = new PatientAppointmentRequest();
        request.setPatientMpi(mpi);
        request.setUserId("testUser");
        request.setCustomerId("testCustomerId");
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setOrgId("testOrgId");
        request.setReason("testReason");
        return request;
    }
}
