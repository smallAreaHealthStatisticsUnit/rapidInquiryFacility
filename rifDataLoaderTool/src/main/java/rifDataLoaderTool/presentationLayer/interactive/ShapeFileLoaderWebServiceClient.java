package rifDataLoaderTool.presentationLayer.interactive;

import com.sun.jersey.api.client.Client;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.*;
import javax.ws.rs.core.*;
import java.io.*;

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

public class ShapeFileLoaderWebServiceClient {

	public static void main(String[] arguments) {
		
		
		try {
			
		ShapeFileLoaderWebServiceClient client
			= new ShapeFileLoaderWebServiceClient();
		
		client.test2();
		}
		catch(Exception exception) {
			exception.printStackTrace(System.out);
		}
	}
	
	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public ShapeFileLoaderWebServiceClient() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	
	public void test1() 
		throws Exception {
		
		Client client = Client.create();
		WebResource webResource = 
			client.resource("http://localhost:8080/sfc/shapeFileConverter/testShapeFileSubmission");
		
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
		formData.add("userID", "kgarwood");
		
		ClientResponse clientResponse
			= webResource.queryParams(formData).get(ClientResponse.class);
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code" + clientResponse.getStatus());
		}
	}

	
	public void test2() 
		throws Exception {
		
		Client client = Client.create();
		WebResource webResource = 
			client.resource("http://localhost:8080/sfc/shapeFileConverter/submitShapeFiles");
		
	    InputStream goodreadsImport = this.getClass().getResourceAsStream(
	    	"C://jersey_examples//uk_shape_file.zip");
	    
	    //BufferedInputStream bufferedIntputStream
	    	//= new BufferedInputStream(new FileInputStream("C://jersey_examples//uk_shape_file.zip"));

	    BufferedInputStream bufferedIntputStream
	    	= new BufferedInputStream(new FileInputStream("C://jersey_examples//shp5.zip"));
	    
	    
	    FormDataMultiPart form = new FormDataMultiPart();
	    form.field("userID", "kgarwood");
	    FormDataBodyPart fdp = new FormDataBodyPart("fileField",
	    		bufferedIntputStream,
	            MediaType.APPLICATION_OCTET_STREAM_TYPE);
	    form.bodyPart(fdp);
	    ClientResponse clientResponse 
	    	= webResource.type(MediaType.MULTIPART_FORM_DATA).put(ClientResponse.class, form);		
		if (clientResponse.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code" + clientResponse.getStatus());
		}
		
		String output = clientResponse.getEntity(String.class);
		System.out.println("WOO HOO! output from service =="+output+"==");
		
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


