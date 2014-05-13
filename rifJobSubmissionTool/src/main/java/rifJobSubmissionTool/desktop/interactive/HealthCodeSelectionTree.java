package rifJobSubmissionTool.desktop.interactive;

import rifJobSubmissionTool.system.RIFSession;
import rifJobSubmissionTool.util.UserInterfaceFactory;

import rifServices.businessConceptLayer.User;
import rifServices.businessConceptLayer.HealthCode;
import rifServices.businessConceptLayer.HealthCodeTaxonomy;
import rifServices.businessConceptLayer.RIFJobSubmissionAPI;
import rifServices.system.RIFServiceException;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


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

public class HealthCodeSelectionTree 
	implements MouseListener {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	//Data
	/** The user. */
	private User user;
	/** The session. */
	private RIFSession session;	
	/** The service. */
	private RIFJobSubmissionAPI service;
	
	//GUI Components
	/** The tree. */
	private JTree tree;
	
	/** The parent dialog. */
	private JDialog parentDialog;	
	/** The root node. */
	private DefaultMutableTreeNode rootNode;
	/** The tree model. */
	private DefaultTreeModel treeModel;
	
	// ==========================================
	// Section Construction
	// ==========================================

	/**
	 * Instantiates a new health code selection tree.
	 *
	 * @param parentDialog the parent dialog
	 * @param session the session
	 */
	public HealthCodeSelectionTree(
		JDialog parentDialog,
		RIFSession session) {

		this.session = session;
		this.user = session.getUser();
		this.service = session.getService();
		this.parentDialog = parentDialog;
		
		UserInterfaceFactory uiFactory = session.getUIFactory();
		
		tree = uiFactory.createTree();
		
		tree.setScrollsOnExpand(false);
		rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		
		HealthCodeTreeCellRenderer healthCodeTreeCellRenderer
			= new HealthCodeTreeCellRenderer();
		tree.setCellRenderer(healthCodeTreeCellRenderer);
		
		tree.setRootVisible(false);
		
		tree.addMouseListener(this);

	}

	/**
	 * Initialise tree.
	 *
	 * @param healthCodeTaxonomy the health code taxonomy
	 */
	public void initialiseTree(
		HealthCodeTaxonomy healthCodeTaxonomy) {

		rootNode.removeAllChildren();
		ArrayList<HealthCode> topLevelHealthCodes 
			= new ArrayList<HealthCode>();
		try {
			topLevelHealthCodes
				= service.getTopLevelCodes(user, healthCodeTaxonomy);
			for (HealthCode topLevelHealthCode : topLevelHealthCodes) {
				HealthCodeTreeNode healthCodeTreeNode
					= new HealthCodeTreeNode(topLevelHealthCode);
				rootNode.add(healthCodeTreeNode);
			}
			treeModel.nodeStructureChanged(rootNode);
		}
		catch(RIFServiceException rifServiceException) {
			ErrorDialog.showError(parentDialog, rifServiceException);
		}		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * Gets the tree.
	 *
	 * @return the tree
	 */
	public JTree getTree() {
		
		return tree;
	}
	
	/**
	 * Gets the selected health codes.
	 *
	 * @return the selected health codes
	 */
	public ArrayList<HealthCode> getSelectedHealthCodes() {
		
		TreePath[] selectedTreePaths = tree.getSelectionPaths();
		
		ArrayList<HealthCode> selectedHealthCodes = new ArrayList<HealthCode>();
		for (TreePath selectedTreePath : selectedTreePaths) {
			HealthCodeTreeNode selectedNode
				= (HealthCodeTreeNode) selectedTreePath.getLastPathComponent();
			HealthCode selectedHealthCode
				= selectedNode.getHealthCode();
			selectedHealthCodes.add(selectedHealthCode);			
		}
		
		return selectedHealthCodes;			
	}
	
	/**
	 * Gets the lead selected health code.
	 *
	 * @return the lead selected health code
	 */
	public HealthCode getLeadSelectedHealthCode() {
		
		TreePath selectedTreePath
			= tree.getLeadSelectionPath();
		HealthCodeTreeNode selectedNode
			= (HealthCodeTreeNode) selectedTreePath.getLastPathComponent();
		HealthCode selectedHealthCode
			= selectedNode.getHealthCode();
		return selectedHealthCode;			
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================
	//Interface: MouseListener
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(
		MouseEvent event) {

		int clickCount = event.getClickCount();
		if (clickCount == 2) {
			//double click event
			
			TreePath selectedTreePath
				= tree.getLeadSelectionPath();
			HealthCodeTreeNode selectedNode
				= (HealthCodeTreeNode) selectedTreePath.getLastPathComponent();
			HealthCode selectedHealthCode
				= selectedNode.getHealthCode();
			if (selectedHealthCode.getNumberOfSubTerms() == 0) {
				return;
			}
			
			try {
				ArrayList<HealthCode> subTerms
					= service.getImmediateSubterms(
						session.getUser(), 
						selectedHealthCode);
				for (HealthCode subTerm : subTerms) {
					HealthCodeTreeNode healthCodeTreeNode
						= new HealthCodeTreeNode(subTerm);
					treeModel.insertNodeInto(
						healthCodeTreeNode, 
						selectedNode, 
						selectedNode.getChildCount());
					TreePath treePath
						= new TreePath(treeModel.getPathToRoot(healthCodeTreeNode));
					tree.scrollPathToVisible(treePath);					
				}
				
				/*
				 * forcing visibility of sub terms may cause tree to scroll down
				 * so far the original parent node is not visible.  After all the
				 * paths of sub terms are made visible, scroll to path of 
				 * parent term
				 */
				TreePath treePath
					= new TreePath(treeModel.getPathToRoot(selectedNode));
				tree.scrollPathToVisible(treePath);					
			
			}
			catch(RIFServiceException rifServiceException) {
				ErrorDialog.showError(parentDialog, rifServiceException);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(
		MouseEvent event) {}	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(
		MouseEvent event) {}
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(
		MouseEvent event) {}
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(
		MouseEvent event) {}
	
	// ==========================================
	// Section Override
	// ==========================================
}

