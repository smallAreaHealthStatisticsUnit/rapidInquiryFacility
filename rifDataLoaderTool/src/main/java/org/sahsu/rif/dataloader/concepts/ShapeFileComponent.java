package org.sahsu.rif.dataloader.concepts;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;

/**
 *
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
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

public enum ShapeFileComponent {
	SHP("shp", "shapeFile.shp.toolTipText"),
	SHX("shx", "shapeFile.shx.toolTipText"),
	DBF("dbf", "shapeFile.dbf.toolTipText"),
	PRJ("prj", "shapeFile.prj.toolTipText"),
	SBX("sbx", null),
	SBN("sbn"),
	FBN("fbn"),
	FBX("fbx"),
	AIN("ain"),
	AIH("aih"),
	IXS("ixs"),
	MXS("mxs"),
	ATX("atx"),
	CPG("cpg"),
	QIX("qix");
		
	private String fileExtension;
	private String fileExtensionDescriptionProperty;

	private ShapeFileComponent(
		final String fileExtension) {
			
		this.fileExtension = "." + fileExtension;
	}
	
	private ShapeFileComponent(
		final String fileExtension,
		final String fileExtensionDescriptionProperty) {
		
		this.fileExtension = "." + fileExtension;
		this.fileExtensionDescriptionProperty = fileExtensionDescriptionProperty;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public String getFileExtensionDescription() {
		if (fileExtensionDescriptionProperty == null) {
			return null;
		}
		else {
			return RIFDataLoaderToolMessages.getMessage(fileExtensionDescriptionProperty);
		}
	}
	
	public boolean isFileComponent(final String filePath) {
		
		String lowerCaseFilePath = filePath.toLowerCase();
		return lowerCaseFilePath.endsWith(fileExtension);
	}
	
	public static final ShapeFileComponent getShapeFileComponent(
		final String shapeFileComponentPath) {
		
		if (SHP.isFileComponent(shapeFileComponentPath)) {
			return SHP;
		}
		else if (SHP.isFileComponent(shapeFileComponentPath)) {
			return SHP;
		}
		else if (SHX.isFileComponent(shapeFileComponentPath)) {
			return SHX;
		}
		else if (DBF.isFileComponent(shapeFileComponentPath)) {
			return DBF;
		}
		else if (PRJ.isFileComponent(shapeFileComponentPath)) {
			return PRJ;
		}
		else if (SBX.isFileComponent(shapeFileComponentPath)) {
			return SBX;
		}
		else if (SBN.isFileComponent(shapeFileComponentPath)) {
			return SBN;
		}
		else if (FBN.isFileComponent(shapeFileComponentPath)) {
			return FBN;
		}
		else if (FBX.isFileComponent(shapeFileComponentPath)) {
			return FBX;
		}
		else if (AIN.isFileComponent(shapeFileComponentPath)) {
			return AIN;
		}
		else if (AIH.isFileComponent(shapeFileComponentPath)) {
			return AIH;
		}
		else if (IXS.isFileComponent(shapeFileComponentPath)) {
			return IXS;
		}
		else if (MXS.isFileComponent(shapeFileComponentPath)) {
			return MXS;
		}
		else if (ATX.isFileComponent(shapeFileComponentPath)) {
			return ATX;
		}
		else if (CPG.isFileComponent(shapeFileComponentPath)) {
			return CPG;
		}
		else if (QIX.isFileComponent(shapeFileComponentPath)) {
			return QIX;
		}
		else {
			return null;
		}
	}
}


