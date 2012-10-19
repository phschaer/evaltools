# draw the power law plots for a given directory, a year and a data type
drawPlot <- function(currentDir,year,type){      
  
  #init the PDF export
  folder=paste(currentDir,'/',year,sep='')
  filename <- paste(currentDir,'/../pdf.merged/',type,year,'.pdf',sep='')
  pdf(filename, pointsize=8)
  
  # arrange the layout
  par(mfrow=c(5,5),mar=c(1.5,2,1.5,1.5),lwd=0.5,pty='s')
  
  for(file in dir(folder,pattern='*Boost1.csv$')){
    # extract the topic number from the filename
    topic <- sub(paste('-',type,'rerankTopicQueryFiltersBoost1.csv',sep=''),'',file)
    
    #print status line
    print(paste('plotting',topic,'for type',type))
    
    # read in the freq from the single csv files
    temptab <- t(read.csv(paste(folder,'/',file,sep=''), 
                          sep=';',
                          header=FALSE, 
                          blank.lines.skip=TRUE))
    
    freqs <- temptab[1,] # data conversion, just the first row
    ranks <- 1:length(freqs)   
    
    # plot with on a log-log scale
    plot(ranks,freqs,xlab='rank',ylab='frequency',log='xy')        
    
    # extract the power law exponent
    tempValues <- powerLawExponents[grep(topic,powerLawExponents$topic),]
    resultValues <- tempValues[grep(type,tempValues$run),]
    
    # when more than one result, take the first one
    resultValue <- resultValues[1,]
    
    alpha <- resultValue$alpha
    D <- resultValue$D
    xmin <- resultValue$xmin
    pval <- resultValue$pval      
    
    # draw a dotted line to mark xmin
    abline(v=xmin,lty=3)
    
    # check is we really observed a PL 
    # See Clauset et al (2009) - section 4.2        
    # print(pval)
    if(pval >= 0.1){      
                   
      # calulate the intersection with y-axis (y_0) and set this
      # as the (a) intersect=y_0 and (b) slope=-alpha      
      # y_xmin <- freqs[xmin] 
      # y_0 <- y_xmin + (xmin * alpha)      
      # abline(a=log10(y_0), b=-log10(alpha),lty=2, col='yellow', lwd=2)            
      # print(paste('intersection method: ', xmin, y_xmin, -alpha, y_0))
      # --> this just does not work out!      
      
      # very skewed plot... but seems right
      # WARNING: NOT WORKING WITH LOG-LOG-PLOT      
      # x0 <- xmin
      # y0 <- freqs[xmin]      
      # x1 <- max(ranks)
      # y1 <- x1^(-log10(alpha))
      # segments(c(x0), c(y0), c(x1), c(y1), lty=2, col='red', lwd=2)
      # print(paste('segment method: ',x0,y0,x1,y1))      
      
      # draw an approximated (wrong!) linear regression model
      # abline(lm(log10(freqs)~log10(ranks)),lty=2)      
      # we only use the data in respect to xmin
      filteredRanks <- log10(ranks[xmin:length(ranks)])
      filteredFreqs <- log10(freqs[xmin:length(freqs)])
      logmodel <- lm(filteredFreqs~filteredRanks)            
      # print(summary(logmodel))
      # print(logmodel$df.residual)
      if(logmodel$df.residual > 0){
        abline(logmodel, lty=1, col='gray', lwd=1.5)  
      }                        
    }
    
    # add some decorating text
    alpha <- format(alpha,digits=3) # only 3 digits
    text(max(ranks), max(freqs), 
         labels=(paste('top:',topic,'a: -',alpha,'xmin:',xmin)),
         adj=1)    
    
  }
  dev.off() #close file    
  #compress the pdf file with pdftk  
  commandLine <- paste('C:/cygwin/bin/pdftk.exe',
                       filename,
                       'output',
                       sub('merged','compressed',filename),                       
                       'compress')  
  system(commandLine)  
}