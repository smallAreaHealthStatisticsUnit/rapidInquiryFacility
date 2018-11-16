package org.sahsu.rif.generic.util;

import javax.servlet.http.HttpServletRequest;

public class UrlFromServletRequest {

	private final HttpServletRequest request;

	public UrlFromServletRequest(final HttpServletRequest request) {

		this.request = request;
	}

	public String get() {

		String host = request.getLocalName();

		// Windows 7 (maybe others) has been known to give the IPv6 form.
		if (host.equals("0:0:0:0:0:0:0:1")) {

			host="localhost";
		}

		return request.getScheme() + "://" + host + ":" + request.getLocalPort();
	}

}
