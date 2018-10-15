package org.sahsu.taxonomyservices;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.sahsu.rif.generic.util.TaxonomyLogger;

/**
 * The root of the Taxonomy Service web service. The root path, taxonomies,
 * is provided by the name of the application's war file.
 */
@ApplicationPath("/service")
public class RIFTaxonomyWebServiceApplication extends Application {

	public RIFTaxonomyWebServiceApplication() {

		final TaxonomyLogger rifLogger = TaxonomyLogger.getLogger();
		rifLogger.info(this.getClass(), "!!!!!!!!!!!!!!!!!!!!! RIFTaxonomyWebServiceApplication !!!!!!");
	}

	@Override
	public Set<Class<?>> getClasses() {
		
		Set<Class<?>> set = new HashSet<>();
		set.add(RIFTaxonomyWebServiceResource.class);
		return set;
	}
	
}
