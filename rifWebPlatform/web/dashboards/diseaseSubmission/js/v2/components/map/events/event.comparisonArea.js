RIF['map']['event-comparisonArea'] = (function (dom, firer) {

  $('#' + dom.id).on("mousemove", 'path', function (aEvent) {
    var xPos = aEvent.pageX + 40,
      yPos = aEvent.pageY - 50;
    dom.tooltip.css({
      'top': yPos,
      'left': xPos,
      'display': 'block'
    });
  }).on("mouseout", function (aEvent) {
    dom.tooltip.hide();
  }).on("mouseleave", function (aEvent) {
    firer.comparisonMapAreaSelectionEvent(dom.id);
  });

});