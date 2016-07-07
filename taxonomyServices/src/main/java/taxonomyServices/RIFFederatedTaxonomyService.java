package taxonomyServices;


import rifGenericLibrary.system.ClassFileLocator;
import rifGenericLibrary.taxonomyServices.*;

import java.util.ArrayList;

public class RIFFederatedTaxonomyService extends DefaultFederatedTaxonomyService {

	public static void main(String[] args) {
		RIFFederatedTaxonomyService service
			= new RIFFederatedTaxonomyService();
		try {
			String defaultResourceDirectoryPath
				= ClassFileLocator.getClassRootLocation("taxonomyServices");
			service.initialise(defaultResourceDirectoryPath);
			
			ArrayList<TaxonomyTerm> taxonomyTerms
				= service.getRootTerms("icd10");
			for (TaxonomyTerm taxonomyTerm : taxonomyTerms) {
				System.out.println("TaxonomyTerm=="+taxonomyTerm.getLabel()+"==");
				ArrayList<TaxonomyTerm> subTerms
					= service.getImmediateChildTerms("icd10", taxonomyTerm.getIdentifier());
				for (TaxonomyTerm subTerm : subTerms) {
					System.out.println("CHILD TERM=="+subTerm.getLabel()+"==");
				}
			}
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
	}
	
}
