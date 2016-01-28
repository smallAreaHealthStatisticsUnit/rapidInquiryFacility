// Not using request!

var FormData = require('form-data');
var fs = require('fs');
var zlib = require('zlib');
var inspect = require('util').inspect;

var form = new FormData();

form.append('verbose', 'true');

var zstream=zlib.createDeflate();
var rstream=fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js');
var wstream=fs.createWriteStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77');

/*
var chunks2 = [];
var chunk2;

zstream.on('readable', function() {
	while ((chunk2=zstream.read()) != null) {
		chunks2.push(chunk2);
	}
});
zstream.on('end', function() {
	data=Buffer.concat(chunks2)
	console.log("zstream binary stream(" + data.length + "): " + data.toString('hex').substring(0, 132));
}); */
rstream.pipe(zstream).pipe(wstream);
wstream.on('finish', function() {
	json_file2 = fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77');
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
		console.log("Lz77 (zlib) binary stream(" + data.length + "): " + data.toString('hex').substring(0, 132))


		console.error("Stream 1: test_6_sahsu_4_level4_0_0_0.js.gz")
		form.append('my_file 1', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.gz'), {
			filename: 'test_6_sahsu_4_level4_0_0_0.js.gz',
			ContentType: 'application/gzip', 
			ContentTransferEncoding: 'gzip', 
			TransferEncoding: 'gzip', 
			ContentEncoding: 'gzip', 
			AcceptEncoding: "gzip,zip,zlib"});
			
		console.error("Stream 2: test_6_sahsu_4_level4_0_0_0.js.gz")
		form.append('my_file 2', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.gz'), {
			filename: 'test_6a_sahsu_4_level4_0_0_0.js.gz'});	

		console.error("Stream 3: test_6_sahsu_4_level4_0_0_0.js.lz77")	
		form.append('my_file 3', fs.createReadStream('./data/test_6_sahsu_4_level4_0_0_0.js.lz77'), {
			filename: 'test_6a_sahsu_4_level4_0_0_0.js.lz77',
			ContentType: 'application/zlib', 
			ContentTransferEncoding: 'zlib', 
			TransferEncoding: 'zlib', 
			ContentEncoding: 'zlib', 
			AcceptEncoding: "gzip,zip,zlib",
			knownLength: data.length}); 
			
		console.error("SUBMIT");	
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
	});		  
});