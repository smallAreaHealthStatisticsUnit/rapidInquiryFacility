RIF.table['event-areaSelection'] = (function(_dom, firer) {
   var slctd = [],
      isMouseDown = false,
      isHighlighted;


   _dom.areaSelectionWrapper.on("mousedown", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      if (!row.hasClass('aSR')) {
         return false;
      };
      isMouseDown = true;

      $(row).toggleClass("rowSelected");
      isHighlighted = $(row).hasClass("rowSelected");
      return false; // prevent text selection
   }).on("mouseover", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      if (!row.hasClass('aSR')) {
         return false;
      };
      if (isMouseDown) {
         $(row).toggleClass("rowSelected", isHighlighted);
      }
   });

   _dom.areasSelectionDialog.on("mouseup", _dom.areaSelectionWrapper, function(aEvent) {
      if (isMouseDown) {
         var slctd = [];
         var r = d3.selectAll('#areaSelectionWrapper .rowSelected').each(function(d, i) {
            var idLabel = $(this).children();
            var gid = this.id.split('_')[1];
            slctd.push({
               id: $(idLabel[0]).text(),
               gid: gid, // USING GID FOR NOW RATHER THAN THE ACTUAL AREA ID ABOVE    
               label: $(idLabel[1]).text()
            });

         });

         firer.studyAreaSelectionEvent(slctd);
         _dom.studyAreaCount.innerHTML = slctd.length;
      }
      isMouseDown = false;
   });


});