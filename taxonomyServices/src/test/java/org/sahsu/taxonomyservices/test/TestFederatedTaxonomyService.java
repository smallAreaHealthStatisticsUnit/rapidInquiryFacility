package org.sahsu.taxonomyservices.test;

import org.junit.Test;
import org.sahsu.rif.generic.system.ClassFileLocator;
import org.sahsu.rif.generic.taxonomyservices.FederatedTaxonomyService;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;

import java.util.ArrayList;
import java.util.List;

public class TestFederatedTaxonomyService {

	@Test
	public void testService() {
		
		FederatedTaxonomyService service
			= FederatedTaxonomyService.getFederatedTaxonomyService();

		try {
			String defaultResourceDirectoryPath
				= ClassFileLocator.getClassRootLocation("taxonomyServices");
						
			service.initialise(defaultResourceDirectoryPath);
			System.out.println("Initialising again");
			service.initialise(defaultResourceDirectoryPath);
			
			List<TaxonomyTerm> taxonomyTerms = service.getRootTerms("icd10");
			for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
				System.out.println("TaxonomyTerm=="+taxonomyTerm.getLabel()+"==");
				List<TaxonomyTerm> subTerms = service.getImmediateChildTerms("icd10",
				                                                             taxonomyTerm.getLabel());
				for (TaxonomyTerm subTerm : subTerms) {
					System.out.println("CHILD TERM=="+subTerm.getLabel()+"==");
				}
			}
		} catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
		
	}
}

