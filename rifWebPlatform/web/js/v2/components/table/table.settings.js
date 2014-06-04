RIF.table.settings = (function () {

    var settings = {
		fromRow: 1,
		nRows: 500,
		fields: [],
		selectedRows: [],
		missing: [], 
		geolevel: "",
		defaultSize: {'height':'202px', 'top' : '-163px'},
		minColumnWidth: 100,
		toptions: {
			enableCellNavigation: true,
			enableColumnReorder: false,
			forceFitColumns: true 
		}
    };
	
    return settings;
});