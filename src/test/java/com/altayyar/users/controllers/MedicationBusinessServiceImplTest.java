package com.wellogic.phoenix.businessservices.medication;

import com.wellogic.code.model.CodeSetEnum;
import com.wellogic.phoenix.PhoenixException;
import com.wellogic.phoenix.businessservices.*;
import com.wellogic.phoenix.code.CodeLookupBusinessServiceImpl;
import com.wellogic.phoenix.code.CodeSetConstants;
import com.wellogic.phoenix.code.SpineCodeLookupService;
import com.wellogic.phoenix.common.domain.dto.CodeDTO;
import com.wellogic.phoenix.common.domain.dto.DateTimeDTO;
import com.wellogic.phoenix.common.domain.dto.PersonDTO;
import com.wellogic.phoenix.common.domain.model.dbone.*;
import com.wellogic.phoenix.common.domain.model.dbone.DBOneBase.DBOneModelType;
import com.wellogic.phoenix.common.eprescription.SurescriptsMessage;
import com.wellogic.phoenix.common.util.ModuleName.ModuleNames;
import com.wellogic.phoenix.common.util.WellogicErrorCode.WellogicErrorCodes;
import com.wellogic.phoenix.commons.util.EntityAssociationLoader;
import com.wellogic.phoenix.dao.*;
import com.wellogic.phoenix.domain.dto.*;
import com.wellogic.phoenix.domain.models.dbone.DataProviderBuilder;
import com.wellogic.phoenix.domain.models.dbone.EntityBaseTest;
import com.wellogic.phoenix.domain.models.dbone.EntityDocumentBuilder;
import com.wellogic.phoenix.domain.models.dbone.MedicationBuilder;
import com.wellogic.phoenix.domain.models.transformers.MedicationToMedicationDTOTransformer;
import com.wellogic.phoenix.eprescription.ExternalPharmacyDirectoryBusinessServiceImpl;
import com.wellogic.phoenix.eprescription.PatientRxDispensedMedication;
import com.wellogic.phoenix.eprescription.PatientRxDispensedMedicationBuilder;
import com.wellogic.phoenix.eprescription.SurescriptsMessageBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

import static com.wellogic.phoenix.code.CodeSetConstants.MEDICATION_LIFECYCLE;
import static com.wellogic.phoenix.code.CodeSetConstants.NDC_COMPENDIUM;
import static com.wellogic.phoenix.common.util.ApplicationConstants.DEFAULT_ENTITY_DOCUMENT_TYP_CD;
import static com.wellogic.phoenix.common.util.ApplicationConstants.DEFAULT_WELLOGIC_AUTHORITY;
import static com.wellogic.phoenix.dao.DBOneBaseMockUtils.mockDbOneSaveReturningInputArgument;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * unit tests for {@link MedicationBusinessServiceImpl}
 *
 * @author matt merrill
 *
 */
public class MedicationBusinessServiceImplTest
{
    private MedicationDAO mockMedicationDao;
    private EntityDocumentDAO mockEntityDocumentDao;
    private SupplyDAO mockSupplyDao;
    private PersonDAO mockPersonDao;
    private PatientDemographicsDAO mockDemoDao;
    private SureScriptsMedHistoryDAO mockMedHistoryDao;
    private OrganizationUserViewDAO mockOrgUserViewDao;
    private ProviderBusinessServiceImpl mockProviderService;
    private PharmacyBusinessServiceImpl mockPharmacyService;
    private CodeLookupBusinessServiceImpl mockCodeLookupBusinessService;
    private EntityAssociationLoader mockEntityAssociationLoader;
    private EntityAliasDAO mockEntityAliasDao;
    private SurescriptsMessageBusinessServiceImpl mockSurescriptsMessageService;
    private MedicationToMedicationDTOTransformer mockMedicationDTOTransformer;
    private ExternalPharmacyDirectoryBusinessServiceImpl mockExternalPharmacyDirectoryService;
    private AuthorityBusinessServiceImpl mockAuthorityBusinessService;
    private MedicationHistoryBusinessServiceImpl mockMedicationHistoryBusinessService;
    private UserBusinessServiceImpl mockUserBusinessService;
    private SpineCodeLookupService mockSpineCodeLookUpService;

    private MedicationBusinessServiceImpl testService;
    private String defaultCodeId;
    private String testLoggedInUserId;

    @Before
    public void setup()
    {
        mockMedicationDao = mock(MedicationDAO.class);
        mockEntityDocumentDao = mock(EntityDocumentDAO.class);
        mockSupplyDao = mock(SupplyDAO.class);
        mockPersonDao = mock(PersonDAO.class);
        mockDemoDao = mock(PatientDemographicsDAO.class);
        mockMedHistoryDao = mock(SureScriptsMedHistoryDAO.class);
        mockOrgUserViewDao = mock(OrganizationUserViewDAO.class);
        mockProviderService = mock(ProviderBusinessServiceImpl.class);
        mockPharmacyService = mock(PharmacyBusinessServiceImpl.class);
        mockCodeLookupBusinessService = mock(CodeLookupBusinessServiceImpl.class);
        mockEntityAliasDao = mock(EntityAliasDAO.class);
        mockEntityAssociationLoader = mock(EntityAssociationLoader.class);
        mockSurescriptsMessageService = mock(SurescriptsMessageBusinessServiceImpl.class);
        mockMedicationDTOTransformer = mock(MedicationToMedicationDTOTransformer.class);
        mockExternalPharmacyDirectoryService = mock(ExternalPharmacyDirectoryBusinessServiceImpl.class);
        mockAuthorityBusinessService = mock(AuthorityBusinessServiceImpl.class);
        mockMedicationHistoryBusinessService = mock(MedicationHistoryBusinessServiceImpl.class);
        mockUserBusinessService = mock(UserBusinessServiceImpl.class);
        mockSpineCodeLookUpService = mock(SpineCodeLookupService.class);


        // setup the codemanager mock to return a dummy code ID for things we don't really care about isolating
        defaultCodeId = "2342";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class), anyString(),
                        anyBoolean())).thenReturn(defaultCodeId);

        // mock out the DAO to send us back a fake merged medication, mimicking the EntityManager.merge() call in DAO's.
        when(mockMedicationDao.saveWithAssociations((Medication) anyObject())).thenAnswer(
                mockDbOneSaveReturningInputArgument());

        // setup a logged in user.
        testLoggedInUserId = "testLoggedInUserId123";
        BusinessServiceTestUtils.setupLoggedInUser(testLoggedInUserId, null);

        testService = new MedicationBusinessServiceImpl(mockMedicationDao,
                mockEntityDocumentDao, mockSupplyDao, mockPersonDao, mockDemoDao,
                mockMedHistoryDao, mockOrgUserViewDao, mockProviderService,
                mockPharmacyService, mockCodeLookupBusinessService, mockEntityAliasDao, 
                mockSurescriptsMessageService, mockMedicationDTOTransformer,
                mockExternalPharmacyDirectoryService, mockAuthorityBusinessService,
                mockMedicationHistoryBusinessService, mockSpineCodeLookUpService);
        testService.setUserBusinessService(mockUserBusinessService);
        testService.setEntityAssociationLoader(mockEntityAssociationLoader);
    }

    @Test
    public void createNewRxMedicationMapsFieldsCorrectly() {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aNewRxDTO().build();
        testMedicationDto.setDntfRefillMessageId("dntf123");

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        // setup a test Pharmacy to test association
        Organization testPharmacy = new Organization();
        String testPharmacyId = "testOrgId";
        testPharmacy.setPrimaryId(testPharmacyId);
        when(mockPharmacyService.updateOrCreateEprescribePharmacy(eq(testMedicationDto.getPharmacy()), eq(testDataProvider))).thenReturn(
                testPharmacy);

        // setup a surescripts message to return
        SurescriptsMessage testSSMessage = SurescriptsMessageBuilder.aBasicSurescriptsMessage().build();
        when(mockSurescriptsMessageService.getById(testMedicationDto.getDntfRefillMessageId())).thenReturn(testSSMessage);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication.getDntfRefillMessageId(), is(testSSMessage));
        assertFalse(resultMedication.getControlledSubstance());
        assertEquals(resultMedication.getMedicationType(), testMedicationDto.getMedicationType().name());

        /*
         * Eprescribe meds call different pharmacy logic
         */
        verify(mockPharmacyService).updateOrCreateEprescribePharmacy(testMedicationDto.getPharmacy(), testDataProvider);
    }

    @Test
    public void createNewRxMedicationMapsFieldsCorrectly_WhenMedIsControlledSubstance() {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aNewRxDTO().asAControlledSubstance().build();
        testMedicationDto.setDntfRefillMessageId("dntf123");

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        // setup a test Pharmacy to test association
        Organization testPharmacy = new Organization();
        String testPharmacyId = "testOrgId";
        testPharmacy.setPrimaryId(testPharmacyId);
        when(mockPharmacyService.updateOrCreateEprescribePharmacy(eq(testMedicationDto.getPharmacy()), eq(testDataProvider))).thenReturn(
                testPharmacy);

        // setup a surescripts message to return
        SurescriptsMessage testSSMessage = SurescriptsMessageBuilder.aBasicSurescriptsMessage().build();
        when(mockSurescriptsMessageService.getById(testMedicationDto.getDntfRefillMessageId())).thenReturn(testSSMessage);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertTrue(resultMedication.getControlledSubstance());

        /*
         * Eprescribe meds call different pharmacy logic
         */
        verify(mockPharmacyService).updateOrCreateEprescribePharmacy(testMedicationDto.getPharmacy(), testDataProvider);
    }

    @Test
    public void createNewRxMedicationWithInvalidDNTFMessageFails() {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aNewRxDTO().build();
        testMedicationDto.setDntfRefillMessageId("dntf123");

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        // setup a test Pharmacy to test association
        Organization testPharmacy = new Organization();
        String testPharmacyId = "testOrgId";
        testPharmacy.setPrimaryId(testPharmacyId);
        when(mockPharmacyService.updateOrCreateEprescribePharmacy(eq(testMedicationDto.getPharmacy()), eq(testDataProvider))).thenReturn(
                testPharmacy);

        // setup a surescripts message to return null
        when(mockSurescriptsMessageService.getById(testMedicationDto.getDntfRefillMessageId())).thenReturn(null);

        // when
        Exception resultException = null;

        try {
            testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                    testEncounter);
        }
        catch (Exception e) {
            resultException = e;
        }

        // then
        assertThat(resultException, instanceOf(PhoenixException.class));
        assertThat(resultException.getMessage(), containsString("SurescriptsMessage not found"));

    }

    @Test
    public void createMedicationHistoryMedicationMapsFieldsCorrectly() throws ParseException
    {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();

        // for things we _do_ care about asserting on codes, setup the mock to return them.
        String testActiveLifecycleCodeId = "activelifecyclecodeid";
        String testNDCCodeId = "NDCCodeId";
        String testDoseUnitCodeId = "doseCodeId";
        String testMedStartedCodeId = "medStarted";
        String testMedEndedCodeId = "medEnded";
        String testEventMoodCodeId = "eventMoodCodeId";
        String testOrderMoodCodeId = "orderMoodCodeId";

        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), anyBoolean())).thenReturn(testMedStartedCodeId);
        when(mockSpineCodeLookUpService.getDefaultMoodCodeId()).thenReturn(testEventMoodCodeId);

        when(mockSpineCodeLookUpService.moodCodeTitleToCodeId(CodeSetConstants.MOOD_CODE.ORDER_NM)).thenReturn(
                testOrderMoodCodeId);
        when(mockSpineCodeLookUpService.medicationStatusTitleToCodeId(CodeSetConstants.MEDICATION_STATUS.ACTIVE_NM))
                .thenReturn(testActiveLifecycleCodeId);

        when(mockCodeLookupBusinessService.findCodeIdByMnemonic((String) anyString(), (CodeDTO) anyObject()))
        .thenReturn(testNDCCodeId);

        when(mockCodeLookupBusinessService.getCodeId(any(CodeDTO.class), any(CodeSetEnum.class), eq(false)))
                .thenReturn(testDoseUnitCodeId);

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        // setup a test Pharmacy to test association
        Organization testPharmacy = new Organization();
        String testPharmacyId = "testOrgId";
        testPharmacy.setPrimaryId(testPharmacyId);
        when(mockPharmacyService.findOrCreateEPrescribePharmacy((OrganizationDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPharmacy);

        when(mockCodeLookupBusinessService.getCodeIdByTitle(anyString(),
                any(CodeSetEnum.class), anyString(), eq(false))).thenReturn(testMedStartedCodeId);

        when(mockSpineCodeLookUpService.getDefaultMoodCodeId()).thenReturn(testEventMoodCodeId);

        when(mockSpineCodeLookUpService.moodCodeTitleToCodeId(CodeSetConstants.MOOD_CODE.ORDER_NM)).thenReturn(
                testOrderMoodCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedStartedCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedEndedCodeId);

        when(mockCodeLookupBusinessService.getCodeId((CodeDTO) anyObject(), any(CodeSetEnum.class), eq(true))).thenReturn(testDoseUnitCodeId);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication, is(not(nullValue())));
        assertThat("the passed in patient should be attached to the medication", resultMedication.getPatient(),
                is(testPatient));
        assertThat("since this is a surescripts-based medication we are not creating entity aliases for the medication", resultMedication
                .getEntityAliasSet().size(), is(1));
        EntityAlias medAlias = resultMedication.getEntityAliasSet().iterator().next();
        assertThat(medAlias.getEntityAlias(), is(testMedicationDto.getPrimaryAlias().getValue()));
        assertThat(medAlias.getEntityAliasNorm(), is(testMedicationDto.getPrimaryAlias().getValue()));
        assertThat(medAlias.getCreatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(medAlias.getUpdatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(resultMedication.getCreatedBy(), is(testLoggedInUserId));
        assertThat(resultMedication.getCreatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(resultMedication.getDomainId(), containsString(testDataProvider.getOrganization().getPrimaryId()));
        assertThat(resultMedication.getDomainId(), containsString(ModuleNames.patientMedications.getName()));
        assertThat(resultMedication.getAdminInstructions(), is(testMedicationDto.getAdministrativeInstructions()));
        assertThat(resultMedication.getDoseValue(), is(testMedicationDto.getDose().getDose()));
        assertThat(resultMedication.getDoseCode(), is(testDoseUnitCodeId));
        assertThat(resultMedication.getNote().getMsgText(), is(testMedicationDto.getNote().getBytes()));
        assertThat(resultMedication.getNumRefills(), is(new BigDecimal(testMedicationDto.getNumberOfRefills())));
        assertThat(resultMedication.getUncertaintyCd(), is(testMedicationDto.getSubstitutionCode().getTitle()));
        assertTrue("test data should set auto inactivate to true", testMedicationDto.getAutoInactivate());
        assertThat(resultMedication.getAutoInactivate(), is("1"));

        assertThat(resultMedication.getMedId(), is(testNDCCodeId));
        assertThat(resultMedication.getNDC(), is(testMedicationDto.getMedicationCode().getMnemonic()));
        assertThat(resultMedication.getFreeText(), is(testMedicationDto.getFreeText()));

        assertThat(resultMedication.getReportedAt(), is(testMedicationDto.getReportedAt().toDateTime("EST")));
        assertThat(resultMedication.getOrderedAt(), is(testMedicationDto.getOrderedAt().toDateTime("EST")));
        assertThat(resultMedication.getEndAt(), is(testMedicationDto.getEndAt().toDateTime("EST")));
        assertThat(resultMedication.getStartAt(), is(testMedicationDto.getStartAt().toDateTime("EST")));

        assertTrue("test data should set PRN indicator to true", testMedicationDto.getPrnIndicator());
        assertThat(resultMedication.getPrn(), is("1"));

        assertThat("An active lifecycle code should be set for this medication",
                resultMedication.getLifeCycleCode(), is(testMedStartedCodeId));

        assertThat("the medication should have the found/created provider associated to it.",
                resultMedication.getOrderedByPerson(), is(testPerson));

        assertThat(
                "Surescripts meds have the 'event' code set to mood code to differentiate them from other types of medications",
                resultMedication.getMoodCode(), is(testOrderMoodCodeId));

        assertThat(resultMedication.getIsTemplate(), is(testEventMoodCodeId));

        Supply resultSupply = resultMedication.getSupply();
        assertThat(resultSupply.getCreatedBy(), is(testLoggedInUserId));
        assertThat(resultSupply.getDomainId(), containsString(testDataProvider.getOrganization().getPrimaryId()));
        assertThat(resultSupply.getDomainId(), containsString(ModuleNames.patientMedications.getName()));
        assertThat(resultSupply.getUpdatedBy(), is(testLoggedInUserId));
        assertThat(resultSupply.getQuantity(), is(testMedicationDto.getSupply().getQuantity()));
        assertThat(resultSupply.getQuantityUnitCode(), is(not(nullValue())));
        assertThat(resultSupply.getRepeatNumber(), is(new BigDecimal(testMedicationDto.getSupply().getRepeatNumber())));

        Set<EntityAlias> resultSupplyAliasSet = resultSupply.getEntityAliasSet();
        assertThat("The supply should have no entity alises", resultSupplyAliasSet.size(), is(0));

        // check pharmacy association
        verify(mockPharmacyService).findOrCreateEPrescribePharmacy(testMedicationDto.getPharmacy(), testDataProvider);
        assertThat(resultMedication.getPharmacyOrganization(), is(testPharmacy));

        // finally, assert that we save the med
        verify(mockMedicationDao).saveWithAssociations(resultMedication);

    }

    @Test
    public void createInterfaceMedicationMapsFieldsCorrectly() throws ParseException
    {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.anInterfaceMedicationDTO().build();

        // for things we _do_ care about asserting on codes, setup the mock to return them.
        String testActiveLifecycleCodeId = "activelifecyclecodeid";
        when(mockSpineCodeLookUpService.medicationStatusTitleToCodeId(CodeSetConstants.MEDICATION_STATUS.ACTIVE_NM))
                .thenReturn(testActiveLifecycleCodeId);

        String testMedCodeId = "medCodeId";
        when(
                mockCodeLookupBusinessService.findCodeIdByMnemonic(
                        eq(NDC_COMPENDIUM.CD_SET_NM), eq(testMedicationDto.getMedicationCode()))).thenReturn(testMedCodeId);

        String testMedStartedCodeId = "medStarted";
        when(
                mockSpineCodeLookUpService.titleToCodeId(MEDICATION_LIFECYCLE.STARTED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedStartedCodeId);

        String testMedEndedCodeId = "medEnded";
        when(
                mockSpineCodeLookUpService.titleToCodeId(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedEndedCodeId);

        String testRouteCodeId = "routeCodeId123";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getRouteCode()),
                        eq(CodeSetEnum.CLINICAL_ROUTE), anyBoolean())).thenReturn(testRouteCodeId);

        String testEventMoodCodeId = "eventMoodCodeId";

        String medicationDoseUnitsCodeId = "medUnitsCodeId";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getDose().getDoseUnit()),
                        eq(CodeSetEnum.MEDICATION_UNITS), anyBoolean())).thenReturn(medicationDoseUnitsCodeId);

        String testQuantityUnitCode = "quanunitcodeid";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getSupply().getQuantityUnitCode()),
                        eq(CodeSetEnum.MEDICATION_UNITS), anyBoolean())).thenReturn(testQuantityUnitCode);

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        when(mockCodeLookupBusinessService.getCodeIdByTitle(anyString(),
                any(CodeSetEnum.class), anyString(), eq(false))).thenReturn(testMedStartedCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedStartedCodeId);
        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedEndedCodeId);

        when(mockSpineCodeLookUpService.getDefaultMoodCodeId()).thenReturn(testEventMoodCodeId);

        String orderNumberAliasCodeId1 = "aliascodeId1";
        when(mockCodeLookupBusinessService.getCodeIdByTitle(anyString(),
               any(CodeSetEnum.class), anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication, is(not(nullValue())));
        assertThat("the passed in patient should be attached to the medication", resultMedication.getPatient(),
                is(testPatient));
        assertThat("one and only one entity alias should be created for a medication", resultMedication
                .getEntityAliasSet().size(), is(1));
        EntityAlias medAlias = resultMedication.getEntityAliasSet().iterator().next();
        assertThat(medAlias.getEntityAlias(), is(testMedicationDto.getPrimaryAlias().getValue()));
        assertThat(medAlias.getEntityAliasNorm(), is(testMedicationDto.getPrimaryAlias().getValue()));
        assertThat(medAlias.getCreatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(medAlias.getUpdatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(resultMedication.getCreatedBy(), is(testLoggedInUserId));
        assertThat(resultMedication.getCreatedSourceId(), is(testDataProvider.getAuthority()));
        assertThat(resultMedication.getDomainId(), containsString(testDataProvider.getOrganization().getPrimaryId()));
        assertThat(resultMedication.getDomainId(), containsString(ModuleNames.patientMedications.getName()));
        assertThat(resultMedication.getDoseValue(), is(testMedicationDto.getDose().getDose()));
        assertThat(resultMedication.getDoseCode(), is(medicationDoseUnitsCodeId));
        assertThat(resultMedication.getNote().getMsgText(), is(testMedicationDto.getNote().getBytes()));
        assertThat(resultMedication.getNumRefills(), is(new BigDecimal(testMedicationDto.getNumberOfRefills())));

        assertThat(resultMedication.getMedId(), is(testMedCodeId));
        assertThat(resultMedication.getFreeText(), is(testMedicationDto.getFreeText()));

        assertThat(resultMedication.getReportedAt(), is(testMedicationDto.getReportedAt().toDateTime("EST")));
        assertThat(resultMedication.getOrderedAt(), is(testMedicationDto.getOrderedAt().toDateTime("EST")));
        assertThat(resultMedication.getEndAt(), is(testMedicationDto.getEndAt().toDateTime("EST")));
        assertThat(resultMedication.getStartAt(), is(testMedicationDto.getStartAt().toDateTime("EST")));

        assertThat(resultMedication.getRouteCode(), is(testRouteCodeId));

        assertThat("the medication should have the provider found/created associated to it.",
                resultMedication.getOrderedByPerson(), is(testPerson));

        assertThat(
                "Interface meds have the 'order' code set to mood code to differentiate them from other types of medications",
                resultMedication.getMoodCode(), is(testEventMoodCodeId));

        assertThat(resultMedication.getIsTemplate(), is(testEventMoodCodeId));

        Supply resultSupply = resultMedication.getSupply();
        assertThat(resultSupply.getCreatedBy(), is(testLoggedInUserId));
        assertThat(resultSupply.getDomainId(), containsString(testDataProvider.getOrganization().getPrimaryId()));
        assertThat(resultSupply.getDomainId(), containsString(ModuleNames.patientMedications.getName()));
        assertThat(resultSupply.getUpdatedBy(), is(testLoggedInUserId));
        assertThat(resultSupply.getQuantity(), is(testMedicationDto.getSupply().getQuantity()));
        assertThat(resultSupply.getQuantityUnitCode(), is(testQuantityUnitCode));
        assertThat(resultSupply.getRepeatNumber(), is(new BigDecimal(testMedicationDto.getSupply().getRepeatNumber())));

        Set<EntityAlias> resultSupplyAliasSet = resultSupply.getEntityAliasSet();
        assertThat("The supply should have no entity alises", resultSupplyAliasSet.size(), is(0));

        // finally, assert that we save the med
        verify(mockMedicationDao).saveWithAssociations(resultMedication);

    }


    @Test
    public void createMedicationWithDifferentEncounterIdThrowsException() throws ParseException
    {
        // given
        Encounter testEncounter = EncounterBuilder.aBasicEncounter().build();
        testEncounter.setPrimaryId("thisIdIsDifferent");
        Medication testMedication = MedicationBuilder.aBasicMedication().withEncounter().build();
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.anInterfaceMedicationDTO().withEncounterId().withMedicationPrimaryId(testMedication.getPrimaryId()).build();
        Patient testPatient = testMedication.getPatient();

        when(mockMedicationDao.find(testMedicationDto.getMedicationPrimaryId())).thenReturn(testMedication);

        // for things we _do_ care about asserting on codes, setup the mock to return them.
        String testActiveLifecycleCodeId = "activelifecyclecodeid";
        when(mockSpineCodeLookUpService.medicationStatusTitleToCodeId(CodeSetConstants.MEDICATION_STATUS.ACTIVE_NM))
                .thenReturn(testActiveLifecycleCodeId);

        String testMedCodeId = "medCodeId";
        when(
                mockCodeLookupBusinessService.findCodeIdByMnemonic(
                        eq(NDC_COMPENDIUM.CD_SET_NM), eq(testMedicationDto.getMedicationCode()))).thenReturn(testMedCodeId);

        String testMedStartedCodeId = "medStarted";
        when(
                mockSpineCodeLookUpService.titleToCodeId(MEDICATION_LIFECYCLE.STARTED_NM, CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedStartedCodeId);

        String testMedEndedCodeId = "medEnded";
        when(
                mockSpineCodeLookUpService.titleToCodeId(MEDICATION_LIFECYCLE.ENDED_NM, CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testMedEndedCodeId);

        String testRouteCodeId = "routeCodeId123";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getRouteCode()),
                        eq(CodeSetEnum.CLINICAL_ROUTE), anyBoolean())).thenReturn(testRouteCodeId);

        String testEventMoodCodeId = "eventMoodCodeId";
        when(mockSpineCodeLookUpService.getDefaultMoodCodeId()).thenReturn(testEventMoodCodeId);

        String medicationDoseUnitsCodeId = "medUnitsCodeId";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getDose().getDoseUnit()),
                        eq(CodeSetEnum.MEDICATION_UNITS), anyBoolean())).thenReturn(medicationDoseUnitsCodeId);

        String testQuantityUnitCode = "quanunitcodeid";
        when(
                mockCodeLookupBusinessService.getCodeId(eq(testMedicationDto.getSupply().getQuantityUnitCode()),
                        eq(CodeSetEnum.MEDICATION_UNITS), anyBoolean())).thenReturn(testQuantityUnitCode);

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        when(mockCodeLookupBusinessService.getCodeIdByTitle(anyString(),
                any(CodeSetEnum.class), anyString(), eq(false))).thenReturn(testMedStartedCodeId);

        when(mockSpineCodeLookUpService.getDefaultMoodCodeId()).thenReturn(testEventMoodCodeId);

        String orderNumberAliasCodeId1 = "aliascodeId1";
        when(mockCodeLookupBusinessService.getCodeIdByTitle(anyString(),
               any(CodeSetEnum.class), anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);

        // when
        PhoenixException phoenixException = null;
        try
        {
            testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                    testEncounter);
        }
        catch(PhoenixException pe)
        {
            phoenixException = pe;
        }

        // then
        assertThat(phoenixException, is(not(nullValue())));
        assertThat(phoenixException.getErrorMessageDto(), is(not(nullValue())));
        assertThat(phoenixException.getErrorMessageDto().getErrorCode(), is(WellogicErrorCodes.REG01.getCode()));

    }



    @Test
    public void whenProcessMedicationIsCalledLifecycleCodeIsSetToValueSpecifiedOnMedicationDTO()
            throws ParseException
    {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        String testStatus = "lifecycleCdTitle1";
        testMedicationDto.setMedicationStatus(testStatus);

        String testLifecycleCodeId = "someRandomLifecycleCodeId";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(eq(testStatus),
                        eq(CodeSetEnum.MEDICATION_STATUS), eq(DEFAULT_WELLOGIC_AUTHORITY), anyBoolean()))
                .thenReturn(testLifecycleCodeId);
        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testLifecycleCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testLifecycleCodeId);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication, is(not(nullValue())));
        assertThat(resultMedication.getLifeCycleCode(), is(testLifecycleCodeId));
    }

    @Test
    public void whenProcessMedicationIsCalledLifecycleCodeIsSetToActiveByDefault()
            throws ParseException
    {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        testMedicationDto.setMedicationStatus(null);

        String testActiveLifecycleCodeId = "activeLIfecycleCodeId";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(eq(CodeSetConstants.MEDICATION_STATUS.ACTIVE_NM),
                        eq(CodeSetEnum.MEDICATION_STATUS), eq(DEFAULT_WELLOGIC_AUTHORITY),
                        anyBoolean())).thenReturn(testActiveLifecycleCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testActiveLifecycleCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testActiveLifecycleCodeId);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication, is(not(nullValue())));
        assertThat(resultMedication.getLifeCycleCode(), is(testActiveLifecycleCodeId));
    }

    @Test
    public void whenANoteIsUpdatedToBlankItIsSetToInactive()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        // override the note to blank
        testMedicationDto.setNote("");

        PatientRxDispensedMedication patientRxDispensedMed = getPatientRxDispensedMed();
        testMedicationDto.setPatientRxDispensedMed(patientRxDispensedMed);

        when(mockMedicationHistoryBusinessService.getPatientRxDispensedMedication(anyString())).thenReturn(
                patientRxDispensedMed);

        // setup the medicationDAO to return an existing medication with an ote
        Medication testMedication = new Medication();
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        Message testNoteMessage = new Message();
        testNoteMessage.setActiveStatusValue(ActiveStatus.ACTIVE);
        testNoteMessage.setPrimaryId("testId");
        testMedication.setNote(testNoteMessage);
        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));
        mockLifecycleCodeLookup();

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication.getNote().getActiveStatusValue(), is(ActiveStatus.INACTIVE));
    }

    @Test
    public void whenAnAdministrativeNoteIsUpdatedToBlankItIsSetToInactive()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        // override the note to blank
        testMedicationDto.setAdministrativeNotes("");

        PatientRxDispensedMedication patientRxDispensedMed = getPatientRxDispensedMed();
        testMedicationDto.setPatientRxDispensedMed(patientRxDispensedMed);

        when(mockMedicationHistoryBusinessService.getPatientRxDispensedMedication(anyString())).thenReturn(
                patientRxDispensedMed);

        // setup the medicationDAO to return an existing medication
        Medication testMedication = new Medication();
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        testMedication.getAdministrativeNotesEntityDocument();
        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        // then return an entity document for the existing med
        EntityDocument testAdminEntityDocument = EntityDocumentBuilder.aBasicEntityDocument().build();
        when(mockEntityDocumentDao.getEntityDocumentByEntityIdAndType(
                testMedication.getPrimaryId(), DBOneModelType.MEDICATION.getTypeCode(), DEFAULT_ENTITY_DOCUMENT_TYP_CD)).thenReturn(testAdminEntityDocument);
        mockLifecycleCodeLookup();

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication.getAdministrativeNotesEntityDocument().getActiveStatusValue(), is(ActiveStatus.INACTIVE));
    }

    @Test
    public void whenAHistoricalMedicationHasUpdatesToNumberOfRefillsTheExistingValueIsKeptIfItIsLower()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        Integer newNumberOfRefills = 6;
        testMedicationDto.setNumberOfRefills(newNumberOfRefills);
        testMedicationDto.getSupply().setRepeatNumber(newNumberOfRefills);

        PatientRxDispensedMedication patientRxDispensedMed = getPatientRxDispensedMed();
        testMedicationDto.setPatientRxDispensedMed(patientRxDispensedMed);

        when(mockMedicationHistoryBusinessService.getPatientRxDispensedMedication(anyString())).thenReturn(
                patientRxDispensedMed);

        // setup the medicationDAO to return an existing medication with
        BigDecimal oldNumberOfRefills = new BigDecimal(5);
        Medication testMedication = new Medication();
        testMedication.setNumRefills(oldNumberOfRefills);
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        Supply testSupply = new Supply();
        testSupply.setRepeatNumber(oldNumberOfRefills);
        testMedication.setSupply(testSupply);

        assertTrue("for this test, the old number of refills should be more than the new number of refills",
                oldNumberOfRefills.doubleValue() < new BigDecimal(newNumberOfRefills).doubleValue() );

        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        when(mockSupplyDao.findSupplyByAlias(anyString(), anyString(), anyString())).thenReturn(testSupply);

        String orderNumberAliasCodeId = "codeId";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(false))).thenReturn(orderNumberAliasCodeId);

        String orderNumberAliasCodeId1 = "codeId1";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);
        mockLifecycleCodeLookup();

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat("the old (lower) value should be set for the number of refills for a historical medicaiton",
                resultMedication.getNumRefills(), is(oldNumberOfRefills));
        assertThat("the old (lower) value should be set for the number of refills for a historical medicaiton",
                resultMedication.getSupply().getRepeatNumber(), is(oldNumberOfRefills));
    }

    @Test
    public void whenAHistoricalMedicationHasUpdatesToNumberOfRefillsTheNewValueIsKeptIfItIsLower()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        Integer newNumberOfRefills = 5;
        testMedicationDto.setNumberOfRefills(newNumberOfRefills);
        testMedicationDto.getSupply().setRepeatNumber(newNumberOfRefills);

        // setup the medicationDAO to return an existing medication with
        BigDecimal oldNumberOfRefills = new BigDecimal(6);
        Medication testMedication = new Medication();
        testMedication.setNumRefills(oldNumberOfRefills);
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        Supply testSupply = new Supply();
        testSupply.setRepeatNumber(oldNumberOfRefills);
        testMedication.setSupply(testSupply);

        assertTrue("for this test, the new number of refills should be more than the ols number of refills",
                oldNumberOfRefills.doubleValue() > (new BigDecimal(newNumberOfRefills)).doubleValue());

        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        when(mockSupplyDao.findSupplyByAlias(anyString(), anyString(), anyString())).thenReturn(testSupply);

        String orderNumberAliasCodeId = "codeId";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(false))).thenReturn(orderNumberAliasCodeId);

        String orderNumberAliasCodeId1 = "codeId1";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);
        mockLifecycleCodeLookup();

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        BigDecimal newNumberOfRefillsBigDec = new BigDecimal(newNumberOfRefills);
        assertThat("the new (lower) value should be set for the number of refills for a historical medicaiton",
                resultMedication.getNumRefills(), is(newNumberOfRefillsBigDec));
        assertThat("the new (lower) value should be set for the number of refills for a historical medicaiton",
                resultMedication.getSupply().getRepeatNumber(), is(newNumberOfRefillsBigDec));
    }

    @Test
    public void whenAHistoricalMedicationHasUpdatesToEndAtTheExistingValueIsKeptWhenItIsMoreRecent()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        DateTime newEndAtDate = new DateTime(2013, 1, 1, 0, 0, 0);
        String newEndAtDateString = DateTimeDTOTest.TEST_DATE_FORMATTER.print(newEndAtDate);
        testMedicationDto.setEndAt(new DateTimeDTO(newEndAtDateString));

        PatientRxDispensedMedication patientRxDispensedMed = getPatientRxDispensedMed();
        testMedicationDto.setPatientRxDispensedMed(patientRxDispensedMed);

        when(mockMedicationHistoryBusinessService.getPatientRxDispensedMedication(anyString())).thenReturn(
                patientRxDispensedMed);

        // setup the medicationDAO to return an existing medication with
        DateTime existingDateTime = new DateTime(2013, 2, 1, 0, 0, 0);
        Medication testMedication = new Medication();
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        testMedication.setEndAt(existingDateTime.toDate());

        assertTrue("for this test, the existing medication's date should be later than the new one",
                existingDateTime.isAfter(newEndAtDate));

        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        // need to be more specific about the
        String testEndedCodeId = "endedId";
        when( mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testEndedCodeId);
        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testEndedCodeId);

        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(false))).thenReturn(testEndedCodeId);

        String orderNumberAliasCodeId1 = "codeId1";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication.getEndAt(), is(existingDateTime.toDate()));

    }

    @Test
    public void whenAHistoricalMedicationHasUpdatesToEndAtTheNewValueIsKeptWhenItIsMoreRecent() throws ParseException
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        DateTime newEndAtDate = new DateTime(2013, 2, 2, 0, 0, 0);
        String newEndAtDateString = DateTimeDTOTest.TEST_DATE_FORMATTER.print(newEndAtDate);
        DateTimeDTO newEndAtDateDTO = new DateTimeDTO(newEndAtDateString);
        testMedicationDto.setEndAt(newEndAtDateDTO);

        // setup the medicationDAO to return an existing medication with
        DateTime existingDateTime = new DateTime(2013, 1, 1, 0, 0, 0);
        Medication testMedication = new Medication();
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);
        testMedication.setEndAt(existingDateTime.toDate());

        assertTrue("for this test, the existing medication's date should be older than the new one",
                existingDateTime.isBefore(newEndAtDate));

        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        // need to be more specific about the
        String testEndedCodeId = "endedId";
        when(
                mockSpineCodeLookUpService.titleToCodeId(MEDICATION_LIFECYCLE.ENDED_NM, CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testEndedCodeId);

        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(false))).thenReturn(testEndedCodeId);

        String orderNumberAliasCodeId1 = "codeId1";
        when(
                mockCodeLookupBusinessService.getCodeIdByTitle(anyString(), any(CodeSetEnum.class),
                        anyString(), eq(true))).thenReturn(orderNumberAliasCodeId1);
        mockLifecycleCodeLookup();

        // when
        testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient, testEncounter);

        // then
        // TODO PHX-6259 confirm that this time conversion works as expected
        //Date expectedDate = newEndAtDateDTO.toDateTime(testDataProvider.getTimezone());
        //assertThat(resultMedication.getEndAt(), is(newEndAtDate.toDate()));
    }

    @Test
    public void whenBothExistingAndNewRefillAmountsAreNull_NullIsCorrectRefillAmount()
    {
        // given
        BigDecimal newRefilAmount = null;
        BigDecimal existingRefillAmount = null;
        boolean isHistoricalmedication = true;

        // when
        BigDecimal result = (BigDecimal) ReflectionUtils.invokeMethod(findGetRefillNumberMethod(), testService,
                isHistoricalmedication, existingRefillAmount, newRefilAmount);

        // then
        assertThat(result, is(nullValue()));
    }

    @Test
    public void whenExistingRefillAmountIsNullAndNewValueIsSent_NewValueIsCorrectRefillAmount()
    {
        // given
        BigDecimal newRefilAmount = new BigDecimal(6);
        BigDecimal existingRefillAmount = null;
        boolean isHistoricalmedication = true;

        // when
        BigDecimal result = (BigDecimal) ReflectionUtils.invokeMethod(findGetRefillNumberMethod(), testService,
                isHistoricalmedication, existingRefillAmount, newRefilAmount);

        // then
        assertThat(result, is(newRefilAmount));
    }

    @Test
    public void whenNewRefillAmountIsNullAndExistingValueIsSent_NewValueIsCorrectRefillAmount()
    {
        // given
        BigDecimal newRefilAmount = null;
        BigDecimal existingRefillAmount = new BigDecimal(6);
        boolean isHistoricalmedication = true;

        // when
        BigDecimal result = (BigDecimal) ReflectionUtils.invokeMethod(findGetRefillNumberMethod(), testService,
                isHistoricalmedication, existingRefillAmount, newRefilAmount);

        // then
        assertThat(result, is(newRefilAmount));
    }

    @Test
    public void whenNewRefillAmountIsLessThanExistingValueIsSent_NewValueIsCorrectRefillAmount()
    {
        // given
        BigDecimal newRefilAmount = new BigDecimal(5);
        BigDecimal existingRefillAmount = new BigDecimal(6);
        boolean isHistoricalmedication = true;

        // when
        BigDecimal result = (BigDecimal) ReflectionUtils.invokeMethod(findGetRefillNumberMethod(), testService,
                isHistoricalmedication, existingRefillAmount, newRefilAmount);

        // then
        assertThat(result, is(newRefilAmount));
    }

    @Test
    public void whenNewRefillAmountIsMoreThanExistingValueIsSent_ExistingValueIsCorrectRefillAmount()
    {
        // given
        BigDecimal newRefilAmount = new BigDecimal(6);
        BigDecimal existingRefillAmount = new BigDecimal(5);
        boolean isHistoricalmedication = true;

        // when
        BigDecimal result = (BigDecimal) ReflectionUtils.invokeMethod(findGetRefillNumberMethod(), testService,
                isHistoricalmedication, existingRefillAmount, newRefilAmount);

        // then
        assertThat(result, is(existingRefillAmount));
    }

    @Test
    public void whenBothDatesArePassedAsNull_NullIsReturnedForLatestDate()
    {
        // given
        Date newDate = null;
        Date existingDate = null;

        // when
        Date result = (Date) ReflectionUtils.invokeMethod(findGetLatestMedicationDateMethod(), testService, true,
                existingDate, newDate);

        // then
        assertThat(result, is(nullValue()));
    }

    @Test
    public void whenTheExistingDateIsLaterThanTheNewDate_TheExistingDateIsReturned()
    {
        // given
        Date newDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
        Date existingDate = new DateTime(2013, 1, 2, 0, 0, 0, 0).toDate();

        // when
        Date result = (Date) ReflectionUtils.invokeMethod(findGetLatestMedicationDateMethod(), testService, true,
                existingDate, newDate);

        // then
        assertThat(result, is(existingDate));
    }

    @Test
    public void whenTheExistingDateIsBeforeTheNewDate_TheNewDateIsReturned()
    {
        // given
        Date newDate = new DateTime(2013, 1, 3, 0, 0, 0, 0).toDate();
        Date existingDate = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();

        // when
        Date result = (Date) ReflectionUtils.invokeMethod(findGetLatestMedicationDateMethod(), testService, true,
                existingDate, newDate);

        // then
        assertThat(result, is(newDate));
    }

    @Test(expected = PhoenixException.class)
    public void whenBothAPrimaryAliasDTO_AndAnAliasStringArePassedAnExceptionIsThrown()
    {
        // given
        String testPatientId = "testpat1";
        Patient testPatient = new Patient();
        testPatient.setPrimaryId(testPatientId);
        testPatient.setMpi("mpi");
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aMedicationHistory8_1DTO().build();
        testMedicationDto.setAlias("whatever");
        assertThat("This test needs both an alias and primary alias set", testMedicationDto.getPrimaryAlias()
                .getValue(), is(not(nullValue())));

        // setup the medicationDAO to return an existing medication with
        Medication testMedication = new Medication();
        testMedication.setPrimaryId("testId");
        testMedication.setPatient(testPatient);

        when(mockMedicationDao.findMedicationsByAlias(anyString(), anyString(), anyString())).thenReturn(Arrays.asList(testMedication));

        // when
        testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient, testEncounter);

        // then
        // explosions!
    }

    @Test
    public void testMedicationLifecycleCodeUpdate_WhenSuccessful() throws ParseException
    {
        // given
        Medication testMedication = MedicationBuilder.aBasicMedication().build();
        String lifeCycleCodeTitle = CodeSetConstants.MEDICATION_STATUS.INITIATED_NM;

        // when
        testService.updateMedicationLifecycleCode(testMedication, lifeCycleCodeTitle);

        // then
        assertThat(testMedication, is(not(nullValue())));
        assertThat(testMedication.getLifeCycleCode(), is(not(nullValue())));
        assertThat(testMedication.getLifeCycleCode(), is(defaultCodeId));
    }

    @Test
    public void testMedicationLifecycleCodeUpdate_WhenNullStatusProvided() throws ParseException
    {
        // given
        Medication testMedication = MedicationBuilder.aBasicMedication().build();
        String lifeCycleCodeTitle = null;

        // when
        testService.updateMedicationLifecycleCode(testMedication, lifeCycleCodeTitle);

        // then verify lifecycle code has not changed.
        assertThat(testMedication, is(not(nullValue())));
        assertThat(testMedication.getLifeCycleCode(), is(nullValue()));

    }

    /*
     * Tests getMedicationsWithAssociatedEntities() throws exception when called with null ID list
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetMedsWithLightweightEntitiesWithEmptyIDList()
    {

        List<String> emptyList = new ArrayList<String>();
        testService.getMedicationWithLightweightAssociatedEntities(emptyList);

    }

    /*
     * Tests get medications by med Ids fills in associates entities and returns correct number of medications
     */
    @Test
    public void testGetMedsWithLightweightAssocEntities()
    {

        List<String> medIds = Arrays.asList("111", "222");
        Medication medication1 = MedicationBuilder.aBasicMedication().build();
        Medication medication2 = MedicationBuilder.aBasicMedication().build();

        List<Medication> medications = Arrays.asList(medication1, medication2);
        when(mockMedicationDao.findMedicationsByIds(medIds)).thenReturn(medications);

        List<Medication> actualMedications = testService.getMedicationWithLightweightAssociatedEntities(medIds);

        assertEquals(medications.size(), actualMedications.size());
        verify(mockEntityDocumentDao, times(2)).getEntityDocumentByEntityIdAndType((String) anyObject(),
                (String) anyObject(), (String) anyObject());

        // we should call the load on the organization entity
        verify(mockEntityAssociationLoader, times(2)).loadEntityAssociation(any(Organization.class), any(DBOneModelType.class));

    }


    @Test
    public void testMedicationLookupForPrimaryId_IsNotFound_ExceptionIsThrown() throws ParseException
    {
        // given
        MedicationDTO medicationDto = MedicationDTOBuilder.aNewRxDTO().build();
        String medicationId = "medicationId123";
        medicationDto.setMedicationPrimaryId(medicationId);

        when(mockMedicationDao.find(medicationId)).thenReturn(null);

        // when
        PhoenixException pe = null;
        try
        {
            testService.lookupMedication((DataProvider) anyObject(), medicationDto);
        }
        catch (PhoenixException e)
        {
            pe = e;
        }

        assertThat(pe, is(not(nullValue())));
        assertThat(pe.getErrorMessageDto().getErrorCode(), is(WellogicErrorCodes.SS08.getCode()));
    }

    @Test
    public void whenInactivateMedicationWithAssociationsIsCalledEverythingIsInactivated() {
        // given
        Medication testEntity = MedicationBuilder.aBasicMedication().build();
        testEntity.setAdministrativeNotesEntityDocument(new EntityDocument());
        testEntity.setNote(new Message());
        Set<EntityAlias> aliases = new HashSet<EntityAlias>();
        aliases.add(EntityAliasBuilder.aBasicEntityAliasBuilder().build());
        aliases.add(EntityAliasBuilder.aBasicEntityAliasBuilder().build());
        testEntity.setEntityAliasCollection(aliases);
        assertThat(testEntity.getEntityAliasSet().size(), is(2));

        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        String testUpdatedBy = "upd123";

        // when
        testService.inactivateMedicationAndAssociatedEntities(testEntity, testDataProvider, testUpdatedBy);

        // then
        EntityBaseTest.assertEntityBaseIsInactive(testEntity, testDataProvider, testUpdatedBy);

        EntityBaseTest.assertEntityBaseIsInactive(testEntity.getSupply(), testDataProvider, testUpdatedBy);

        EntityBaseTest.assertEntityBaseIsInactive(testEntity.getNote(), testDataProvider, testUpdatedBy);

        EntityBaseTest.assertEntityBaseIsInactive(testEntity.getAdministrativeNotesEntityDocument(), testDataProvider, testUpdatedBy);

        for(EntityAlias entity : testEntity.getEntityAliasSet() ) {
            EntityBaseTest.assertEntityBaseIsInactive(entity, testDataProvider, testUpdatedBy);
        }

    }

    @Test(expected = PhoenixException.class)
    public void whenInactivateMedicationsWithAssociationsIsCalledAndOneMedicationIsntFoundAnExceptionIsThrown()
    {
        // given
        Medication testMed1 = MedicationBuilder.aBasicMedication().build();
        String aNotFoundMedId = "notfound123";

        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        String testUpdatedBy = "upd123";

        when(mockMedicationDao.find(testMed1.getPrimaryId())).thenReturn(testMed1);
        when(mockMedicationDao.find(aNotFoundMedId)).thenReturn(null);

        // when
        testService.inactivateMedicationsAndAssociatedEntitiesAndSave(
                Arrays.asList(testMed1.getPrimaryId(), aNotFoundMedId), testDataProvider, testUpdatedBy);

        // then
        // explosions
    }

    @Test
    public void getMedicationDtoRetrievesAMedicationAndCallsTheTransformer() {
        // given
        String medId = "testmedid123";
        Medication testMed = MedicationBuilder.aBasicMedication().build();
        when(mockMedicationDao.find(medId)).thenReturn(testMed);

        // when
        testService.getMedicationDtoWithoutPrescriber(medId);

        // then
        verify(mockMedicationDTOTransformer).transformWithoutPrescriber(testMed);
    }

    @Test
    public void getMedicationDtoWithPharmacyLookupCallsExternalPharmacyService()
    {
        // given
        Medication testMed = MedicationBuilder.aBasicMedication().build();
        MedicationDTO testMedDTo = MedicationDTOBuilder.aNewRxDTO().build();
        when(mockMedicationDTOTransformer.transformWithoutPrescriber(testMed)).thenReturn(testMedDTo);

        PharmacyOrganizationDTO returnedPharmOrg = PharmacyOrganizationDTOBuilder.aBasicPharmacyOrganization().build();
        when(mockExternalPharmacyDirectoryService.getPharmacySearchResultByNcpdpId(testMedDTo.getPharmacy().getPrimaryAlias().getValue())).thenReturn(returnedPharmOrg);

        // when
        testService.getMedicationDtoWithoutPrescriberIncludingPharmacyLookup(testMed);

        // then
        verify(mockExternalPharmacyDirectoryService).getPharmacySearchResultByNcpdpId(testMedDTo.getPharmacy().getPrimaryAlias().getValue());
        // we should be internally creating an instance of PharmacyOrganizationDTO
        PharmacyOrganizationDTO resultPharmOrgDto = (PharmacyOrganizationDTO)testMedDTo.getPharmacy();
        assertThat(resultPharmOrgDto.getFax(), is(returnedPharmOrg.getFax()));
        assertThat(resultPharmOrgDto.getSpecialties(), is(returnedPharmOrg.getSpecialties()));
    }

    @Test
    public void markAsPrintedSetsPrintedByAndDateAndSaves() {
        // given
        Medication testMed = MedicationBuilder.aBasicMedication().build();
        Date testPrintedDate = new DateTime(2013, 1, 1, 0, 0,0).toDate();
        Person testPrintedBy = PersonBuilder.aBasicProvider().build();


        // when
        testService.markMedicationAsPrinted(testMed, testPrintedDate, testPrintedBy);

        // then
        ArgumentCaptor<Medication> medCaptor = ArgumentCaptor.forClass(Medication.class);
        verify(mockMedicationDao).save(medCaptor.capture());
        Medication savedMed = medCaptor.getValue();
        assertThat("Saved med should be same as passed in", savedMed, is(testMed));
        assertThat(savedMed.getLastPrintedAt(), is(testPrintedDate));
        assertThat(savedMed.getLastPrintedBy(), is(testPrintedBy));

    }

    @Test
    public void updateMedicationNoOfRefillsToNullCorrectly() {
        // given
        Patient testPatient = new Patient();
        Encounter testEncounter = null;
        DataProvider testDataProvider = DataProviderBuilder.aBasicFilledOutDataProvider().build();
        MedicationDTO testMedicationDto = MedicationDTOBuilder.aNewRxDTO().build();
        testMedicationDto.setNumberOfRefills(null);

        // setup a test Provider person to test associations
        Person testPerson = new Person();
        String testPersonId = "tespid";
        testPerson.setPrimaryId(testPersonId);
        when(mockProviderService.findOrCreateProvider((PersonDTO) anyObject(), eq(testDataProvider))).thenReturn(
                testPerson);

        // setup a test Pharmacy to test association
        Organization testPharmacy = new Organization();
        String testPharmacyId = "testOrgId";
        testPharmacy.setPrimaryId(testPharmacyId);
        when(mockPharmacyService.updateOrCreateEprescribePharmacy(eq(testMedicationDto.getPharmacy()), eq(testDataProvider))).thenReturn(
                testPharmacy);

        // setup a surescripts message to return
        SurescriptsMessage testSSMessage = SurescriptsMessageBuilder.aBasicSurescriptsMessage().build();
        when(mockSurescriptsMessageService.getById(testMedicationDto.getDntfRefillMessageId())).thenReturn(testSSMessage);

        // when
        Medication resultMedication = testService.processMedicationInNewTransaction(testDataProvider, testMedicationDto, testPatient,
                testEncounter);

        // then
        assertThat(resultMedication.getNumRefills(), is(nullValue()));
    }

    @Test
    public void testFindMedicationsByPatIdAndVisitId()
    {
        List<Medication> medications = new ArrayList<Medication>();
        medications.add(MedicationBuilder.aBasicMedication().build());
        when(mockMedicationDao.findMedicationsByPatIdAndVisitId(anyString(),anyString(), anyString())).thenReturn(medications);
        List<Medication> meds = testService.findMedicationsByPatIdAndVisitId(RandomStringUtils.random(5), RandomStringUtils.random(5));
        assertTrue(isNotEmpty(meds));
    }

    private Method findGetLatestMedicationDateMethod()
    {
        Method method = ReflectionUtils
                .findMethod(MedicationBusinessServiceImpl.class, "getLatestMedicationDate", (Class<?>[]) null);

        method.setAccessible(true);
        return method;
    }

    private Method findGetRefillNumberMethod()
    {
        Method method = ReflectionUtils.findMethod(MedicationBusinessServiceImpl.class, "getRefillNumber", (Class<?>[]) null);

        method.setAccessible(true);
        return method;
    }

    private PatientRxDispensedMedication getPatientRxDispensedMed()
    {
        PatientRxDispensedMedicationBuilder dispensedMedBuilder = new PatientRxDispensedMedicationBuilder();
        dispensedMedBuilder.contents("testContent");
        PatientRxDispensedMedication dispensedMed = dispensedMedBuilder.build();
        return dispensedMed;
    }

    private void mockLifecycleCodeLookup()
    {
        String testActiveLifecycleCodeId = "aLifeCycleCdId";
        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.STARTED_NM,
                CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testActiveLifecycleCodeId);

        when(mockSpineCodeLookUpService.titleToCodeIdReq(MEDICATION_LIFECYCLE.ENDED_NM,
                        CodeSetEnum.MEDICATION_LIFECYCLE)).thenReturn(testActiveLifecycleCodeId);
    }
}
