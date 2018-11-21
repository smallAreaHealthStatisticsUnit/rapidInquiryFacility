package org.sahsu.stats.service.loading;

/*
 *
 * Copied from https://web.archive.org/web/20131202084001/http://www.codeslices.net:80/snippets/simple-java-custom-class-loader-implementation
 * with help from https://community.oracle.com/thread/1821395
 *
 */

/**
 *
 * Simple custom class loader implementation. Intended to allow us to unload the JRI library
 * when the Statistics Service shuts down. Doesn't work at the time of writing.
 *
 */
public class CustomClassLoader extends ClassLoader {

	@Override
	public String toString() {

		return getClass().getName();
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {

		Class theClass = findLoadedClass(name);

		if (theClass == null) {

			theClass = Thread.currentThread().getContextClassLoader().loadClass(name);
		}

		return theClass;
	}
}
