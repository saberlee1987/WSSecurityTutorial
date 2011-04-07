/*
 * Created on Apr 5, 2011
 */
package com.example.tutorial.ws;

import java.util.Calendar;

import javax.inject.Named;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import com.example.tutotial.MessageAcknowledgment;
import com.example.tutotial.MessageSource;
import com.example.tutotial.RequiredHeaderMissingFault;
import com.example.tutotial.RequiredHeaderMissingFaultMessage;
import com.example.tutotial.SystemUnavailableFault;
import com.example.tutotial.SystemUnavailableFaultMessage;
import com.example.tutotial.TutorialRequest;
import com.example.tutotial.TutorialResponse;
import com.example.tutotial.TutorialWebService;
import com.example.tutotial.types.ResponseStructure;

/**
 * TODO Class description
 *
 * @author Ross M. Lodge
 */
@Named("TutorialWebService")
@Scope(value = "singleton")
@WebService(endpointInterface = "com.example.tutotial.TutorialWebService", targetNamespace = "http://example.com/tutotial/",
        serviceName = "TutorialWebService", portName = "TutorialWebServiceSOAP" )
public class TutorialWebServiceImpl implements TutorialWebService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	/** 
	 * {@inheritDoc}
	 *
	 * @see com.example.tutotial.TutorialWebService#sendTutorialMessage(com.example.tutotial.TutorialRequest, com.example.tutotial.MessageSource, javax.xml.ws.Holder, javax.xml.ws.Holder)
	 */
	@Override
	public void sendTutorialMessage(TutorialRequest parameters, MessageSource source, Holder<TutorialResponse> response,
			Holder<MessageAcknowledgment> acknowledgment) throws RequiredHeaderMissingFault, SystemUnavailableFault {
		log.info("Received request message: {}", parameters.getRequestStructure());
		if (source == null) {
			throwRequiredHeaderFault("Source cannot be null.", "source");
		}
		else if (StringUtils.isEmpty(source.getMessageIdentifier())) {
			throwRequiredHeaderFault("messageIdentifier cannot be null or empty.", "message-identifier");
		}
		else if (StringUtils.isEmpty(source.getSystemIdentifier())) {
			throwRequiredHeaderFault("systemIdentifier cannot be null or empty.", "system-identifier");
		}
		else if (source.getMessageIdentifier().equals("SYSTEM FAILURE")) {
			SystemUnavailableFaultMessage faultInfo = new SystemUnavailableFaultMessage();
			faultInfo.setMessage("SystemUnvailabelFault requested.");
			throw new SystemUnavailableFault(faultInfo.getMessage(), faultInfo);
		}
		TutorialResponse responseMessage = new TutorialResponse();
		ResponseStructure responseStructure = new ResponseStructure();
		responseStructure.setResponseCode("00000");
		responseStructure.setResponseMessage("SUCCESS!");
		responseStructure.setServerDate(Calendar.getInstance());
		responseMessage.setResponseStructure(responseStructure);
		response.value = responseMessage;
		
		MessageAcknowledgment acknowledgmentMessage = new MessageAcknowledgment();
		acknowledgmentMessage.setSomeMessage("WE ACKNOWLEDGE!");
		acknowledgment.value = acknowledgmentMessage;
	}

	/**
	 * @param message
	 * @param headerName
	 * @throws RequiredHeaderMissingFault
	 */
	private void throwRequiredHeaderFault(String message, String headerName) throws RequiredHeaderMissingFault {
		RequiredHeaderMissingFaultMessage faultInfo = new RequiredHeaderMissingFaultMessage();
		faultInfo.setMessage(message);
		faultInfo.setMissingHeaderName(headerName);
		throw new RequiredHeaderMissingFault(message, faultInfo);
	}

}
