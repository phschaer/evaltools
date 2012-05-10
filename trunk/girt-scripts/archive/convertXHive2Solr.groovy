import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

def startTime = System.currentTimeMillis()

// Put path to SOLIS XHive files here
def base = "/Users/schaer/Desktop/girt_test"
def inputPath = new File(base, "solis_xhive/")
def outputPath = new File(base, "temp/")
def xslt = new File('../resources/xHive2Solr.xsl')

def xHiveFiles = inputPath.list().toList()

xHiveFiles.each{xHiveFile ->
  println "Converting ${xHiveFile}"
  def inFile = new File(inputPath,xHiveFile)
  def solrFile = new File(outputPath, xHiveFile)

  def factory = TransformerFactory.newInstance()
  def transformer = factory.newTransformer(new StreamSource(new FileReader(xslt)))
  transformer.transform(new StreamSource(new FileReader(inFile)), new StreamResult(new FileWriter(solrFile)))

}

println "Converting duration in total: ${(System.currentTimeMillis() - startTime)/1000} secs"
