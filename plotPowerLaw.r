library(matlab)
library(VGAM)
# source("http://tuvalu.santafe.edu/~aaronc/powerlaws/plpva.r")

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
    if(length(freqs < 100)){
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
    pval <- plpva(freqs,xmin,Bt=10,quiet=TRUE)
    # print(pval)
    
    # draw an approximated (wrong!) logistic model 
    abline(lm(log10(freqs)~log10(ranks)),lty=2)
    
    if(pval$p >= 0.1){
      # TODO: This is not correctly plotting!!!
      # not quite sure why I have to add 10 to xmin 
      # (maybe log10(10)=1, so we just add 1 actually...            
      # abline(a=log10(xmin+10),b=(-(log10(alpha))))              
      
      # x1 <- xmin
      # y1 <- freqs[xmin]
      # x2 <- xmin+10
      # y2 <- xmin+10^(-alpha)
      # lines(c(x1,y1),c(x2,y2))
      # print(paste(x1,y1,x2,y2))

      # draw a dotted line to mark xmin
      abline(v=xmin,lty=3)
      
      # draw an approximated (wrong!) logistic regression model
      # we only use the data in respect to xmin
      # abline(lm(log10(freqs[xmin:length(freqs)])~log10(ranks[xmin:length(ranks)])),lty=2)    
    }
    
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
entities <- c("test")
#entities <- c("author","classification","issn","location","method",
#              "publisher","pubyear","subject")

for(entity in entities){
  currentDir = paste(rootDir,'/',entity,sep='')
  for(year in dir(currentDir)){
    drawPlot(currentDir,year,entity)
  }    
}
