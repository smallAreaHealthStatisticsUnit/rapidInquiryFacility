package rifServices.dataStorageLayer.common;
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
	RIFGRAPHICS_JPEG(1, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Joint Photographic Experts Group";
		}
	},
	RIFGRAPHICS_PNG(2, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Portable Network Graphics";
		}
	},
	RIFGRAPHICS_TIFF(3, false) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Tagged Image File Format";
		}
	},
	RIFGRAPHICS_SVG(4, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Scalable Vector Graphics";
		}
	},
	RIFGRAPHICS_EPS(5, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Encapsulated Postscript";
		}
	},
	RIFGRAPHICS_PS(6, true) {
		public String getRIFGraphicsOutputTypeDescription() {
			return "Postscript";
		}
	};
	
	private final int outputType;
	private final boolean enabled;
	
	RIFGraphicsOutputType(int outputType, boolean enabled) { // Constructor
		this.outputType=outputType;
		this.enabled=enabled;
	}
	
	int getRIFGraphicsOutputType() { // Get method
		return outputType;
	}	
	
	boolean isRIFGraphicsOutputTypeEnabled() { // Get method
		return enabled;
	}
	
	public abstract String getRIFGraphicsOutputTypeDescription(); // ToString replacement
	public String getRIFGraphicsOutputTypeShortName() {
		return name().replace("RIFGRAPHICS_", "");
	}
}