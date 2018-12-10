package org.sahsu.rif.services.graphics;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.graphics.SlippyTile;

/**
 * Tile handling exception (usually the tile requested is invalid).
 *
 * @author		Peter Hambly
 * @version 	1.0
 * @since 		4.0
 */
public class RIFTilesException extends RIFServiceException {
	
	private static SlippyTile slippyTile = null;
	
	public RIFTilesException( 
		final Exception exception, 
		final SlippyTile slippyTile) {
		
		super((exception != null && exception.getMessage() != null) ? exception.getMessage() : "RIFTilesException: no error message",
			exception != null ? exception.getCause() : null);
		this.slippyTile = slippyTile;
	}

	public SlippyTile getSlippyTile() {
		return slippyTile;
	}	
}
