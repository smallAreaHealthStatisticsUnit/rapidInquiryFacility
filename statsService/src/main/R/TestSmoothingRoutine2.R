
tryCatch(
	print("hello world"),
	error=function(c) {
		print(paste0("error==",c))
		return(555)
	},
	warning=function(c) {
		print(paste0("warnings==",c))
		return(666)	
	},
	message=function(c) {
		print(paste0("message==",c))
		return(777)			
	},
	finally={
		print("finally code")
		return(777)	
		
	})
