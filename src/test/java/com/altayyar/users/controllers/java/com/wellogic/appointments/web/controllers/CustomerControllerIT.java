package com.wellogic.appointments.web.controllers;

import javax.ws.rs.core.MediaType;

import com.wellogic.appointments.client.kirona.service.KironaServiceInitializer;
import com.wellogic.util.user.LumiraUserUtil;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import com.wellogic.appointments.WebApplicationTest;
import com.wellogic.appointments.common.model.Patient;
import com.wellogic.appointments.core.services.AppointmentCustomerService;
import com.wellogic.appointments.phoenix.PhoenixPatientSearcher;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for CustomerController.
 *
 * @author christopherclark
 *
 */
@WebMvcTest(CustomerControllerIT.class)
public class CustomerControllerIT extends WebApplicationTest
{
    @MockBean
    private KironaServiceInitializer mockKironaServiceInitializer;
    @MockBean
    private AppointmentCustomerService mockCustomerService;
    @MockBean
    private PhoenixPatientSearcher mockSearcher;
    @MockBean
    private LumiraUserUtil mockUserUtil;

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testRegisterPatient() throws Exception
    {
        String mpi = "mpi";
        String programName = "programName";
        String customerId = "customerId";
        String loggedInUserId = "loggedInUserId";
        String loggedInUsername = "loggedInUsername";
        String loggedInOrgId = "loggedInOrgId";
        String JSON = "{\"mpi\":\"mpi\",\"programName\":\"programName\", \"customerId\":\"customerId\", \"loggedInUserId\":\"loggedInUserId\", \"loggedInOrgId\":\"loggedInOrgId\"}";

        Patient patient = new Patient();
        patient.setNhs("1234567890");

        when(mockUserUtil.getUsernameForId(loggedInUserId)).thenReturn(loggedInUsername);
        when(mockSearcher.findByMpi(mpi, loggedInUsername, loggedInOrgId)).thenReturn(patient);

        this.mvc.perform(post("/patients").content(JSON).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(mockCustomerService).addCustomer(patient, programName, customerId, loggedInUserId, loggedInOrgId);
    }

    @Test
    @WithMockUser(roles = "Calendar_Update")
    public void testRemovePatient() throws Exception
    {
        String mpi = "mpi";
        String loggedInUserId = "loggedInUserId";
        String loggedInOrgId = "loggedInOrgId";
        String customerId = "customerId";
        
        Patient patient = new Patient();
        patient.setNhs("1234567890");
        String JSON = "{\"mpi\":\"mpi\",\"loggedInUserId\":\"loggedInUserId\", \"loggedInOrgId\":\"loggedInOrgId\"}";

        when(mockSearcher.findByMpi(mpi, loggedInUserId, loggedInOrgId)).thenReturn(patient);
        this.mvc.perform(delete("/patients/" + customerId).content(JSON).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        verify(mockCustomerService).removeCustomer(customerId);
    }

}