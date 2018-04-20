package rifServices.dataStorageLayer.pg;

import rifServices.dataStorageLayer.common.AbstractSQLManager;
import rifServices.system.RIFServiceStartupOptions;

/**
 * Responsible for managing a pool of connections for each registered user.  Connections will
 * be configured to be write only or read only.  We do this for two reasons:
 * <ol>
 * <li><b>security</b>.  Many of the operations will require a read connection.  Therefore, should
 * the connection be used to execute a query that contains malicious code, it will likely fail because
 * many malicious attacks use <i>write</i> operations.
 * </li>
 * <li>
 * <b>efficiency</b>. It is easier to develop database clustering if the kinds of operations for connections
 * are streamlined
 * </li>
 * </ul>
 * <p>
 * Note that this connection manager does not pool anonymised connection objects.  Each of them must be associated
 * with a specific userID
 * </p>
 *
 */

public final class PGSQLConnectionManager extends AbstractSQLManager {
	
	/**
	 * Instantiates a new SQL connection manager.
	 *
	 * @param rifServiceStartupOptions the rif service startup options
	 */
	public PGSQLConnectionManager(
			final RIFServiceStartupOptions rifServiceStartupOptions) {

		super(rifServiceStartupOptions);
		
	}
	
}
