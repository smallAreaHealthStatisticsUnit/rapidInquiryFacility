RIF4
====
JS Client Code for RIF4



--Modules:[Manager,DM,RA]
	-define relationships among components using the Observer Pattern, this is the only module initialized explicitly by the HTML document.
	-structure:
		
		 {
			_p = {
				components: {},
				events: {},
				init: function(){}
			};
			return {
				setUp:(function(){
					_p.init();
				})
			};		
		 }
		 
--

--Components:[Map,Table,Menu,Chart]	
	-building blocks, manage specific parts of the application, decoupled from one another.  
	-structure:
	
		 {
			_p = {
				init: function(){},
				firingEvents: function(){
					//Assign events to DOM objects
				},
				facade: {
					//Implement subscribed Events
				},
			};
			_p.init();
			return _p.facade;		
		 }	