drawPlot <- function(currentDir,year,type){    
    
  #init the PDF export
  folder=paste(currentDir,'/',year,sep='')
  pdf(paste(currentDir,'/',type,year,'.pdf',sep=''), 
      pointsize=8,
      )
  
  # arrange the layout
  par(mfrow=c(5,5),mar=c(1.5,2,1.5,1.5),lwd=0.5,pty="s")
  for(file in dir(folder,pattern='*Boost1.csv$')){
    topic <- sub('-authorrerankTopicQueryFiltersBoost1.csv','',file)
    
    # read in the freq from the single csv files
    freqs <- t(read.csv(paste(folder,'/',file,sep=''), 
                        sep=";",
                        header=FALSE, 
                        blank.lines.skip=TRUE))
    ranks <- 1:length(freqs)   
    
    # plot with on a log-log scale
    plot(ranks,freqs,xlab='rank',ylab='frequency',log="xy")        
    
    # extract the power law exponent
    plvals <- plfit(freqs[1,])
    alpha <- plvals$alpha
    D <- plvals$D
    xmin <- plvals$xmin
    
    # check is we really observed a PL
    if(D>=0.05){
      # not quite sure why I have to add 10 to xmin 
      # (maybe log10(10)=1, so we just add 1 actually...)
      abline(a=log10(xmin+10),b=-(log10(alpha)))  
    }    
    
    # add some decorating text
    text(max(ranks), max(freqs), 
         labels=(paste('top: ',topic,' a: -',alpha,', xmin: ',xmin)),
         adj=1)
    
  }
  dev.off() #close file  
}

rootDir <- "C:/Users/sc/Dropbox/Dissertation/results/girt/Facetten-Analyse"
entities <- c("author",
              "classification",
              "issn",
              "location",
              "method",
              "publisher",
              "pubyear",
              "subject")

for(entity in entities){
  currentDir = paste(rootDir,'/',entity,sep='')
  for(year in dir(currentDir)){
    drawPlot(currentDir,year,entity)
    }    
}
