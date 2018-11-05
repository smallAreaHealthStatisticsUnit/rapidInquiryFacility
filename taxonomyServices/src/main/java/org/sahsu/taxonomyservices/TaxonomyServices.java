package org.sahsu.taxonomyservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahsu.rif.generic.system.Messages;
import org.sahsu.rif.generic.util.TaxonomyLogger;

class TaxonomyServices {

	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	private static final TaxonomyLogger logger = TaxonomyLogger.getLogger();

	private final List<TaxonomyServiceConfiguration> configurations;
	private Map<String, TaxonomyServiceAPI> apis = new HashMap<>();
	private final List<TaxonomyServiceProvider> providers = new ArrayList<>();

	TaxonomyServices(final List<TaxonomyServiceConfiguration> configs) {

		configurations = configs;
	}

	List<String> start() {

		List<String> errorMessages = new ArrayList<>();
		for (TaxonomyServiceConfiguration config : configurations) {

			String ontologyServiceClassName = null;
			try {

				ontologyServiceClassName = config.getOntologyServiceClassName().trim();

				/*
				 * Load the class in to memory, if it is not loaded - which means creating in-memory
				 * representation of the class from the .class file so that an instance can be created out of it. This includes initializing static variables (resolving of that class)
				 *
				 * create an instance of that class and store the reference to the variable.
				 */
				Class<? extends TaxonomyServiceAPI> taxonomyServiceClass = Class.forName(
						ontologyServiceClassName).asSubclass(TaxonomyServiceAPI.class);
				TaxonomyServiceAPI taxonomyService =
						taxonomyServiceClass.getConstructor().newInstance();

				taxonomyService.initialiseService(config);

				apis.put(taxonomyService.getIdentifier().trim(), taxonomyService);

				TaxonomyServiceProvider provider = new TaxonomyServiceProvider();
				provider.setIdentifier(taxonomyService.getIdentifier().trim());
				provider.setName(taxonomyService.getName().trim());
				provider.setDescription(taxonomyService.getDescription().trim());
				providers.add(provider);

			} catch(NoClassDefFoundError noClassDefFoundError) {

				logger.error(this.getClass(), "Unable to load taxonomyService: " +
				                              ontologyServiceClassName, noClassDefFoundError);

				String errorMessage = GENERIC_MESSAGES.getMessage(
						"taxonomyServices.error.initialisationFailure", config.getName());
				errorMessages.add(errorMessage);
			}
			catch(Exception exception) {

				logger.error(this.getClass(), "Exception initializing taxonomyService: " +
				                              ontologyServiceClassName, exception);
				String errorMessage = GENERIC_MESSAGES.getMessage(
						"taxonomyServices.error.initialisationFailure", config.getName());
				errorMessages.add(errorMessage);
			}
		}

		return errorMessages;
	}

	TaxonomyServiceAPI getApi(String identifier) {

		return apis.get(identifier);
	}

	List<TaxonomyServiceAPI> getApis() {

		return new ArrayList<>(apis.values());
	}

	List<TaxonomyServiceProvider> getProviders() {

		return providers;
	}
}
