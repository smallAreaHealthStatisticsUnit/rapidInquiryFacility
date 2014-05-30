RIF.table.settings = (function () {

    var settings = {
		fromRow: 0,
		nRows: 5000,
		fields: [],
		selectedRows: [],
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