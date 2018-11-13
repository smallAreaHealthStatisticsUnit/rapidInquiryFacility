package org.sahsu.rif.stats.service;

import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameters;

/**
 * The script (only R at the time of writing) that implements the function of the service.
 */
abstract class Script {

	Response run(Parameters parameters) {

		LinkToR r = LinkToR.instance();




		return null;
	}
}
