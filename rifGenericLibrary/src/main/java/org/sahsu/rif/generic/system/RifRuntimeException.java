package org.sahsu.rif.generic.system;

public class RifRuntimeException extends RuntimeException {

	public RifRuntimeException(Throwable t) {

		super(t);
	}

	public RifRuntimeException(String msg, Throwable t) {

		super(msg, t);
	}
}
