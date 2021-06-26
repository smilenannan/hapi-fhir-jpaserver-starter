package ca.uhn.fhir.jpa.starter;

import java.time.LocalDate;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Identifier;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

@Interceptor
public class ResourceInterceptor {
	@Autowired
	private IFhirResourceDao<EpisodeOfCare> myEpisodeOfCareDao;
	private static ApplicationContext context;
	public ResourceInterceptor(ApplicationContext myApplicationContext) {
		context = myApplicationContext;
	}

	@Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
	public void created(IBaseResource resource, RequestDetails theRequest) {
		String resourceType = resource.fhirType();
		if(resourceType == "EpisodeOfCare") {
			myEpisodeOfCareDao = (IFhirResourceDao<EpisodeOfCare>) context.getBean("myEpisodeOfCareDaoR4");
			SearchParameterMap paramMap = new SearchParameterMap();
			DateParam param = new DateParam();
			LocalDate todayDate = LocalDate.now();
			LocalDate firstDateOfMonth = todayDate.withDayOfMonth(1);
			param.setValueAsString("ge" + firstDateOfMonth);
			param.setValueAsString("le" + todayDate);
            paramMap.add("date", param);
            IBundleProvider found = myEpisodeOfCareDao.search(paramMap, theRequest);
            int count = found.size();
			int year = todayDate.getYear();
			int month = todayDate.getMonthValue();
			String stn = "TN." + String.format("%02d", year%100) + String.format("%02d", month) + "." + String.format("%06d", count+1);
			EpisodeOfCare eoc =  (EpisodeOfCare) resource;
			Identifier identifier = eoc.addIdentifier();
			CodeableConcept type = new CodeableConcept();
			type.setText("STN");
			identifier.setType(type).setValue(stn);
		}
	}
}
