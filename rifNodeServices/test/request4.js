// Not using request!

var FormData = require('form-data');
var fs = require('fs');
 
var form = new FormData();
form.append('verbose', 'true');


		json_file2 = fs.createReadStream('./data/helloworld.js.gz');
		var data = new Buffer('');
		var chunks = [];
		var chunk;

		json_file2.on('readable', function() {
			while ((chunk=json_file2.read()) != null) {
				chunks.push(chunk);
			}
		});

		json_file2.on('end', function() {
			data=Buffer.concat(chunks)
			console.log('Gzipped binary stream: ' + data.toString('hex').substring(0, 132))
		});	

form.append('my_file', fs.createReadStream('./data/helloworld.js.gz'), {
	filename: 'helloworld2.js.gz',
	ContentType: 'image/gzip', 
	ContentTransferEncoding: 'gzip', 
	TransferEncoding: 'gzip', 
	ContentEncoding: 'gzip', 
	AcceptEncoding: "gzip,zip,zlib",
	knownLength: data.length});
		
form.submit('http://127.0.0.1:3000/zipfile',
  function (error, response) {
    if (error) {
      return console.error('upload failed:', error);
    }
    console.log('Upload successful!  Server responded with:', JSON.stringify(response.headers, null, 4));
	response.on('data', (chunk) => {
		console.log(`BODY: ${chunk}`);
	});
	response.on('end', () => {
		console.log('No more data in response.')
	})
	response.resume();
  })