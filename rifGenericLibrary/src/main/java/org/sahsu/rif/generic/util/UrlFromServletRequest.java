package org.sahsu.rif.generic.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Utility class that does what its name says.
 */
public class UrlFromServletRequest {

	private final HttpServletRequest request;

	public UrlFromServletRequest(final HttpServletRequest request) {

		this.request = request;
	}

	/**
	 * Returns the URL that was encoded in the {@code {@link HttpServletRequest}. Note that this
	 * does not include the context root part, just scheme + host + port.
	 * @return the URL
	 */
	public String get() {

		String host = request.getLocalName();

		// Windows 7 (maybe others) has been known to give the IPv6 form.
		if (host.equals("0:0:0:0:0:0:0:1")) {

			host = "localhost";
		}

		return request.getScheme() + "://" + host + ":" + request.getLocalPort();
	}
}
