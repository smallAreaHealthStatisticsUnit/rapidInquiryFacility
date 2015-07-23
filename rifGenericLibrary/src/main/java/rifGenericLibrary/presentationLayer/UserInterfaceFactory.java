
package rifGenericLibrary.presentationLayer;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.TableModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import rifGenericLibrary.system.RIFGenericLibraryMessages;




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


public final class UserInterfaceFactory {


// ==========================================
// Section Constants
// ==========================================
	
	/** Default width for text components */
	private static final int DEFAULT_TEXT_FIELD_WIDTH = 20;
	
	/** Default height for text areas */
	private static final int DEFAULT_TEXT_AREA_HEIGHT = 5;
	
	/** The Constant HORIZONTAL_COMPONENT_GAP. */
	public static final int HORIZONTAL_COMPONENT_GAP = 10;
	
	/** The Constant VERTICAL_COMPONENT_GAP. */
	public static final int VERTICAL_COMPONENT_GAP = 10;
		
// ==========================================
// Section Properties
// ==========================================
	/** The instruction label colour. */
	private Color INSTRUCTION_LABEL_COLOUR = new Color(25, 125, 55);
	
	/** The ui defaults. */
	private UIDefaults uiDefaults;
    
// ==========================================
// Section Construction
// ==========================================
    /**
     * Instantiates a new user interface factory.
     */
	public UserInterfaceFactory() {
		
    	try {    
    		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {   
    			if ("Nimbus".equals(info.getName())) {
    				UIManager.setLookAndFeel(info.getClassName());
    				break;        
    			}   
    		}
    	} 
    	catch (Exception e) {    
    		//@TODO
    	}
    	
    	Color bgColor = (new JLabel()).getBackground();
    	uiDefaults = new UIDefaults();
    	uiDefaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
    }

// ==========================================
// Section Accessors and Mutators
// ==========================================
    
    /**
     * Creates a new UserInterface object.
     *
     * @param headerLevel the header level
     * @param labelText the label text
     * @return the string
     */
	public String createHTMLLabelText(
		int headerLevel, 
		String labelText) {
   
		ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();
	
    	HTMLUtility htmlUtility = new HTMLUtility();
    	htmlUtility.initialise(outputStream, "UTF-8");
    	htmlUtility.beginDocument();
    	htmlUtility.beginBody();
    	htmlUtility.writeHeader(headerLevel, labelText);
    	htmlUtility.endBody();
    	htmlUtility.endDocument();
    	
    	JLabel label = new JLabel("");
    	try {    		
    		String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");	
    		outputStream.close();
    		return htmlFormattedInstructionText;
    	}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    		label.setText(labelText);
    	}
    	
    	return labelText;
    }

    
    /**
     * Creates a new UserInterface object.
     *
     * @param headerLevel the header level
     * @param labelText the label text
     * @return the j label
     */
    public JLabel createHTMLLabel(
    	int headerLevel, 
    	String labelText) {
   
    	ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();
	
    	HTMLUtility htmlUtility = new HTMLUtility();
    	htmlUtility.initialise(outputStream, "UTF-8");
    	htmlUtility.beginDocument();
    	htmlUtility.writeHeader(headerLevel, labelText);
    	htmlUtility.endDocument();
    	
    	JLabel label = new JLabel("");
    	try {    		
    		String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");			
    		label.setText(htmlFormattedInstructionText);
    		outputStream.close();
    	}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    		label.setText(labelText);
    	}
    	
    	return label;
    }
    
    
    /**
     * Creates a new UserInterface object.
     *
     * @param instructionsText the instructions text
     * @return the string
     */
    public String createHTMLInstructionText(
    	String instructionsText) {
        
    	ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();
    	
    	HTMLUtility htmlUtility = new HTMLUtility();
    	htmlUtility.initialise(outputStream, "UTF-8");
    	htmlUtility.beginItalic();
    	htmlUtility.writeParagraph(instructionsText);
    	htmlUtility.endItalic();
    	htmlUtility.endDocument();
    	
    	try {    		
    		String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");			
    		outputStream.close();
    		return htmlFormattedInstructionText;
    	}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    	}
 	
    	return instructionsText;	
    }
    
    /**
     * Creates a new UserInterface object.
     *
     * @param instructionsText the instructions text
     * @return the j panel
     */
    public JPanel createHTMLInstructionPanel(
    	String instructionsText) {
    
    	ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();
    	
    	HTMLUtility htmlUtility = new HTMLUtility();
    	htmlUtility.initialise(outputStream, "UTF-8");
    	htmlUtility.beginDocument();
    	htmlUtility.beginItalic();
    	htmlUtility.writeParagraph(instructionsText);
    	htmlUtility.endItalic();
    	htmlUtility.endDocument();
    	
    	JPanel panel = createPanel();
    	panel.setOpaque(false);
    	GridBagConstraints panelGC = createGridBagConstraints();
    	panelGC.fill = GridBagConstraints.BOTH;
    	panelGC.weightx = 1;
    	panelGC.weighty = 1;
    	JEditorPane editorPane = createHTMLEditorPane(); 
    	editorPane.putClientProperty("Nimbus.Overrides", uiDefaults);
    	editorPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
    	editorPane.setBackground((new JLabel()).getBackground());
    	
    	
    	editorPane.setOpaque(false);
    	panel.add(editorPane, panelGC);
    	panel.setBorder(LineBorder.createGrayLineBorder());
    	try {    		
    		String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");			
    		editorPane.setText(htmlFormattedInstructionText);
    		outputStream.close();
    	}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    		editorPane.setText(instructionsText);
    	}
 	
    	return panel;	
    }
    
    /**
     * Creates a new UserInterface object.
     *
     * @param dialogTitle the dialog title
     * @return the j dialog
     */
    public JDialog createHTMLDialog(
    	String dialogTitle) {
   
    	ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();
	
    	HTMLUtility htmlUtility = new HTMLUtility();
    	htmlUtility.initialise(outputStream, "UTF-8");
    	htmlUtility.beginDocument();
    	htmlUtility.writeHeader(1, dialogTitle);
    	htmlUtility.endDocument();
    
    	JDialog dialog = new JDialog();
    	
    	try {    		
    		String htmlFormattedInstructionText 
				= new String(outputStream.toByteArray(), "UTF-8");			
    		dialog.setTitle(htmlFormattedInstructionText);
    		outputStream.close();
    	}
    	catch(Exception exception) {
    		//in the event of an exception, just write instruction
    		//text as normal
    		dialog.setTitle(dialogTitle);
    	}
    	
    	return dialog;
    }
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j label
	 */
	public JLabel createSelectedArrowLabel() {
		
		ImageIcon icon = new ImageIcon("\\img\\SelectedArrow.bmp");		
		JLabel label = new JLabel(icon);
		return label;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the basic arrow button
	 */
	public BasicArrowButton createBlackArrowButton() {
		
		Color backGroundColour = (new JLabel()).getBackground();
		BasicArrowButton button
			= new BasicArrowButton(SwingConstants.EAST,
				backGroundColour,
				Color.black,
				Color.black,
				Color.black);
		button.setEnabled(false);
		return button;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the basic arrow button
	 */
	public BasicArrowButton createGrayArrowButton() {
		
		Color backGroundColour = (new JLabel()).getBackground();
		BasicArrowButton button
			= new BasicArrowButton(SwingConstants.EAST,
				backGroundColour,
				Color.darkGray,
				Color.darkGray,
				Color.darkGray);
		button.setEnabled(false);
		return button;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j menu bar
	 */
	public JMenuBar createMenuBar() {
		
		JMenuBar menuBar = new JMenuBar();
		return menuBar;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param menuName the menu name
	 * @return the j menu
	 */
	public JMenu createMenu(String menuName) {
		
		JMenu menu = new JMenu(menuName);
		return menu;
	}
	
	
	public JMenu createFileMenu() {
		String fileMenuText
			= RIFGenericLibraryMessages.getMessage("menus.file.label");
		return createMenu(fileMenuText);
	}
	
	public JMenuItem createExitMenuItem() {
		String exitMenuItemText
			= RIFGenericLibraryMessages.getMessage("fileMenu.exit.label");
		return createJMenuItem(exitMenuItemText);
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param menuItemName the menu item name
	 * @return the j menu item
	 */
	public JMenuItem createJMenuItem(
		String menuItemName) {
		
		JMenuItem menuItem = new JMenuItem(menuItemName);
		return menuItem;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param buttonName the button name
	 * @return the j button
	 */
	public JButton createButton(
		final String buttonName) {
		
		JButton button = new JButton(buttonName);
		setPlainFont(button);
		return button;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param buttonName the button name
	 * @return the j radio button
	 */
	public JRadioButton createRadioButton(
		final String buttonName) {

		JRadioButton button = new JRadioButton(buttonName);
		setPlainFont(button);
		return button;
	}

	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j combo box< string>
	 */
	public JComboBox<String> createComboBox() {
		
		JComboBox<String> comboBox = new JComboBox<String>();
		setPlainFont(comboBox);
		return comboBox;		
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param options the options
	 * @return the j combo box< string>
	 */
	public JComboBox<String> createComboBox(
		final String[] options) {

		JComboBox<String> comboBox = new JComboBox<String>(options);
		setPlainFont(comboBox);
		return comboBox;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param options the options
	 * @return the j combo box< string>
	 */
	public JComboBox<String> createComboBox(
		final Vector<String> options) {

		JComboBox<String> comboBox = new JComboBox<String>(options);
		setPlainFont(comboBox);
		return comboBox;
	}	
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param checkBoxName the check box name
	 * @return the j check box
	 */
	public JCheckBox createCheckBox(
		final String checkBoxName) {

		JCheckBox checkBox = new JCheckBox(checkBoxName);
		setPlainFont(checkBox);
		return checkBox;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @param topComponent the top component
	 * @param bottomComponent the bottom component
	 * @return the j split pane
	 */
	public JSplitPane createTopBottomSplitPane(
		Component topComponent,
		Component bottomComponent) {
		
		JSplitPane splitPane 
			= new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				topComponent, 
				bottomComponent);
		
		return splitPane;
	}


	/**
	 * Creates a new UserInterface object.
	 *
	 * @param topComponent the top component
	 * @param bottomComponent the bottom component
	 * @return the j split pane
	 */
	public JSplitPane createLeftRightSplitPane(
		Component leftComponent,
		Component rightComponent) {
		
		JSplitPane splitPane 
			= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
				leftComponent, 
				rightComponent);
		
		return splitPane;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param listData the list data
	 * @return the j list< string>
	 */
	public JList<String> createList(
		Vector<String> listData) {

		JList<String> list = new JList<String>(listData);
		return list;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param listModel the list model
	 * @return the j list< string>
	 */
	public JList<String> createList(
		ListModel<String> listModel) {

		JList<String> list = new JList<String>(listModel);
		return list;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param labelName the label name
	 * @return the j label
	 */
	public JLabel createLabel(
		final String labelName) {

		JLabel label = new JLabel(labelName);
		setPlainFont(label);
		return label;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param labelName the label name
	 * @return the j label
	 */
	public JLabel createInstructionLabel(
		final String labelName) {

		JLabel label = createLabel(labelName);
		label.setForeground(INSTRUCTION_LABEL_COLOUR);
		setBoldFont(label);
		return label;		
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param top the top
	 * @param left the left
	 * @param bottom the bottom
	 * @param right the right
	 * @return the insets
	 */
	public Insets createInsets(
		int top, 
		int left, 
		int bottom, 
		int right) {
		
		Insets insets = new Insets(top, left, bottom, right);
		return insets;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @param numberOfColumns the number of columns
	 * @return the j text field
	 */
	public JTextField createTextField() {

		JTextField textField = new JTextField(DEFAULT_TEXT_FIELD_WIDTH);
		return textField;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param numberOfColumns the number of columns
	 * @return the j text field
	 */
	public JTextField createNonEditableTextField() {

		JTextField textField = createTextField();
		textField.setEditable(false);
		textField.setBackground( (new JLabel()).getBackground());
		return textField;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param numberOfRows the number of rows
	 * @param numberOfColumns the number of columns
	 * @return the j text area
	 */
	public JTextArea createTextArea() {
		
		JTextArea textArea 
			= new JTextArea(DEFAULT_TEXT_AREA_HEIGHT, DEFAULT_TEXT_FIELD_WIDTH);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		return textArea;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param numberOfRows the number of rows
	 * @param numberOfColumns the number of columns
	 * @return the j text area
	 */
	public JTextArea createNonEditableTextArea(
		final int numberOfRows, 
		final int numberOfColumns) {
		
		JTextArea textArea = createTextArea();		
		textArea.setEditable(false);
		//textArea.setBackground( (new JLabel()).getBackground());
		
		textArea.putClientProperty("Nimbus.Overrides", uiDefaults);
		textArea.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		textArea.setBackground((new JLabel()).getBackground());
				
		return textArea;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j editor pane
	 */
	public JEditorPane createHTMLEditorPane() {
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		return editorPane;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j text pane
	 */
	public JTextPane createHTMLTextPane() {
		
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		return textPane;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param component the component
	 * @return the j scroll pane
	 */
	public JScrollPane createScrollPane(
		final Component component) {

		JScrollPane scrollPane = new JScrollPane(component);
		return scrollPane;
	}

	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j panel
	 */
	public JPanel createBorderLayoutPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		return panel;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j panel
	 */
	public JPanel createPanel() {
		
		JPanel panel = new JPanel(new GridBagLayout());
		return panel;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j panel
	 */
	public JPanel createIndentedPanel() {
		
		JPanel panel = new JPanel(new GridBagLayout());

		EmptyBorder emptyBorder = new EmptyBorder(0, 50, 0, 0);
		panel.setBorder(emptyBorder);
		
		return panel;		
	}
	
	/**
	 * Sets the plain font.
	 *
	 * @param component the new plain font
	 */
	public void setPlainFont(Component component) {
		
		Font font = component.getFont();
		font = font.deriveFont(Font.PLAIN);
		component.setFont(font);		
	}
	
	/**
	 * Sets the bold font.
	 *
	 * @param component the new bold font
	 */
	public void setBoldFont(Component component) {
		
		Font font = component.getFont();
		font = font.deriveFont(Font.BOLD);
		component.setFont(font);
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the empty border
	 */
	public EmptyBorder createIndentedEmptyBorder() {
		
		EmptyBorder emptyBorder = new EmptyBorder(0, 0, 50, 0);
		return emptyBorder;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the grid bag constraints
	 */
	public GridBagConstraints createGridBagConstraints() {
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.anchor = GridBagConstraints.NORTHWEST;

		return constraints;
	}
		
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param title the title
	 * @return the j frame
	 */
	public JFrame createFrame(
		final String title) {
		
		JFrame frame = new JFrame(title);
		return frame;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param title the title
	 * @return the j dialog
	 */
	public JDialog createDialog(
		final String title) {
		
		JDialog dialog = new JDialog();
		dialog.setTitle(title);
		return dialog;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j file chooser
	 */
	public JFileChooser createFileChooser() {
		
		JFileChooser fileChooser = new JFileChooser();
		return fileChooser;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j tabbed pane
	 */
	public JTabbedPane createTabbedPane() {
		
		JTabbedPane tabbedPane = new JTabbedPane();
		return tabbedPane;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the j tree
	 */
	public JTree createTree() {
		
		JTree tree = new JTree();
		return tree;
	}
	
	/**
	 * Creates a new UserInterface object.
	 *
	 * @param tableModel the table model
	 * @return the j table
	 */
	public JTable createTable(
		TableModel tableModel) {
		
		JTable table = new JTable(tableModel);
		return table;
	}
	
	
	public JTableHeader createTableHeader() {
		JTableHeader tableHeader = new JTableHeader();
		return tableHeader;
	}
	/**
	 * Creates a new UserInterface object.
	 *
	 * @return the button group
	 */
	public ButtonGroup createButtonGroup() {
		
		ButtonGroup buttonGroup = new ButtonGroup();
		return buttonGroup;
	}
		
// ==========================================
// Section Errors and Validation
// ==========================================

// ==========================================
// Section Interfaces
// ==========================================

// ==========================================
// Section Override
// ==========================================

}
