package ca.uhn.fhir.jpa.starter;

import java.util.Base64;

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
		Integer partitionId = payload.optInt("group_id");
		if (partitionId == 0) {
			// Default partition's id is null
			return RequestPartitionId.defaultPartition();
		}
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
		Integer partitionId = payload.optInt("group_id");
		String resourceType = theRequestDetails.getResourceName();
		if (resourceType.equals("CodeSystem")) {
			return RequestPartitionId.defaultPartition();
		}
		if (partitionId == 0) {
			// Default partition's id is null
			return RequestPartitionId.defaultPartition();
		}
		return RequestPartitionId.fromPartitionId(partitionId);
	}

}
