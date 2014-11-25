library(spam)

BYM.UPDATE_MH=function(SET,Y,E,M,Nsimu=1000,thin=1,au=0.001,bu=0.001,av=0.001,bv=0.001,DIC=FALSE){
M=as.spam(M)
  
#fonction f pour algorithme ars
f<-function(x, A1, A2, A3, A4, prec){A1*x-A2*exp(x+A3)-0.5*prec*(x-A4)^2}
fprima<-function(x,A1,A2,A3,A4,prec){A1-A2*exp(x+A3)-prec*(x-A4)}

#Number of data
N=length(Y)

#Extracting variables from set list
ALPHA=SET$ALPHA
SIG2=SET$SIG2
TAU2=SET$TAU2
U=SET$U
V=SET$V
Dbar=SET$Dbar
thetabar=SET$thetabar

#starting values
n=length(ALPHA)
u=U[,n]
v=V[,n]
sig2=SIG2[n]
tau2=TAU2[n]
alpha=ALPHA[n]
eta=u+v

for (iit in 1:Nsimu){
  print(iit)
  
  #Paramètres de variance
  
  #tau2m dans une inverse gamma(am +(M-1)/2,bm+0.5u'Mau)
  tau2<-rgamma(1,au+0.5*(N-1),rate=bu+0.5*t(u)%*%M%*%u)
  tau2<-1/tau2
  print(tau2)
  
  #sig2v dans une inverse gamma(av+N/2,bv+0.5v'v)
  sig2<-rgamma(1,av+0.5*N,rate=bv+0.5*t(v)%*%v)
  sig2<-1/sig2
  
  # u dans une normale(m,Omega^-1)
  ratio<-sig2/tau2
  L<-chol(diag.spam(1,nrow=N)+ratio*M)
  X<-backsolve(L,eta,upper.tri=TRUE,transpose=TRUE)
  m<-backsolve(L,X,upper.tri=TRUE,transpose=FALSE)
  z<-rnorm(N,0,1)
  eps<-backsolve(L,z,upper.tri=TRUE,transpose=FALSE)
  u<-m+eps*sqrt(sig2)
  u<-u-mean(u)
    
  #Adaptative rejection sampling for alpha and eta
  alpha<-ars(1,f,fprima,x=c(-50,1,50),A1=sum(Y),A2=sum(E*exp(eta)), A3=0, A4=0, prec=0)
  for ( i in 1:N){
    eta[i]<-ars(1,f,fprima,x=c(-50,1,50),A1=sum(Y[i]), A2=exp(alpha)*E[i], A3=0, A4=u[i], prec=1/sig2)
  }
  eta<-eta-mean(eta)
  
  v=eta-u
  
  #Calcul du DIC à partir de nchauff
  labmba=E*exp(alpha+eta)
  if (DIC==TRUE){
    Dbar<-(co*Dbar-2*sum(dpois(Y,lambda,log=TRUE)))/(co+1)
    thetabar<-(co*thetabar+theta)/(co+1)
    co<-co+1
  }
  
  
  #Enregistrement
  if (iit/thin==floor(iit/thin)){
    U<-cbind(U,u)
    V<-cbind(V,v)
    SIG2<-c(SIG2,sig2)
    TAU2<-c(TAU2,tau2)
    ALPHA<-c(ALPHA,alpha)
  }
}      #Sur les itérations

return(list(ALPHA=ALPHA,SIG2=SIG2,TAU2=TAU2,U=U,V=V,Dbar=Dbar,thetabar=thetabar))

} #end function