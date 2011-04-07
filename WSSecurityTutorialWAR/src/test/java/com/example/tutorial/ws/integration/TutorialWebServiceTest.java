/*
 * Created on Nov 4, 2010
 */
package com.example.tutorial.ws.integration;

import static org.testng.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.xml.ws.Holder;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.example.tutotial.MessageAcknowledgment;
import com.example.tutotial.MessageSource;
import com.example.tutotial.RequiredHeaderMissingFault;
import com.example.tutotial.SystemUnavailableFault;
import com.example.tutotial.TutorialRequest;
import com.example.tutotial.TutorialResponse;
import com.example.tutotial.TutorialWebService;
import com.example.tutotial.types.RequestStructure;

/**
 * Integration tests (i.e. tests that must be run against a running server).  Uses Spring's test framework,
 * but, using another method of context loading (e.g. via web.xml or hard-coding) serves as an example
 * of how to write a CXF client for these services.
 *
 * @author Ross M. Lodge
 */
@ContextConfiguration(locations = {
        "classpath*:/com/**/war-config.xml",
        "classpath*:/META-INF/cxf/cxf.xml",
        "classpath*:/META-INF/cxf/cxf-extension-soap.xml",
        "classpath*:/META-INF/cxf/cxf-extension-http.xml",
        "classpath*:/com/**/war-spring-test.xml"
})
public class TutorialWebServiceTest extends AbstractTestNGSpringContextTests
{
	
	/**
	 * @return
	 */
	public TutorialWebService getPort() {
		return applicationContext.getBean("testClient", TutorialWebService.class);
	}
	
	/**
	 * @throws URISyntaxException 
	 * @throws SystemUnavailableFault 
	 * @throws RequiredHeaderMissingFault 
	 * 
	 */
	@Test(groups = "remote-integration")
	public void basicTest() throws URISyntaxException, RequiredHeaderMissingFault, SystemUnavailableFault {
		TutorialRequest request = getRequest();
		
		MessageSource source = getMessageSource();
		
		Holder<TutorialResponse> responseHolder = new Holder<TutorialResponse>();
		Holder<MessageAcknowledgment> acknowledgmentHolder = new Holder<MessageAcknowledgment>();
		getPort().sendTutorialMessage(request, source, responseHolder, acknowledgmentHolder);
		
		TutorialResponse response = responseHolder.value;
		assertNotNull(response);
		assertNotNull(response.getResponseStructure());
		assertEquals(response.getResponseStructure().getResponseCode(), "00000");
		assertEquals(response.getResponseStructure().getResponseMessage(), "SUCCESS!");
		assertNotNull(response.getResponseStructure().getServerDate());
		
		MessageAcknowledgment acknowledgment = acknowledgmentHolder.value;
		assertEquals(acknowledgment.getSomeMessage(), "WE ACKNOWLEDGE!");
	}

	/**
	 * @throws SystemUnavailableFault 
	 * @throws RequiredHeaderMissingFault 
	 * @throws URISyntaxException 
	 * 
	 */
	@Test(groups = "remote-integration", expectedExceptions = RequiredHeaderMissingFault.class)
	public void requiredHeaderMissingTest() throws RequiredHeaderMissingFault, SystemUnavailableFault, URISyntaxException {
		TutorialRequest request = getRequest();
		
		MessageSource source = getMessageSource();
		source.setMessageIdentifier(null);
		
		Holder<TutorialResponse> responseHolder = new Holder<TutorialResponse>();
		Holder<MessageAcknowledgment> acknowledgmentHolder = new Holder<MessageAcknowledgment>();
		getPort().sendTutorialMessage(request, source, responseHolder, acknowledgmentHolder);
	}

	/**
	 * @throws URISyntaxException 
	 * @throws SystemUnavailableFault 
	 * @throws RequiredHeaderMissingFault 
	 * 
	 */
	@Test(groups = "remote-integration", expectedExceptions = SystemUnavailableFault.class)
	public void systemUnavailableTest() throws URISyntaxException, RequiredHeaderMissingFault, SystemUnavailableFault {
		TutorialRequest request = getRequest();
		
		MessageSource source = getMessageSource();
		source.setMessageIdentifier("SYSTEM FAILURE");
		
		Holder<TutorialResponse> responseHolder = new Holder<TutorialResponse>();
		Holder<MessageAcknowledgment> acknowledgmentHolder = new Holder<MessageAcknowledgment>();
		getPort().sendTutorialMessage(request, source, responseHolder, acknowledgmentHolder);
	}

	/**
	 * @param uriString
	 * @param value
	 * @return
	 * @throws URISyntaxException
	 */
	private RequestStructure.Tag newTag(String uriString, String value) throws URISyntaxException {
		RequestStructure.Tag tag = new RequestStructure.Tag();
		tag.setUri(new URI(uriString));
		tag.setValue(value);
		return tag;
	}

	/**
	 * @return
	 */
	private MessageSource getMessageSource() {
		MessageSource source = new MessageSource();
		source.setMessageIdentifier(UUID.randomUUID().toString());
		source.setSystemIdentifier("test");
		return source;
	}

	/**
	 * @return
	 * @throws URISyntaxException
	 */
	private TutorialRequest getRequest() throws URISyntaxException {
		TutorialRequest request = new TutorialRequest();
		RequestStructure structure = new RequestStructure();
		structure.setGuid(UUID.randomUUID().toString());
		structure.getTags().add(newTag("tag:vork:339:#1", "Some Value 1"));
		structure.getTags().add(newTag("tag:vork:339:#2", "Some Value 2"));
		structure.getTags().add(newTag("tag:vork:339:#3", "Some Value 3"));
		structure.getTags().add(newTag("tag:vork:339:#4", "Some Value 4"));
		request.setRequestStructure(structure);
		return request;
	}

}
