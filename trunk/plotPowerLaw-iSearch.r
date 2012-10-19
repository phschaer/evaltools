# set the working directory
setwd('D:/evaldata/evaltools/')
# setwd('/Users/schaer/Desktop/evaltools/')

# Configure the root directory and the correct CSV file
rootDir <- 'C:/Users/sc/Dropbox/Dissertation/results/girt/facets_topicQuery'
#rootDir <- '/Users/schaer/Dropbox/Dissertation/results/girt/Facetten-Analyse'
csvFile <- 'powerlawForPlot.csv'

# entities <- c('subject')
entities <- c('author',
              'classification',
              'issn',
              'location',
              'method',
              'publisher',
              'pubyear',
              'subject')

# load the draw function
source('drawPowerLaw.r') 

# read in the csv file with all PL exponents
powerLawExponents <- read.csv2(paste(rootDir,csvFile,sep='/'), 
                               sep=';',
                               header=TRUE,
                               blank.lines.skip=TRUE,
)[,-8] # delete last empty column

for(entity in entities){
  currentDir = paste(rootDir,'/',entity,sep='')
  for(year in dir(currentDir)){
    drawPlot(currentDir,year,entity)
  }    
}
