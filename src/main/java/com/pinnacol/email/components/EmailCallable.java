package com.pinnacol.email.components;

import java.util.HashMap;

import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;

public class EmailCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		MuleMessage message = eventContext.getMessage();
		
		HashMap<String, String> emailPayload = (HashMap<String, String>) message.getPayload();
		
		message.setProperty("to", emailPayload.get("to"), PropertyScope.OUTBOUND);
		message.setProperty("subject", emailPayload.get("subject"), PropertyScope.OUTBOUND);
		
		message.setPayload(emailPayload.get("body"));
		
		
		return message;
	}

	
}
