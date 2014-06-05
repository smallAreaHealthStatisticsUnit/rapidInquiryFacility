RIF.resizable = function(){

	var rightCol = function(){
		/*if($('#leftcol').width() > 500) {
			$('#rightcol').css('margin-left','35%');
		};*/
	}();

	var resizable = {
		
		clientH: screen.height,
		clientW: screen.width,
		svgLeafletTranslateX: 29,
		rtime: new Date(1, 1, 2000, 12,00,00),
        timeout: false,
        delta: 200,
		
		data: function(){
		    $("#data").resizable({
                handles: "n",
                resize: function (event, ui) {
                    ui.size.width = ui.originalSize.width;
					resizable.fire('resizeTable');
                }
            });
		}(),
		
		leftCol: function(){
			$("#leftcol").resizable({
                handles: "e",
                resize: function (event, ui) {
                    ui.size.height = ui.originalSize.height;
                    document.getElementById('rightcol').setAttribute("style", "margin-left:" + ui.size.width + "px");
					 resizable.rtime = new Date();
					if (resizable.timeout === false) {
						resizable.timeout = true;
						setTimeout(resizable.resizeend, resizable.delta);
					}
                }
            });
		}(),
		
		resizeend: function() {
			if (new Date() - resizable.rtime < resizable.delta) {
				setTimeout(resizable.resizeend, resizable.delta);
			} else {
				resizable.timeout = false;
				resizable.fire('resizeMap');
			}               
		}
			
    };

	
	return resizable;
};	