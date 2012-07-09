
//http://export.arxiv.org/oai2?verb=GetRecord&identifier=oai:arXiv.org:0706.3300&metadataPrefix=arXiv

String basedir = "D:/evaldata/isearch-v1.0"
File outputDir = new File(basedir,"PF-metadata");outputDir.mkdir();
File PF = new File(basedir,"/iSearchIDs/iSearchIDs-PF.txt")
int lines = PF.length()
int counter = 1

PF.splitEachLine("\t"){iSearchID, filename, oaiID, url ->
    def OAIurl = "http://export.arxiv.org/oai2?verb=GetRecord&identifier=${oaiID}&metadataPrefix=arXiv"
    def data = new URL(OAIurl).getText()
    new File(outputDir,"${iSearchID}.xml").append(data)
    if((counter++).mod(10)==0){println "$counter docs fetched"}
}

