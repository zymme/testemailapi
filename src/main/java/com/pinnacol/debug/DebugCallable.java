package com.pinnacol.debug;


import java.io.ByteArrayOutputStream;

import org.apache.commons.httpclient.ContentLengthInputStream;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.apache.commons.fileupload.MultipartStream;

public class DebugCallable implements Callable {

	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {

		MuleMessage message = eventContext.getMessage();

		System.out.println("Message payload " + message.getPayload());
		
		ContentLengthInputStream cis = (ContentLengthInputStream) message.getPayload();
		
		
		String contentType = message.getProperty("Content-Type", PropertyScope.INBOUND);
		
		int boundaryIndex = contentType.indexOf("boundary=");
		byte[] boundary = (contentType.substring(boundaryIndex + 9)).getBytes();
		
		MultipartStream in = new MultipartStream(cis, boundary);
		
		boolean nextPart = true;
		
		while(nextPart) {
			
		  String headers = in.readHeaders();
		  System.out.println("Headers: " + headers);
		  ByteArrayOutputStream data = new ByteArrayOutputStream();
		  
		  in.readBodyData(data);
		  
		  System.out.println(new String(data.toByteArray()));

		  nextPart = in.readBoundary();
		  
		}
		
		return message;
	}

}
