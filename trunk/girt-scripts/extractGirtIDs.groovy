def startTime = System.currentTimeMillis()

// Put path to GIRT Files here
def folder = "/Users/schaer/Desktop/girt_test/GIRT4-XML"
def fileList = new File(folder).list().toList()
def idList = new File("../resources/GIRT-IDs.txt")

fileList.each{file ->
	def GIRT = new XmlSlurper().parse(new File(folder,file))
	println "Extracting IDs from ${file}"
    GIRT.DOC.each{doc ->
	  idList << "${doc.DOCID} \n"
	}
}

println "Extraction duration in total: ${(System.currentTimeMillis() - startTime)/1000} sec"