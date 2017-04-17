package com.twilio.callforwarding.controllers;

import com.twilio.callforwarding.models.Senator;
import com.twilio.callforwarding.models.State;
import com.twilio.callforwarding.models.Zipcode;
import com.twilio.callforwarding.repositories.SenatorRepository;
import com.twilio.callforwarding.repositories.StateRepository;
import com.twilio.callforwarding.repositories.ZipcodeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import spark.Request;
import spark.Response;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CallCongressControllerTest {

    private CallCongressController subject;

    @Mock
    private StateRepository mockStateRepository;
    @Mock
    private ZipcodeRepository mockZipcodeRepository;
    @Mock
    private SenatorRepository mockSenatorRepository;
    @Mock
    private Request mockRequest;
    @Mock
    private Response mockResponse;

    @Before
    public void setup() {
        subject = new CallCongressController(mockSenatorRepository, mockStateRepository, mockZipcodeRepository);
    }

    @Test
    public void testWelcomeRouteTriggerLookupStateWhenNoFromStateParameter() throws Exception {
        // when
        String twiml = (String) subject.welcomeRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("Thank you for calling Call Congress! If you wish to")));
        assertThat(xml, hasXPath("//Response/Gather/@action",
                equalTo("/callcongress/state-lookup")));
        assertThat(xml, hasXPath("//Response/Gather/@method",
                equalTo("POST")));
    }

    @Test
    public void testWelcomeRouteTriggerSetStateWhenFromStateParameterIsPresent() throws Exception {
        // given
        when(mockRequest.params("FromState")).thenReturn("STATE");

        // when
        String twiml = (String) subject.welcomeRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("Thank you for calling congress! It looks like")));
        assertThat(xml, hasXPath("//Response/Gather/@action",
                equalTo("/callcongress/set-state")));
        assertThat(xml, hasXPath("//Response/Gather/@method",
                equalTo("POST")));
    }

    @Test
    public void testCollectZipRouteTriggersStateLookup() throws Exception {
        // when
        String twiml = (String) subject.collectZipRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("If you wish to call your senators, please")));
        assertThat(xml, hasXPath("//Response/Gather/@action",
                equalTo("/callcongress/state-lookup")));
        assertThat(xml, hasXPath("//Response/Gather/@method",
                equalTo("POST")));
    }

    @Test
    public void testStateLookupRouteRedirectsToCallSenators() throws Exception {
        // given
        Zipcode zipcode = new Zipcode(12345,"PR");
        when(mockZipcodeRepository.getFirstResultFilteredByZipcode("12345")).thenReturn(zipcode);
        when(mockRequest.queryParams("Digits")).thenReturn("12345");

        // when
        subject.stateLookupRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).redirect("/callcongress/call-senators/PR");
    }

    @Test
    public void testSetStateRouteRedirectToCallSenatorsWhenParametersIs1() throws Exception {
        // given
        when(mockRequest.params("Digits")).thenReturn("1");
        when(mockRequest.params("CallerState")).thenReturn("PR");

        // when
        subject.setStateRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).redirect("/callcongress/call-senators/PR");
    }

    @Test
    public void testSetStateRouteRedirectToCollectZipcodeWhenParametersIs2() throws Exception {
        // given
        when(mockRequest.params("Digits")).thenReturn("2");

        // when
        subject.setStateRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).redirect("/callcongress/collect-zip");
    }

    @Test
    public void testCallSenatorsRouteTriggersCallToFirstSenator() throws Exception {
        // given
        when(mockRequest.params("state")).thenReturn("STATE");
        State state = new State().withSenators(Arrays.asList(
                new Senator("senator1", "phone1", null),
                new Senator(1L,"senator2", "phone2", null)
        ));
        when(mockStateRepository.findByStateName("STATE")).thenReturn(state);

        // when
        String twiml = (String) subject.callSenatorsRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("Connecting you to senator1")));
        assertThat(xml, hasXPath("//Response/Dial",
                equalTo("phone1")));
        assertThat(xml, hasXPath("//Response/Dial/@action",
                equalTo("/callcongress/call-second-senator/1")));
    }

    @Test
    public void testCallSecondSenatorsRouteTriggersCallToSecondSenator() throws Exception {
        // given
        when(mockRequest.params("senatorId")).thenReturn("123");
        Senator senator = new Senator(1L, "senator", "phone", null);
        when(mockSenatorRepository.find(123)).thenReturn(senator);

        // when
        String twiml = (String) subject.callSecondSenatorRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("Connecting you to senator")));
        assertThat(xml, hasXPath("//Response/Dial",
                equalTo("phone")));
        assertThat(xml, hasXPath("//Response/Dial/@action",
                equalTo("/callcongress/goodbye")));
    }

    @Test
    public void testGoodbyeRouteTriggersHangupCall() throws Exception {
        // when
        String twiml = (String) subject.goodbyeRoute.handle(mockRequest, mockResponse);

        // then
        verify(mockResponse).type("application/xml");
        Document xml = parse(twiml);
        assertThat(xml, hasXPath("//Response/Say",
                containsString("Your voice makes a difference. Goodbye.")));
        assertThat(xml, hasXPath("//Response/Hangup"));
    }

    private static Document parse(String xml) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new ByteArrayInputStream(xml.getBytes()));
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}