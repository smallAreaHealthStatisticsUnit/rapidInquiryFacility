---
layout: default
title: Setting Up R Packages
---

Start *R* in an Administrator command window and run the following script:

```R
# CHECK & AUTO INSTALL MISSING PACKAGES
packages <- c("pryr", "plyr", "abind", "maptools", "spdep", "RODBC", "RJDBC", "MatrixModels", "rJava", "here")
if (length(setdiff(packages, rownames(installed.packages()))) > 0) {
  install.packages(setdiff(packages, rownames(installed.packages())))
}
if (!require(INLA)) {
	install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
}

```

* If R cannot be found, add it to the PATH and restart the administrator Window
* If R asks if you want to use a personal library you are not running as Administrator, do *NOT* accept this.
  The script will fail. These R libraries *NUST* be installed for all users.
* R will ask for the nearest CRAN (R code archive); select one geographically near you (e.g. same country).
* R output (version numbers will be higher as you always get the latest version):

```
--- Please select a CRAN mirror for use in this session ---
also installing the dependencies 'gtools', 'gdata', 'Rcpp', 'sp', 'LearnBayes', 'deldir', 'coda', 'gmodels', 'expm'


  There are binary versions available but the source versions are later:
       binary source needs_compilation
deldir 0.1-12 0.1-14              TRUE
spdep  0.6-12 0.6-13              TRUE

Do you want to install from sources the packages which need compilation?
y/n: if (!require(INLA)) {
trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gtools_3.5.0.zip'
Content type 'application/zip' length 144014 bytes (140 KB)
downloaded 140 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gdata_2.17.0.zip'
Content type 'application/zip' length 1178306 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/Rcpp_0.12.10.zip'
Content type 'application/zip' length 3261850 bytes (3.1 MB)
downloaded 3.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/sp_1.2-4.zip'
Content type 'application/zip' length 1528674 bytes (1.5 MB)
downloaded 1.5 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/LearnBayes_2.15.zip'
Content type 'application/zip' length 1129565 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/deldir_0.1-12.zip'
Content type 'application/zip' length 171603 bytes (167 KB)
downloaded 167 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/coda_0.19-1.zip'
Content type 'application/zip' length 201300 bytes (196 KB)
downloaded 196 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/gmodels_2.16.2.zip'
Content type 'application/zip' length 73931 bytes (72 KB)
downloaded 72 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/expm_0.999-2.zip'
Content type 'application/zip' length 194188 bytes (189 KB)
downloaded 189 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/plyr_1.8.4.zip'
Content type 'application/zip' length 1121290 bytes (1.1 MB)
downloaded 1.1 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/abind_1.4-5.zip'
Content type 'application/zip' length 40002 bytes (39 KB)
downloaded 39 KB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/maptools_0.9-2.zip'
Content type 'application/zip' length 1818632 bytes (1.7 MB)
downloaded 1.7 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/spdep_0.6-12.zip'
Content type 'application/zip' length 3819364 bytes (3.6 MB)
downloaded 3.6 MB

trying URL 'https://cran.ma.imperial.ac.uk/bin/windows/contrib/3.2/RODBC_1.3-15.zip'
Content type 'application/zip' length 829585 bytes (810 KB)
downloaded 810 KB

package 'gtools' successfully unpacked and MD5 sums checked
package 'gdata' successfully unpacked and MD5 sums checked
package 'Rcpp' successfully unpacked and MD5 sums checked
package 'sp' successfully unpacked and MD5 sums checked
package 'LearnBayes' successfully unpacked and MD5 sums checked
package 'deldir' successfully unpacked and MD5 sums checked
package 'coda' successfully unpacked and MD5 sums checked
package 'gmodels' successfully unpacked and MD5 sums checked
package 'expm' successfully unpacked and MD5 sums checked
package 'plyr' successfully unpacked and MD5 sums checked
package 'abind' successfully unpacked and MD5 sums checked
package 'maptools' successfully unpacked and MD5 sums checked
package 'spdep' successfully unpacked and MD5 sums checked
package 'RODBC' successfully unpacked and MD5 sums checked

The downloaded binary packages are in
        C:\Users\admin\AppData\Local\Temp\RtmpSkeuRW\downloaded_packages
> install.packages("INLA", repos="https://www.math.ntnu.no/inla/R/stable")
Warning: dependency 'MatrixModels' is not available
trying URL 'https://www.math.ntnu.no/inla/R/stable/bin/windows/contrib/3.2/INLA_0.0-1485844051.zip'
Content type 'application/zip' length 93004915 bytes (88.7 MB)
downloaded 88.7 MB


The downloaded binary packages are in
        C:\Users\admin\AppData\Local\Temp\RtmpSkeuRW\downloaded_packages
```
