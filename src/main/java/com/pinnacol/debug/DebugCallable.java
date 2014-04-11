package com.pinnacol.debug;


import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.httpclient.ContentLengthInputStream;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.mule.api.transport.PropertyScope;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

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
		
		this.setUpMessage(message, in);
		
		return message;
	}
	
	
	private void setUpMessage(MuleMessage message, MultipartStream in) throws Exception {
		
		boolean nextPart = true;
		
		DataHandler inboundDH = null;
		DataHandler outboundDH = null;
		File file = null;

		while(nextPart) {

			String headers = in.readHeaders();
			System.out.println("Headers: " + headers);
			
			
			if(headers.indexOf("name=\"to\"") > -1 ){
				
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				in.readBodyData(data);
				String bodydata = new String(data.toByteArray());
				
				System.out.println(bodydata);
				
				String replacedBodyData = bodydata.replaceAll("%40", "@");
				
				message.setProperty("to", replacedBodyData, PropertyScope.OUTBOUND);
				
			}
			else if(headers.indexOf("name=\"subject\"") > -1) {
				
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				in.readBodyData(data);
				String bodydata = new String(data.toByteArray());
				
				System.out.println(bodydata);
				
				message.setProperty("subject", bodydata, PropertyScope.OUTBOUND);
				
			}
			else if(headers.indexOf("filename=") > -1) {

				//Content-Type: image/jpeg
				int filenameIndex = headers.indexOf("filename=");
				int newlineIndex = headers.indexOf("\r\n");
								
				String filename = headers.substring(filenameIndex + 9, newlineIndex);
				
				System.out.println("filename = " + filename);
				
				int contentIndex = headers.indexOf("Content-Type: ");
				String contentType = headers.substring(contentIndex + 14);
				int contentNewlineIndex = contentType.indexOf("\r\n");
				
				String finalContentType = contentType.substring(0, contentNewlineIndex);
				
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				in.readBodyData(data);
				
				ByteArrayDataSource bads = new ByteArrayDataSource(data.toByteArray(), finalContentType);
				outboundDH = new DataHandler(bads);
				
//				message.addOutboundAttachment(filename, data, finalContentType);
				message.addOutboundAttachment(filename, outboundDH);
				

				System.out.println(new String(data.toByteArray()));
			}
		

			nextPart = in.readBoundary();
			
////			message.addOutboundAttachment(name, dataHandler)
//			message.add

		}
	}
	

}
