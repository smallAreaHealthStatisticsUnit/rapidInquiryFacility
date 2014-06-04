(function ($) {
  /***
   * A sample AJAX data store implementation.
   * Right now, it's hooked up to load Hackernews stories, but can
   * easily be extended to support any JSONP-compatible backend that accepts paging parameters.
   */
  function RemoteModel( parent ) { /* Passing Table renderer context*/
    // private
    var PAGESIZE = 5000;
	var optimalRequestRangeSize = 1000;
    var data = {length: 0};
    var searchstr = "";
    var sortcol = null;
    var sortdir = 1;
    var request = null;
    var req = null; // ajax request

    // events
    var onDataLoading = new Slick.Event();
    var onDataLoaded = new Slick.Event();


    function init() {
		optimalRequestRangeSize = parent.nRows;
		ensureData(0, parent.nRows, []);
    }


    function isDataLoaded(from, to) {
      for (var i = from; i <= to; i++) {
        if (data[i] == undefined || data[i] == null) {
          return false;
        }
      }

      return true;
    }


    function clear() {
      for (var key in data) {
        delete data[key];
      }
      data.length = 0;
    }


    function ensureData(from, to, fields) {
      if (from < 0) {from = 0;}
        while (data[from] !== undefined && from < to) {from++;}
        while (data[to] !== undefined && from < to) {to--;}
		
        // no need to load anything
        if (data[from] !== undefined) {
            return;
        }
		
		
        // A request for data must be made: increase range if below optimal request size
        // to decrease number of requests to the database
        var size = to - from + 1;
        if (size < optimalRequestRangeSize) {
            // expand range in both directions to make it equal to the optimal size
            var expansion = Math.round((optimalRequestRangeSize - size) / 2);
            from -= expansion;
            to += expansion;

            // if range expansion results in 'from' being less than 0,
            // make it to 0 and transfer its value to 'to' to keep the range size
            if (from < 0) {
                to -= from;
                from = 0;
            }

            // Slide range up or down if data is already loaded or being loaded at the top or bottom...
            if (data[from] !== undefined) {
                while (data[from] !== undefined) {
                    from++; 
                    to++;
                }
            }
            else if (data[to] !== undefined) {
                while (data[to] !== undefined && from > 0) {
                    from--; 
                    to--;
                }
            }
        }

        // After adding look-ahead and look-behind, reduce range again to only unloaded 
        // data by eliminating already loaded data at the extremes
        while (data[from] !== undefined && from < to) {from++;}
        while (data[to] !== undefined && from < to) {to--;}

        // clear any pending request
        if ( request !== null)
            clearTimeout( request);

        request = setTimeout(function () {
			 
			 for (var i = from; i <= to; i++) {
                if (!data[i]) {
                    data[i] = null; 
                }
            }
			
			onDataLoading.notify({from: from, to: to});
			parent.request(from, to + 1);
			
      }, 100);
    }


    function onError(fromPage, toPage) {
      alert("error loading pages " + fromPage + " to " + toPage);
    }


    function reloadData(from, to) {
      for (var i = from; i <= to; i++)
        delete data[i];

      ensureData(from, to);
    }


    function setSort(column, dir) {
      sortcol = column;
      sortdir = dir;
      clear();
    }

    function setSearch(str) {
      searchstr = str;
      clear();
    }


    init();

    return {
      // properties
      "data": data,

      // methods
      "clear": clear,
      "isDataLoaded": isDataLoaded,
      "ensureData": ensureData,
      "reloadData": reloadData,
      "setSort": setSort,
      "setSearch": setSearch,

      // events
      "onDataLoading": onDataLoading,
      "onDataLoaded": onDataLoaded
    };
  }

  // Slick.Data.RemoteModel
  $.extend(true, window, { Slick: { Data: { RemoteModel: RemoteModel }}});
})(jQuery);
