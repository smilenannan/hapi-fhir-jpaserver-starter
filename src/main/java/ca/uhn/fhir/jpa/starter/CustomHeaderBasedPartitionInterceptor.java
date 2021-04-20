package ca.uhn.fhir.jpa.starter;

import java.util.Base64;
import java.util.Iterator;

import org.json.JSONObject;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

@Interceptor
public class CustomHeaderBasedPartitionInterceptor {
	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_CREATE)
	public RequestPartitionId PartitionIdentifyCreate(ServletRequestDetails theRequestDetails) {
		String authHeader = theRequestDetails.getHeader("Authorization");
		String jwsString = authHeader.split(" ")[1];
		String[] chunks = jwsString.split("\\.");
		Base64.Decoder decoder = Base64.getDecoder();
		String payloadString = new String(decoder.decode(chunks[1]));
		JSONObject payload = new JSONObject(payloadString);
		Integer partitionId = payload.getInt("group_id");
		return RequestPartitionId.fromPartitionId(partitionId);
	}

	@Hook(Pointcut.STORAGE_PARTITION_IDENTIFY_READ)
	public RequestPartitionId PartitionIdentifyRead(ServletRequestDetails theRequestDetails) {
		String authHeader = theRequestDetails.getHeader("Authorization");
		String jwsString = authHeader.split(" ")[1];
		String[] chunks = jwsString.split("\\.");
		Base64.Decoder decoder = Base64.getDecoder();
		String payloadString = new String(decoder.decode(chunks[1]));
		JSONObject payload = new JSONObject(payloadString);
		Integer partitionId = payload.getInt("group_id");
		return RequestPartitionId.fromPartitionId(partitionId);
	}

}
