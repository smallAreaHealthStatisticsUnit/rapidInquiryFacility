package org.sahsu.rif.services.graphics;
/**
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF. If not, see <http://www.gnu.org/licenses/>; or write 
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * </pre>
 *
 * <hr>
 * Peter Hambly
 * @author phambly
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

public enum RIFGraphicsOutputType {
	
	/* Abstract initialisation */
	RIFGRAPHICS_JPEG(1, true, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Joint Photographic Experts Group";
		}
		public String getGraphicsExtentsion() {
			return "jpg";
		}
	},
	RIFGRAPHICS_PNG(2, true, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Portable Network Graphics";
		}
		public String getGraphicsExtentsion() {
			return "png";
		}
	},
	RIFGRAPHICS_GEOTIFF(3, true, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Tagged Image File Format with imbedded georeferencing information";
		}
		public String getGraphicsExtentsion() {
			return "tif";
		}
	},
	RIFGRAPHICS_TIFF(3, false, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Tagged Image File Format";
		}
		public String getGraphicsExtentsion() {
			return "tif";
		}
	},
	RIFGRAPHICS_SVG(4, true, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Scalable Vector Graphics";
		}
		public String getGraphicsExtentsion() {
			return "svg";
		}
	},
	RIFGRAPHICS_EPS(5, true, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Encapsulated Postscript";
		}
		public String getGraphicsExtentsion() {
			return "eps";
		}
	},
	RIFGRAPHICS_PS(6, true, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Postscript";
		}
		public String getGraphicsExtentsion() {
			return "ps";
		}
	};
	
	private final int outputType;
	private final boolean enabled;
	private final boolean usesFop;
	
	/**
	 * Constructor
	 *
	 * @param  outputType	Name of graphics output
	 * @param  enabled 		Boolean: is this outputType enabled?
	 * 						TIFF is currently not available due to a bug: 
	 *						https://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/201708.mbox/%3CCY4PR04MB039071041456B1E485DCB893DDB40@CY4PR04MB0390.namprd04.prod.outlook.com%3E
	 *						Has been fixed in the source, requires 1.9.2 or higher Batik
	 * @param  usesFop		Boolean: graphics outputType uses the Apache FOP processor
	 *						[i.e. cannot use color gradients]
	 */		
	RIFGraphicsOutputType(int outputType, boolean enabled, boolean usesFop) { // Constructor
		this.outputType=outputType;
		this.enabled=enabled;
		this.usesFop=usesFop;
	}

	/**
	 * Get method: outputType
	 */		
	public int getRIFGraphicsOutputType() { 
		return outputType;
	}	

	/**
	 * Get method: enabled
	 */	
	public boolean isRIFGraphicsOutputTypeEnabled() {
		return enabled;
	}	

	/**
	 * Get method: usesFop
	 */		
	public boolean doesRIFGraphicsOutputTypeUseFop() { 
		return usesFop;
	}

	/**
	 * Get method: short_name (i.e. not the RIFGRAPHICS_ bit)
	 */		
	public String getRIFGraphicsOutputTypeShortName() {
		return name().replace("RIFGRAPHICS_", "");
	}
	
	/**
	 * Abstract method implemented by constructor initialisors: get graphics extension
	 * 
	 * @return: 	String extension
	 */	 
	public abstract String getGraphicsExtentsion();	
	
	/**
	 * Abstract method implemented by constructor initialisors: get outputType description
	 * 
	 * @return: 	String description
	 */		
	public abstract String getRIFGraphicsOutputTypeDescription(); // ToString replacement

}
