package taxonomyServices;

import rifGenericLibrary.system.ClassFileLocator;
import rifGenericLibrary.system.RIFServiceException;
import rifGenericLibrary.taxonomyServices.*;

public class RIFFederatedTaxonomyService extends DefaultFederatedTaxonomyService {

	
	public static void main(String[] args) {
		try {
			RIFFederatedTaxonomyService service
				= new RIFFederatedTaxonomyService();
			service.initialiseService();	
		}
		catch(RIFServiceException rifServiceException) {
			rifServiceException.printErrors();
		}
	}
	
	public RIFFederatedTaxonomyService() {
		
	}
	
	public void initialiseService() 
		throws RIFServiceException {

		String defaultResourceDirectoryPath
			= ClassFileLocator.getClassRootLocation("taxonomyServices");
		initialise(defaultResourceDirectoryPath);		
	}
	
}
