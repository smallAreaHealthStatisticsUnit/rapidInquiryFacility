package org.sahsu.stats.service;

import javax.ws.rs.core.Response;

import org.sahsu.rif.generic.concepts.Parameters;

/**
 * The script (only R at the time of writing) that implements the function of the service.
 */
class Script {

	Response run(Parameters parameters) {

		ScriptService r = ScriptService.instance();
		return r.runScript(parameters);
	}
}
