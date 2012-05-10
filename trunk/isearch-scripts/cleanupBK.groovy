def basePath = "../documents/BK"
def outputPath = "../documents/BK-clean"
new File(outputPath).mkdir()
def inputFiles = new File(basePath).list()
inputFiles.each{file ->
	def inputFile = new File(basePath,file)	
	def outputFile = new File(outputPath,file)
	outputFile.write(inputFile.getText("utf-8").replace("&","&amp;"),"utf-8")	
}