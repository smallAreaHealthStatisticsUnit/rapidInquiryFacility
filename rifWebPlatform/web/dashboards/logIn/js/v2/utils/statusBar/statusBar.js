/*
 *  Returns an updateStatusBar fucntion.
 */

RIF.statusBar = (function() {


   var statusBarMsgs = {},
      _error = 0,

      _appendRemove = function(i, msg, showOrHide) {
         if (showOrHide) {
            $("#statusbar>div").append("<div class='info msg" + i + "' >" + msg + "</div>");
         } else {
            $(".msg" + i).remove();
         }
      },

      _showNotification = function(msg) {
         _error = 1;
         _show();
         $("#statusbar>div").append("<div class='notification' >" + msg + "</div>");
      },

      _showError = function(msg) {
         _error = 1;
         _show();
         $("#statusbar>div").append("<div class='error' >" + msg + "</div>");
      },

      _removeErrors = function() {
         _error = 0;
         _hide();
         $('.error,.notification,.info').remove();
      },

      _show = function() {
         $("#statusbar").show();
      },

      _hide = function() {
         if ($("#statusbar").hasClass("unclosable")) {
            return;
         };

         $("#statusbar").hide();
      },

      _makeUnclosable = function() {
         $("#statusbar>a").remove();
         $("#statusbar").addClass("unclosable");
      };


   /*
    *  error:
    *    -1     : signal closure of modal dialog and remove all errors in container
    *     1     : error occured, shows error modal dialog
    * 'notify'  : notification, no error and different from a status request as it is a static message
    *
    * unclosable:
    *     If true make the error modal dialog unclosable (i.e when user is not logged in and tries to access the dashboard)
    */


   var updateStatusBar = function(msg, showOrHide, error, unclosable) {

      if (error === -1) { // reset errors
         _removeErrors();
         return;
      };

      if (unclosable) {
         _makeUnclosable()
      };

      if (error == 1) { // errors notification
         _showError(msg);

         return;
      };

      if (error == 'notify') { // errors notification
         _showNotification(msg);
         return;
      };


      var indexOfMsg = -1,
         nRequests = 0,
         requestsInProgress = 0,
         requestsCompleted = 0;

      statusBarMsgs[msg] = showOrHide;

      for (var cmsg in statusBarMsgs) {
         if (msg === cmsg) {
            indexOfMsg = nRequests;
         };
         if (!statusBarMsgs[cmsg]) {
            requestsCompleted++;
         } else if (statusBarMsgs[cmsg]) {
            requestsInProgress++;
         }
         nRequests++;
      };

      if (typeof error === 'undefined') {
         _appendRemove(indexOfMsg, msg, showOrHide);
      };

      if (nRequests == requestsCompleted && _error !== 1) {
         _hide();
         return;
      };

      if (requestsInProgress == 1 || _error === 1) {
         _show();
      };
   };

   return updateStatusBar;

}());