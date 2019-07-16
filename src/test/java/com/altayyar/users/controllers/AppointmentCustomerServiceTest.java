package com.wellogic.appointments.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.wellogic.appointments.phoenix.PhoenixPatientAddressSearcher;
import com.wellogic.phoenix.common.domain.dto.contact.ContactInfoDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.kirona.drs.businesslayer.soap.service.AddCustomersType;
import com.kirona.drs.businesslayer.soap.service.BusinessDataListType;
import com.kirona.drs.businesslayer.soap.service.BusinessDataType;
import com.kirona.drs.businesslayer.soap.service.CustomerType;
import com.kirona.drs.businesslayer.soap.service.RemoveCustomersType;
import com.wellogic.appointments.client.kirona.service.KironaCustomerService;
import com.wellogic.appointments.common.BusinessDataAttribute;
import com.wellogic.appointments.common.model.Patient;
import com.wellogic.appointments.exception.PatientNoHomeAddressException;
import com.wellogic.appointments.mappers.PatientToCustomerTypeMapper;
import com.wellogic.phoenix.common.domain.dto.contact.AddressDTO;

import static com.wellogic.code.CodeConstants.ADDRESS_TYPE_HOME;
import static com.wellogic.code.CodeConstants.ADDRESS_TYPE_TEMPORARY;

/**
 * Test class for AppointmentCustomerService.
 *
 * @author saumil
 * @author christopherclark
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AppointmentCustomerServiceTest
{
    private static final List<String> addressTypes = Arrays.asList(ADDRESS_TYPE_HOME, ADDRESS_TYPE_TEMPORARY);
    private static Set<AddressDTO> addresses;

    @InjectMocks
    private AppointmentCustomerService appointmentCustomerService;

    @Mock
    private KironaCustomerService mockKironaCustomerService;

    @Mock
    private PatientToCustomerTypeMapper mockPatientToCustomerTypeMapper;

    @Mock
    private PhoenixPatientAddressSearcher mockPhoenixPatientAddressSearcher;

    @Before
    public void setup()
    {
        AddressDTO addressDTO = new AddressDTO("st1", "st2", "st3", "city", "state", "zip");
        addressDTO.setAddressType(ADDRESS_TYPE_HOME);
        addresses = Collections.singleton(addressDTO);
    }

    @Test
    public void testAddCustomer()
    {
        Patient patient = new Patient();
        patient.setLastName("Test name");
        patient.setContactInfo(new ContactInfoDTO());

        String testProgramName = "Program name";
        String modelId = "modelId";
        String organization = "orgname";
        String customerId = "customerId";
        String loggedInUserId = "loggedInUserId";
        String loggedInOrgId = "loggedInOrgId";

        appointmentCustomerService.setModelId(modelId);
        appointmentCustomerService.setOrganisation(organization);

        when(mockPhoenixPatientAddressSearcher.getPatientAddresses(patient.getMpi(), addressTypes, loggedInUserId, loggedInOrgId))
                .thenReturn(addresses);

        appointmentCustomerService.addCustomer(patient, testProgramName, customerId, loggedInUserId, loggedInOrgId);
        ArgumentCaptor<AddCustomersType> captor = ArgumentCaptor.forClass(AddCustomersType.class);
        verify(mockKironaCustomerService).addCustomer(captor.capture());

        AddCustomersType requestObject = captor.getValue();
        CustomerType returnedCustomer = requestObject.getCustomers().getCustomer().get(0);
        assertEquals(returnedCustomer.getUserId(), customerId);
        BusinessDataListType annexData = returnedCustomer.getAnnexData();
        assertContainsBusinessData(annexData, BusinessDataAttribute.GP_PRACTICE_NAME.getValue(), testProgramName);
        assertEquals(returnedCustomer.getModelId(), modelId);
        assertEquals(returnedCustomer.getOrganisation(), organization);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddCustomerWithNullModelIdAndOrganization()
    {
        Patient patient = new Patient();
        patient.setContactInfo(new ContactInfoDTO());
        patient.setLastName("Test name");
        String testProgramName = "Program name";
        String customerId = "customerId";
        String loggedInUserId = "loggedInUserId";
        String loggedInOrgId = "loggedInOrgId";

        appointmentCustomerService.setModelId(null);
        appointmentCustomerService.setOrganisation(null);

        when(mockPhoenixPatientAddressSearcher.getPatientAddresses(patient.getMpi(), addressTypes, loggedInUserId, loggedInOrgId))
                .thenReturn(addresses);

        appointmentCustomerService.addCustomer(patient, testProgramName, customerId, loggedInUserId, loggedInOrgId);
    }

    @Test(expected=PatientNoHomeAddressException.class)
    public void testAddCustomerWithNoHomeAddress()
    {
        Patient patient = new Patient();
        patient.setContactInfo(new ContactInfoDTO());
        patient.setLastName("Test name");
        String testProgramName = "Program name";
        String customerId = "customerId";
        String loggedInUserId = "loggedInUserId";
        String loggedInOrgId = "loggedInOrgId";

        appointmentCustomerService.setModelId(null);
        appointmentCustomerService.setOrganisation(null);

        when(mockPhoenixPatientAddressSearcher.getPatientAddresses(patient.getMpi(), addressTypes, loggedInUserId, loggedInOrgId))
                .thenReturn(null);

        appointmentCustomerService.addCustomer(patient, testProgramName, customerId, loggedInUserId, loggedInOrgId);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRemoveCustomerNullPatient()
    {
        appointmentCustomerService.removeCustomer(null);
    }

    @Test
    public void testRemoveCustomer()
    {
        String customerId = "customerId";

        appointmentCustomerService.removeCustomer(customerId);

        ArgumentCaptor<RemoveCustomersType> removeCustomersTypeArgCaptor = ArgumentCaptor.forClass(RemoveCustomersType.class);
        verify(mockKironaCustomerService).removeCustomer(removeCustomersTypeArgCaptor.capture());

        RemoveCustomersType actualRemoveCustomersType = removeCustomersTypeArgCaptor.getValue();

        assertNotNull("RemoveCustomersType should not be null", actualRemoveCustomersType);

        assertNotNull("customerIds object should not be null", actualRemoveCustomersType.getCustomerIds());

        assertNotNull("list of customerIds should not be null", actualRemoveCustomersType.getCustomerIds().getValue());

        assertFalse("list of customerIds should not be empty", actualRemoveCustomersType.getCustomerIds().getValue().isEmpty());

        assertTrue("list of customerIds should have only one element", actualRemoveCustomersType.getCustomerIds().getValue().size() == 1);

        assertEquals("patient mpi mis-match", customerId, actualRemoveCustomersType.getCustomerIds().getValue().get(0));
    }

    /**
     * Throw an exception if a BusinessDataListType does not contain a given key/value pair.
     */
    private void assertContainsBusinessData(BusinessDataListType dataList, String key, String value)
    {
        for(BusinessDataType data : dataList.getBusinessData())
        {
            if(data.getName().equals(key) && data.getValue().equals(value))
            {
                return;
            }
        }
        fail();
    }
}
