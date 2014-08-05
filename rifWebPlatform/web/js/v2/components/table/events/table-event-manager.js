RIF.table['table-event-manager'] = (function(){
		
		var _p = this; //context
		
		$(".modal_close").click(function(){
			$(".overlay").hide();
		});
				
		$(".dropdown dt > div").click(function() {
			$(".dropdown .palette").toggle();
		});
				
		$("#clearSelection").click(function() {
			_p.facade.clearMapTable();
		});
				
		$("#zoomExtent").click(function() {
			_p.facade.zoomToExtent();
		});

		$("#updateCharts").click(function() {
			_p.facade.chartUpdate();
		});	
	
});