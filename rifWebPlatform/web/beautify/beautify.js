var beautify = require('js-beautify').js_beautify,
    fs = require('fs');

var reformat = function( filePath ){
	fs.readFile( filePath , 'utf8', function (err, data) {
		if (err) {
			throw err;
		}
		
		var outputFilename = filePath;
		fs.writeFile(outputFilename, beautify(data, { indent_size: 2, space_in_paren: 1, keep_array_indentation: 1  }) , function(err) {
			if(err) {
				console.log(err);
			} else {
				console.log(" saved to " + outputFilename);
			}
		}); 
	});
}

var walk = function(dir, done) {
  var results = [];
  fs.readdir(dir, function(err, list) {
    if (err) return done(err);
    var pending = list.length;
    if (!pending) return done(null, results);
    list.forEach(function(file) {
      file = dir + '/' + file;
      fs.stat(file, function(err, stat) {
        if (stat && stat.isDirectory()) {
          walk(file, function(err, res) {
            results = results.concat(res);
            if (!--pending) done(null, results);
          });
        } else {
          results.push(file);
          if (!--pending) done(null, results);
        }
      });
    });
  });
};

var callback = function(err, results) {
  if (err) console.log( err );
  var l = results.length;	
  while(l--){
	console.log(results[l]);
	reformat(results[l]);
  }	
};


walk( "../dashboards/dataViewer/js", callback );
walk( "../dashboards/diseaseMapping/js", callback );
walk( "../dashboards/diseaseSubmission/js", callback );
walk( "../dashboards/dataViewer/js", callback );
walk( "../logIn/js", callback );