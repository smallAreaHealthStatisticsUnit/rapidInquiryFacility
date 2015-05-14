RIF.table['event-ageGroups'] = (function(_dom, firer) {

   var menuContext = this,
      slctd = [],
      isHighlighted,
      ageGroupClicked = false,
      isMouseDown = false;


   _dom.ageGroupsWrapper.on("mousedown", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      isMouseDown = true;
      row.toggleClass("rowSelected");
      isHighlighted = row.hasClass("rowSelected");
      ageGroupClicked = true;
      return false; // prevent text selection 
   }).on("mouseover", _dom.rows, function(aEvent) {
      var row = $(aEvent.target).parent();
      if (isMouseDown) {
         $(row).toggleClass("rowSelected", isHighlighted);
      }
   }).on("mouseup", _dom.rows, function(aEvent) {
      isMouseDown = false;
   });


   _dom.investigationBox.on('mouseup', _dom.ageGroupsWrapper, (function(aEvent) {
      if (!ageGroupClicked) {
         return;
      };
      var rows = $('#ageGroupsWrapper .rowSelected');
      if (rows.length > 0) {
         var max = rows[0],
            maxageGroupName = $(max).find('.ageGroupName').text(),
            maxageGroupLimits = $(max).find('.ageBand').text();

         var min = rows[rows.length - 1],
            minageGroupName = $(min).find('.ageGroupName').text(),
            minageGroupLimits = $(min).find('.ageBand').text();

         var band = {
            lower: {
               name: minageGroupName,
               ageLimits: minageGroupLimits
            },
            upper: {
               name: maxageGroupName,
               ageLimits: maxageGroupLimits
            }
         };
         console.log(band);
         firer.ageGroupsChanged(band);
      };

      isMouseDown = false;
      ageGroupClicked = false;
   }));

});