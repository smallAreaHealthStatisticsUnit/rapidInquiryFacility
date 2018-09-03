* Install R package 'rJava'
* Set the Environmental Variables via Control Panel so PATH contains:
  * R library location\rJava\jri\x64 
  * Program Files\R\R-3.X.X\bin\x64

* In Eclipse download the R Java Eclipse Plugin and copy to Program Files\ ... Eclipse\plugins

* Set Eclipse > Preferences > RJAVA
  * JRI DLL: rlibrray\rJava\jri\x64
  * JRI JARS: rlibrray\rJava\jri
  * R DLL: Program Files\R\R-3.X.X\bin\x64
  * JVM DLL: Program Files\Java\jre7\bin\server

* For R, .libPaths() must be set as R_LIBS_USER Envioronment Variable
* When installing R packages for use with JRI, run R in Admin mode




