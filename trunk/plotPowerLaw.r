library(VGAM)
#source("http://tuvalu.santafe.edu/~aaronc/powerlaws/plpva.r")

drawPlot <- function(currentDir,year,type){    
  
  #init the PDF export
  folder=paste(currentDir,'/',year,sep='')
  pdf(paste(currentDir,'/',type,year,'.pdf',sep=''), 
      pointsize=8,
  )
  
  # arrange the layout
  par(mfrow=c(5,5),mar=c(1.5,2,1.5,1.5),lwd=0.5,pty="s")
  
  for(file in dir(folder,pattern='*Boost1.csv$')){
    # extract the topic number from the filename
    topic <- sub(paste('-',type,'rerankTopicQueryFiltersBoost1.csv',sep=''),'',file)
    
    #print status line
    print(paste("plotting",topic,"for type",type))
    
    # read in the freq from the single csv files
    temptab <- t(read.csv(paste(folder,'/',file,sep=''), 
                          sep=";",
                          header=FALSE, 
                          blank.lines.skip=TRUE))
    
    freqs <- temptab[1,] # data conversion, just the first col
    ranks <- 1:length(freqs)   
    
    # plot with on a log-log scale
    plot(ranks,freqs,xlab='rank',ylab='frequency',log="xy")        
    
    # extract the power law exponent
    if(length(freqs < 50)){
      plvals <- plfit(freqs,finite=TRUE)      
    }
    else{
      plvals <- plfit(freqs)  
    }    
    alpha <- plvals$alpha
    D <- plvals$D
    xmin <- plvals$xmin
    
    # TODO: check is we really observed a PL 
    # See Clauset et al (2009) - section 4.2        
    # pval <- plpva(freqs,xmin,Bt=1000,quiet=TRUE)
    # print(pval)
    
    # draw a dotted line to mark xmin
    abline(v=xmin,lty=3)      
    
#     if(pval$p >= 0.1){      
#             
#       # calulate the intersection with y-axis (y_0) and set this
#       # as the (a) intersect=y_0 and (b) slope=-alpha      
#       y_xmin <- freqs[xmin] 
#       y_0 <- y_xmin + (xmin * alpha)
#       # print(paste('Koordinaten: ', xmin, y_xmin, -alpha, y_0))
#       abline(a=log10(y_0), b=-log10(alpha),lty=3)            
#       
#       # very skewed plot... but seems right
#       # WARNING: NOT WORKING WITH LOG-LOG-PLOT
#       # x1 <- log10(xmin)
#       # y1 <- log10(freqs[xmin])
#       # x2 <- log10(max(ranks))
#       # y2 <- x2*(-log10(alpha))            
#       # segments(c(x1), c(y1), c(x2), c(y2), col= 'red')
#       # print(paste('segments: ',x1,y1,x2,y2))
#       
#       # draw an approximated (wrong!) logistic regression model
#       # abline(lm(log10(freqs)~log10(ranks)),lty=2)      
#       # we only use the data in respect to xmin
#       # abline(lm(log10(freqs[xmin:length(freqs)])~log10(ranks[xmin:length(ranks)])),lty=3)      
#             
#     }
    
    # add some decorating text
    alpha <- format(alpha,digits=3) # only 3 digits
    text(max(ranks), max(freqs), 
         labels=(paste('top:',topic,'a: -',alpha,'xmin:',xmin)),
         adj=1)    
    
  }
  dev.off() #close file  
}

#rootDir <- "C:/Users/sc/Dropbox/Dissertation/results/girt/Facetten-Analyse"
rootDir <- "/Users/schaer/Dropbox/Dissertation/results/girt/Facetten-Analyse"
#entities <- c("test")
entities <- c("author","classification","issn","location","method",
              "publisher","pubyear","subject")

for(entity in entities){
  currentDir = paste(rootDir,'/',entity,sep='')
  for(year in dir(currentDir)){
    drawPlot(currentDir,year,entity)
  }    
}
