RIF['map']['event-studyArea'] = (function(dom, firer) {


   //handle clicks here?
   /*$( '#' + dom.id).on( "click", 'path', function ( aEvent ) {
        console.log(aEvent.target)  
    });*/

   $('#' + dom.id).on("mousemove", 'path', function(aEvent) {
      var xPos = aEvent.pageX + 40,
         yPos = aEvent.pageY - 50;
      dom.tooltip.css({
         'top': yPos,
         'left': xPos,
         'display': 'block'
      });
   });

   $('#' + dom.id).on("mouseout", function(aEvent) {
      dom.tooltip.hide();
   });

});