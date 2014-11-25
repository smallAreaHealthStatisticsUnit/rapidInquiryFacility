##################################################################################
#GENERIC FUNCTION FOR SPATIAL POLYGONS PLOTS

RegionPlot<-function(data,sp,del='quant', bd_col='transparent',C=0,legend=TRUE){
  VarInt<-data
  #paletteBleue<-colorRampPalette(c("Gray 92","Gray 16"))
  #paletteBleue<-colorRampPalette(c("blue","red"))
  #paletteBleue<-colorRampPalette(c("white","white"))
  paletteBleue<-colorRampPalette(c("blue","cyan","yellow","orange","red"))
  #paletteBleue<-rainbow
  if (del=="quant"){
    del=quantile(VarInt,prob=seq(0,1,l=10),na.rm=TRUE)
    del[1]<-del[1]-(del[2]-del[1])/10
    del[length(del)]<-del[length(del)]+(del[length(del)]-del[length(del)-1])/10
  }
  couleur<- paletteBleue(length(del)-1)[findInterval(VarInt,del,all.inside=TRUE)]
  
  Alpes=sp
  xmin<-min(Alpes@bbox[1,1])
  xmax<-max(Alpes@bbox[1,2])
  ymin<-min(Alpes@bbox[2,1])
  ymax<-max(Alpes@bbox[2,2])
 
  
  par(mar=c(0,0,3,8))
  plot.new()
  plot.window(xlim=c(xmin,xmax),ylim=c(ymin,ymax), asp=1)
  
  if(C==0){C=1:length(Alpes)}else{
    co=1
    for ( i in 1:length(Alpes)){   
      for ( j in 1:length(Alpes@polygons[[i]]@Polygons)){
        polygon(Alpes@polygons[[i]]@Polygons[[j]]@coords,border=bd_col)
      }
      co=co+1
    }
  }
  co=1
  for ( i in C){   
    for ( j in 1:length(Alpes@polygons[[i]]@Polygons)){
      polygon(Alpes@polygons[[i]]@Polygons[[j]]@coords,col=couleur[co],border=bd_col)
    }
    co=co+1
  }
  
  if (legend==TRUE){
    leg<-rep(0,length(del)-1)
    for (i in 1:(length(del)-1)){
      leg[i]<-paste(as.character(round(del[i],d=2)),',',as.character(round(del[i+1],d=2)))
    }
    colors=color.scale(paletteBleue(length(del)-1),del,name="", unit="",labels=seq(1,length(del)))
    cs.draw(colors, horiz=FALSE, cex=1,digits=4, side=1, length=0.5, offset=0.1, pos=0.95)
    par(mar=c(5, 4, 4, 2) + 0.1)
  }
}



cs.draw <- function (color.scale, name = NULL, unit = NULL, length = 0.8,
                     width = 0.03, horiz = T, pos = 1.09, side = if (pos > 0.5) -1 else 1,
                     cex = 1, offset = 0, border = NULL, lty = NULL, lwd = par("lwd"),
                     xpd = T, digits = 4, roundfunc = zapsmall)
{
  nc <- length(color.scale$cols)
  if (is.null(name))
    name <- color.scale$name
  if (is.null(unit))
    unit <- color.scale$unit
  if (horiz) {
    xc <- (0.5 - offset) * par("usr")[1] + (0.5 + offset) *
      par("usr")[2]
    xd <- length * (par("usr")[2] - par("usr")[1])
    x1 <- xc - xd/2
    x2 <- xc + xd/2
    x <- seq(x1, x2, , nc + 1)
    ya <- par("usr")[4] - par("usr")[3]
    yd <- width * (par("usr")[4] - par("usr")[3])
    y1 <- par("usr")[3] + pos * ya
    y2 <- y1 + side * yd
    for (i in 1:nc) {
      rect(x[i], y1, x[i + 1], y2, col = color.scale$cols[i],
           border = border, lty = lty, lwd = lwd, xpd = xpd)
      if (i %in% color.scale$labels)
        text(x[i], y2, adj = c(0.5, -0.75 * side + 0.5),
             roundfunc(color.scale$breaks[i], digits = digits),
             xpd = xpd, cex = cex)
    }
    text(x[nc + 1], y2, adj = c(0.5, -0.75 * side + 0.5),
         roundfunc(color.scale$breaks[nc + 1], digits = digits),
         xpd = xpd, cex = cex)
    text(xc, y1, adj = c(0.5, 0.75 * side + 0.5), name, xpd = xpd,
         cex = cex)
    text(x[nc + 1], (y1 + y2)/2, adj = c(-0.05, 0.5), unit,
         xpd = xpd, cex = cex)
  }
  else {
    yc <- (0.5 - offset) * par("usr")[3] + (0.5 + offset) *
      par("usr")[4]
    yd <- length * (par("usr")[4] - par("usr")[3])
    y1 <- yc - yd/2
    y2 <- yc + yd/2
    y <- seq(y1, y2, , nc + 1)
    xa <- par("usr")[2] - par("usr")[1]
    xd <- width * (par("usr")[2] - par("usr")[1])
    x1 <- par("usr")[1] + pos * xa
    x2 <- x1 + side * xd
    for (i in 1:nc) {
      rect(x1, y[i], x2, y[i + 1], col = color.scale$cols[i],
           border = border, lty = lty, lwd = lwd, xpd = xpd)
      if (i %in% color.scale$labels)
        text(x2, y[i], adj = c(-0.75 * side + 0.5, 0.5),
             roundfunc(color.scale$breaks[i], digits = digits),
             xpd = xpd, cex = cex)
    }
    text(x2, y[nc + 1], adj = c(-0.75 * side + 0.5, 0.5),
         roundfunc(color.scale$breaks[nc + 1], digits = digits),
         xpd = xpd, cex = cex)
    text(x1, yc, adj = c(0.5, -0.75 * side + 0.5), name,
         xpd = xpd, srt = 90, cex = cex)
    text(x1, y[1], adj = c(-0.75 * side + 0.5, 1.5), unit,
         xpd = xpd, cex = cex)
  }
} 

#Fonction pour creer color.scale
color.scale=function(vectofcols, vectofbreaks, name="name", unit="", labels){
  res=list()
  res$cols=vectofcols
  res$breaks=vectofbreaks
  res$name=name
  res$unit=unit
  if (is.null(labels)){res$labels=seq(1,length(vectofbreaks),1)
  }else{res$labels=labels}
  return(res)
}

