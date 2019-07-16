package com.wellogic.appointments.core.services;

import com.wellogic.appointments.common.model.ApplicationConstants;
import com.wellogic.appointments.common.model.PatientAppointmentRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A builder for building test data objects of {@link PatientAppointmentRequest}.
 * 
 * @author Saumil Jain
 */
public class PatientAppointmentRequestBuilder
{
    private PatientAppointmentRequest patientAppointmentRequest;
    
    public static PatientAppointmentRequestBuilder aFullyLoadedPatientAppointmentRequestBuilder() throws ParseException
    {
        SimpleDateFormat kironaSpecificDateFormat = new SimpleDateFormat(ApplicationConstants.KIRONA_SPECIFIC_DATE_FORMAT);
        String startDateString = "2017-01-11T01:01:01";  //"yyyy-MM-dd'T'HH:mm:ss";
        Date startDate = kironaSpecificDateFormat.parse(startDateString);
        String endDateString = "2017-01-12T01:01:01";
        Date endDate = kironaSpecificDateFormat.parse(endDateString);
         
        PatientAppointmentRequest par = new PatientAppointmentRequest();
        par.setPatientMpi("4545");
        par.setUserId("testUser");
        par.setCustomerId("testCustomerId");
        par.setStartDate(startDate);
        par.setEndDate(endDate);
        par.setOrgId("testOrgId");
        par.setReason("testReason");
        par.setLoggedInUserName("loggedInUserName");
        par.setLoggedInOrgId("loggedInOrgId");
        
        PatientAppointmentRequestBuilder builder = new PatientAppointmentRequestBuilder();
        builder.patientAppointmentRequest = par;
        
        return builder;
    }
    
    public PatientAppointmentRequestBuilder withCustomerIdNull()
    {
        patientAppointmentRequest.setCustomerId(null);
        return this;
    }
    
    public PatientAppointmentRequest build()
    {
        return patientAppointmentRequest;
    }
}
