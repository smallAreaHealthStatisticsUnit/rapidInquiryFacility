package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;

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

public enum UserRole {
	
	RIF_USER("userRole.rifUser.label"),
	RIF_MANAGER("userRole.rifManager.label"),
	RIF_STUDENT("userRole.rifStudent.label");
	
	private static final Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	private String propertyName;
	
	private UserRole(
		final String propertyName) {
		
		this.propertyName = propertyName;
	}
	
	public String getName() {
		String name
			= RIFDataLoaderToolMessages.getMessage(propertyName);
		return name;
	}
	
	public boolean isUserRole(final String userRoleName) {
		
		if (userRoleName == null) {
			return false;
		}
		
		String name = getName();
		Collator collator = GENERIC_MESSAGES.getCollator();
		return collator.equals(name, userRoleName);
	}
	
	public static UserRole getUserRole(
		final String userRoleName) {
		
		if (RIF_MANAGER.isUserRole(userRoleName)) {
			return RIF_MANAGER;
		}
		else if (RIF_USER.isUserRole(userRoleName)) {
			return RIF_USER;
		}
		else if (RIF_STUDENT.isUserRole(userRoleName)) {
			return RIF_STUDENT;
		}
		else {
			assert(false);
			return null;
		}
	}
			
	public static String[] getUserRoleNames() {
		
		String[] roleNames = new String[3];
		roleNames[0] = RIF_USER.getName();
		roleNames[1] = RIF_MANAGER.getName();
		roleNames[2] = RIF_STUDENT.getName();
		
		return roleNames;
	}
}


