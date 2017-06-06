
##Defaults
d <- 12.0


myFunction <- function(){
	hw <- 'Hello World'
	a <- 5
	b <- 10
	#c <- a + b
	c <- a + d
  	return(c)
}

returnGlobal <- function() {
	ev <- environmentName(environment())
	return("ev")
}

returnGlobal2 <- function() {
	return(pth)
}


//test
			//	REXP result1 = rengine.eval("extractTableName");
			//	String id  = result1.asString();
			//	System.out.println("extractTableName: "+ id);

			//use SOURCE to define functions only
			//Calling R code from ln1235 only
			//or, use entire script as function

			/*
			//number
			rengine.eval("x <- 22.7523");
			rengine.eval("x <- x * 100");
			System.out.println("Simple arithmetic: " + rengine.eval("x").asDouble());

			//string
			rengine.eval(String.format("greeting <- '%s'", "Hello R World"));
			REXP result = rengine.eval("greeting");
			System.out.println("A string variable: " + result.asString());
			 */

			//run a test script
			//"C:/Program Files/Apache Software Foundation/Tomcat 8.0/webapps/rifServices/WEB-INF/classes/TestJRI.R";				
			//	rifScriptPath.append("TestJRI.R");
			//	System.out.println("rScriptPath=="+rifScriptPath+"==");
			//rengine.eval("source(\"" + rifScriptPath + "\")");

			/*
			REXP result2 = rengine.eval("as.integer(a<-myFunction())");
			int c  = result2.asInt();
			System.out.println("Result of myFunction: "+ c);

			REXP result3 = rengine.eval("as.character(d<-returnGlobal())");
			String d  = result3.asString();
			System.out.println("String of environment: "+ d);

			String driverPath = "A_pathSomewhere";
			rengine.assign("pth", driverPath);

			rengine.eval("parameters=c(\"SQUIRREL\", \"Monkey\")");
			REXP result4 = rengine.eval("creature <- parameters[1]");
			String p = result4.asString();
			System.out.println("CREATURE: "+ p);

			REXP result5 = rengine.eval("as.character(dd<-returnGlobal2())");
			String dd  = result5.asString();
			System.out.println("Variable outside function: "+ dd);

			REXP result6 = rengine.eval("pth");
			String pth1 = result6.asString();
			System.out.println("PATH: "+ pth1);
			 */