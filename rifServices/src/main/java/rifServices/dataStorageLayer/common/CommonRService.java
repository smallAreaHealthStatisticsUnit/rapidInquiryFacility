package rifServices.dataStorageLayer.common;

import java.util.ArrayList;

import rifGenericLibrary.businessConceptLayer.Parameter;
import rifServices.businessConceptLayer.CalculationMethod;
import rifServices.dataStorageLayer.common.RService;

public abstract class CommonRService implements RService {

	private String odbcDataSourceName;
	private String userID;
	private String password;
	
	private ArrayList<Parameter> parameters;	
	private ArrayList<String> parametersToVerify;
	
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
}
