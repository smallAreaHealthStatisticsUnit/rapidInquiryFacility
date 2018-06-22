package org.sahsu.rif.services.datastorage.common;

import java.io.File;
import java.util.ArrayList;

import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.concepts.User;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.CalculationMethod;

public abstract class CommonRService implements RService {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();
		
	private String odbcDataSourceName;
	private String userID;
	private String password;
	
	private ArrayList<Parameter> parameters;	
	private ArrayList<String> parametersToVerify;

	User user;

	public CommonRService() {
		parameters = new ArrayList<>();
		
		parametersToVerify = new ArrayList<>();
	}

	@Override
	public void addParameter(final String name, final String value) {
		Parameter parameter = Parameter.newInstance(name, value);
		parameters.add(parameter);
	}
	
	@Override
	public void addParameters(final ArrayList<Parameter> _parameters) {
		this.parameters.addAll(_parameters);		
	}
	
	@Override
	public void addParameterToVerify(final String parameterToVerify) {
		parametersToVerify.add(parameterToVerify);
	}

	@Override
	public void addParameterToVerify(final ArrayList<String> _parametersToVerify) {
		parametersToVerify.addAll(_parametersToVerify);
	}
		
	@Override
	public void setUser(final String userID, final String password) {

		user = User.newInstance(userID, password);

		this.userID = userID;
		this.password = password;
	}
	
	@Override
	public void setODBCDataSourceName(final String odbcDataSourceName) {
		this.odbcDataSourceName = odbcDataSourceName;
	}	

	private String getRRoutineModelCode(String proc) {
		String model = "NONE";
		switch (proc) {
			case "het_r_procedure":
				model = "HET";
				break;
			case "car_r_procedure":
				model = "CAR";
				break;
			case "bym_r_procedure":
				model = "BYM";
				break;
		}
		return model;
	}

	@Override
	public void setCalculationMethod(final CalculationMethod calculationMethod) {
		addParameter("model", getRRoutineModelCode(calculationMethod.getName()));
	}
	
	//Fetch parameters array list
	@Override
	public ArrayList<Parameter> getParameterArray() {
	
		addParameter("odbcDataSource", odbcDataSourceName);
		addParameter("userID", userID);
		addParameter("password", password);
		
		return(parameters);
	}
	
	// Source R script
	@Override
	public void sourceRScript(Rengine rengine, String scriptName) 
		throws Exception {
			
		File rScript=new File(scriptName);
		if (rScript.exists()) {
			String nScriptName=scriptName;
			if (File.separatorChar == '\\') { // Windooze!!! R path strings need to be escaped; they must go through a shell 
											  // like runtime at some point
				nScriptName=scriptName.replace("\\","\\\\");
				rifLogger.info(this.getClass(), "Source(" + File.separator + "): '" + nScriptName + "'");
			}
			else {
				rifLogger.info(this.getClass(), "Source: '" + nScriptName + "'");
			}
			rengine.eval("source('" + nScriptName + "')");
		}
		else {
			throw new Exception("Cannot find R script: '" + scriptName + "'");
		}
	}
}
