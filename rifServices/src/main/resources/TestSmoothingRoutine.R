##Validate the command line arguments
args=(commandArgs(TRUE))

if(length(args)==0){
  print("No arguments supplied.")
  #stop("No argument")
  ##supply default values
  model = 'CAR'
}else{
  for(i in 1:length(args)){
    eval(parse(text=args[[i]]))    
  }
}
