folder <- "C:/Users/sc/Dropbox/Dissertation/results/girt/Facetten-Analyse"

for(file in dir(folder,pattern='*Boost1.csv$')){
	# read in the data from the single csv files
  d <- t(read.csv(paste(folder,'/',file,sep=''), sep=";", header=FALSE, blank.lines.skip=TRUE))

  # make a new pdf file (filename.csv -> filename.pdf)  
  pdf(paste(folder,'/pdf/',sub('.csv$','.pdf',file),sep=''))
  plot(log10(1:length(d)),log10(d),xlab='rank',ylab='frequency')
  dev.off() #close file
}