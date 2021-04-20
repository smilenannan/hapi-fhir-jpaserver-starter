package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import org.hl7.fhir.r4.model.IdType;

import io.jsonwebtoken.Jws;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

import ca.uhn.fhir.jpa.starter.PublicKeyReader;

import java.security.PublicKey;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class CustomizedAuthorizationInterceptor extends AuthorizationInterceptor {

   @Override
   public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
	  PublicKey key = null;
	  try {
		  key = PublicKeyReader.get("config/jwt/public_key.der");
	  } catch (Exception ex) {
		  ex.printStackTrace();
	  }
	  String authHeader = theRequestDetails.getHeader("Authorization");
	  if (authHeader=="" || authHeader==null) {
		  throw new AuthenticationException("Missing Authorization header");
	  }
	  String jwsString = authHeader.split(" ")[1];
	  Jws<Claims> jws;
	  try {
		  jws = Jwts.parserBuilder()
		  .setSigningKey(key)
		  .build()
		  .parseClaimsJws(jwsString);
	  }	catch (JwtException ex) {
		  throw new AuthenticationException("Invalid Authorization header value");
	  }
	  //TODO: Add if according role value in JWT payload
	  return new RuleBuilder()
			  .deny().operation().named("$partition-management-create-partition").atAnyLevel().andAllowAllResponses().andThen()
			  .deny().operation().named("$partition-management-update-partition").atAnyLevel().andAllowAllResponses().andThen()
			  .deny().operation().named("$partition-management-delete-partition").atAnyLevel().andAllowAllResponses().andThen()
			  .allowAll()
			  .build();
   }
}
