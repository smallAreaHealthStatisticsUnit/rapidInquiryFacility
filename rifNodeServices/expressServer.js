var express = require('express'),
    toTopojson = require('./routes/toTopojson'),
    simplify = require('./routes/simplify'),
    busboy = require('connect-busboy');


var app = express();
// default options, no immediate parsing 

app.use(busboy({
  highWaterMark: 2000 * 1024 * 1024,
  limits: {
    fileSize: 1000 * 1024 * 1024
  }
}));

app.post('/toTopojson', toTopojson.convert);
app.post('/simplify', simplify.convert);
 
app.listen(3000);

console.log('Listening on port 3000...');