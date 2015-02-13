RIF.resizable = function(studyType) {
   var rightCol = function() {
      /*if($('#leftcol').width() > 500) {
			$('#rightcol').css('margin-left','35%');
		};*/
   }();
   var resizable = {
      clientH: screen.height,
      clientW: screen.width,
      svgLeafletTranslateX: 29,
      rtime: new Date(1, 1, 2000, 12, 00, 00),
      timeout: false,
      delta: 200,
      rr_chart: function() {
         var rr_chart = $("#rr_chart");
         if (rr_chart.hasClass("ui-resizable")) {
            rr_chart.resizable("destroy");
         };
         rr_chart.resizable({
            handles: "n",
            resize: function(event, ui) {
               ui.size.width = ui.originalSize.width;
               resizable.resizedBit = "rr_chart";
               resizable.rtime = new Date();
               if (resizable.timeout === false) {
                  resizable.timeout = true;
                  setTimeout(resizable.resizeend, resizable.delta);
               }
            }
         });
      },
      leftCol: function() {
         $("#leftcol").resizable({
            handles: "e",
            resize: function(event, ui) {
               ui.size.height = ui.originalSize.height;
               resizable.resizedBit = "leftcol";
               document.getElementById('rightcol').setAttribute("style", "margin-left:" + ui.size.width + "px");
               resizable.rtime = new Date();
               if (resizable.timeout === false) {
                  resizable.timeout = true;
                  setTimeout(resizable.resizeend, resizable.delta);
               }
            }
         });
      }(),
      multipleAreaCharts: function() {
         var areaCharts = $("#mAreaCharts");
         if (areaCharts.hasClass("ui-resizable")) {
            areaCharts.resizable("destroy");
         };
         var studyInfoHeight = $("#studyInfo").outerHeight(false),
            studyLabelHeight = $("#studyLabel").outerHeight(false),
            leftOverHeight = parseInt($("#leftcol").outerHeight(false) - (studyInfoHeight + studyLabelHeight));
         //areaCharts.height(leftOverHeight);
         areaCharts.resizable({
            handles: "n",
            // maxHeight: 800,//leftOverHeight + studyInfoHeight  - 20,
            resize: function(event, ui) {
               ui.size.height = ui.originalSize.height;
               resizable.resizedBit = "mAreaCharts";
               resizable.rtime = new Date();
               if (resizable.timeout === false) {
                  resizable.timeout = true;
                  setTimeout(resizable.resizeend, resizable.delta);
               }
            }
         });
      },
      resizeend: function() {
         if (new Date() - resizable.rtime < resizable.delta) {
            setTimeout(resizable.resizeend, resizable.delta);
         } else {
            resizable.timeout = false;
            if (resizable.resizedBit === "rr_chart") {
               resizable.fire('resizeLineBivariateChart', []);
            } else if (resizable.resizedBit == "mAreaCharts") {
               resizable.fire('resizeAreaCharts', []);
            } else if (resizable.resizedBit == "leftcol") {
               resizable.fire('resizeAreaCharts', []);
               resizable.fire('resizeLineBivariateChart', []);
            }
         }
      }
   };
   return resizable;
};