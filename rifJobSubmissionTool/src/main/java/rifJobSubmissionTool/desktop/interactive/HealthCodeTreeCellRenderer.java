package rifJobSubmissionTool.desktop.interactive;

import rifServices.businessConceptLayer.HealthCode;

import java.awt.Component;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JTree;


/**
 *
 *
 * <hr>
 * The Rapid Inquiry Facility (RIF) is an automated tool devised by SAHSU 
 * that rapidly addresses epidemiological and public health questions using 
 * routinely collected health and population data and generates standardised 
 * rates and relative risks for any given health outcome, for specified age 
 * and year ranges, for any given geographical area.
 *
 * <p>
 * Copyright 2014 Imperial College London, developed by the Small Area
 * Health Statistics Unit. The work of the Small Area Health Statistics Unit 
 * is funded by the Public Health England as part of the MRC-PHE Centre for 
 * Environment and Health. Funding for this project has also been received 
 * from the United States Centers for Disease Control and Prevention.  
 * </p>
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
 * Kevin Garwood
 * @author kgarwood
 * @version
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

class HealthCodeTreeCellRenderer 
	extends DefaultTreeCellRenderer {
	
	// ==========================================
	// Section Constants
	// ==========================================
	private static final long serialVersionUID = -1706983240881121450L;

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================


	/**
	 * Instantiates a new health code tree cell renderer.
	 */
	public HealthCodeTreeCellRenderer() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override	
	public Component getTreeCellRendererComponent(
		JTree tree,
		Object value,
		boolean isSelected,
		boolean isExpanded,
		boolean isLeaf,
		int row,
		boolean hasFocus) {
		
		
		if (value.getClass() == DefaultMutableTreeNode.class) {
			return this;
		}
	
		HealthCodeTreeNode healthCodeTreeNode = (HealthCodeTreeNode) value;
		HealthCode healthCode = healthCodeTreeNode.getHealthCode();
		int numberOfChildren = healthCode.getNumberOfSubTerms();
		
		StringBuilder nodeLabelText = new StringBuilder();
		nodeLabelText.append(healthCode.getDisplayName());
		
		if (numberOfChildren > 0) {
			//render the icon as a folder			
			if (isExpanded) {
				setIcon(getOpenIcon());
			}
			else {
				setIcon(getClosedIcon());				
			}
			nodeLabelText.append(" (");
			nodeLabelText.append(String.valueOf(numberOfChildren));
			nodeLabelText.append(")");
		}
		else {
			//render as a leaf
			setIcon(getLeafIcon());
		}
		setText(nodeLabelText.toString());
		this.hasFocus = hasFocus;
		this.selected = isSelected;
		return this;
	}
	
	
}

