(function(){

     var utils = {
	 
		initComponents: function () { /* context passed by calling object */
			for (var c in this.components) {
				this[c] = RIF[c](this.components[c]);
				RIF.makePublisher(this[c]);
			}
		},

		addEvents: function () { /* context passed by calling object */
			var evts = this.events;
			for (var c in evts) {
				var f = evts[c].firer,
					fl = f.length;
				while (fl--) {
					var s = evts[c].subscribers,
						sl = s.length;
					while (sl--) {
						this[f[fl]].on(c, evts[c].method, this[s[sl]]);
					}
				}
			}
		},

		makePublisher: function (o) {
			var i;
			for (i in RIF.publisher) {
				if (RIF.publisher.hasOwnProperty(i) && typeof RIF.publisher[i] === "function") {
					o[i] = RIF.publisher[i];
				}
			}
			o.subscribers = {
				any: []
			};
		},

		extend: function (parent, child) {
			var i,
				toStr = Object.prototype.toString,
				astr = "[object Array]";
			child = child || {};
			for (i in parent) {
				if (parent.hasOwnProperty(i)) {
					if (typeof parent[i] === 'object') {
						child[i] = (toStr.call(parent[i]) === astr) ? [] : {};
						this.extend(parent[i], child[i]);
					} else {
						child[i] = parent[i];
					}
				}
			}
			return child;
		},
		
		removeG: function(id){
			return id.replace("g", '');
		},
		
		addG: function(idN){
			return "g" + idN;;
		},
		
		object: function (o) {
			function F() {};
			F.prototype = o;
			return new F();
		},

		mix: function () {
			var arg, prop, child = {};
			for (arg = 0; arg < arguments.length; arg += 1) {
				for (prop in arguments[arg]) {
					if (arguments[arg].hasOwnProperty(prop)) {
						child[prop] = arguments[arg][prop];
					}
				}
			}
			return child;
		},
		
		replaceAll: function(find, replace, str) {
			return str.replace(new RegExp(find, 'g'), replace);
		},
		
		splitId: function(id){
		    var d = id.split('__');
			return [d[0],d[1]];
		},
		
		arraysEqual: function(arr1, arr2) {
			return $(arr1).not(arr2).length == 0 && $(arr2).not(arr1).length == 0 ;
		},
		
		xhr: function () {
			/* 
				   mime Types : https://github.com/mbostock/d3/wiki/Requests#wiki-d3_json
				   args : url ,  callback , [mime]
				   Implement MORE OPTIONS!
				*/
			var args = Array.prototype.slice.call(arguments, 0),
				mime = args[2] || "text/plain",
				url = "backend/gets/" + args[0];
			
			d3.xhr(url, mime, args[1]);
			
			/*var options = $.extend(options || {}, {
				dataType: "json",
				cache: false,
				url: "backend/gets/" +  args[0],
				success: function(){
				    return args[1].call(this)
				},
				error:function(xhr, ajaxOptions, thrownError) {
					alert("An Error occured"+xhr.status);
					console.log(xhr)
				}
		    };
			jQuery.ajax( url , options );*/
			
		}
	};

    utils.extend( utils, RIF);	
}());