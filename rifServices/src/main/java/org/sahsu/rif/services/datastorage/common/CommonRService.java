package org.sahsu.rif.services.datastorage.common;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.rosuda.JRI.Rengine;
import org.sahsu.rif.generic.concepts.Parameter;
import org.sahsu.rif.generic.util.RIFLogger;
import org.sahsu.rif.services.concepts.CalculationMethod;

public abstract class CommonRService implements RService {

	private static final RIFLogger rifLogger = RIFLogger.getLogger();

	private List<Parameter> parameters;

	public CommonRService() {
		parameters = new ArrayList<>();
	}

	@Override
	public void addParameter(final String name, final String value) {
		Parameter parameter = Parameter.newInstance(name, value);
		parameters.add(parameter);
	}
	
	@Override
	public void addParameters(final List<Parameter> _parameters) {
		this.parameters.addAll(_parameters);		
	}

	@Override
	public void setUser(final String userID, final String password) {

		addParameter("userID", userID);
		addParameter("password", password);
	}
	
	@Override
	public void setODBCDataSourceName(final String odbcDataSourceName) {

		addParameter("odbcDataSource", odbcDataSourceName);
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
	public List<Parameter> getParameters() {

		return(parameters);
	}
	
	// Source R script
	@Override
	public void sourceRScript(Rengine rengine, Path script)
		throws Exception {
			
		if (script.toFile().exists()) {
			
			String scriptString = script.toString();

			// Need to double-escape Windows path separator, or things get confused when we pass
			// the file to R.
			if (SystemUtils.IS_OS_WINDOWS) {

				scriptString = scriptString.replace("\\","\\\\");
				rifLogger.info(this.getClass(), "Source(" + File.separator + "): '"
				                                + scriptString + "'");
			}
			else {
				rifLogger.info(this.getClass(), "Source: '" + scriptString + "'");
			}
			rengine.eval("source('" + scriptString + "')");
			rifLogger.info(this.getClass(), "Done: '" + scriptString + "'");
		}
		else {
			throw new Exception("Cannot find R script: '" + script + "'");
		}
	}
}
