import org.apache.pdfbox.util.*
import org.apache.pdfbox.pdmodel.PDDocument
import groovy.xml.MarkupBuilder

/**
 * Init some things first ...
 */

String basedir = "D:/evaldata/isearch-v1.0"
File pdfdir = new File(basedir,"documents/PF")
File PFids = new File(basedir,"iSearchIDs/iSearchIDs-PF.txt")
File arXivChunks = new File(basedir,"PF-metadata/arXiv-chunks")
File assessments = new File(basedir, "assessments/graded-qrels.PF.txt")
File output = new File(basedir,"PF-solr-judged");output.mkdir()

/**
 * Build the arxivID - iSearchID map
 */

def idMap = [:]
PFids.splitEachLine("\t"){String iSearchID, String filename, String arxivID, String url ->
    //String arxivID = filename.replace(".xml","").trim()
    idMap[(arxivID)] = iSearchID
}
//println idMap
println "Found ${idMap.size()} arxivID - iSearch Mappings"

/**
 * Build the iSearchID - topic map
 */

def idTopicMap = [:]
assessments.splitEachLine(" "){topic, num, iSearchID, relevance ->
    List<String> tempList = idTopicMap[(iSearchID)] ?: []
    tempList.add(topic)
    idTopicMap[(iSearchID)] = tempList // List in Map
}
//println idTopicMap
println "Found ${idTopicMap.size()} iSearch IDs Topic Mappings"

/**
 * Run over all files and do the job!
 */
arXivChunks.listFiles().each {file ->
    println "Processing $file"
    def root = new XmlSlurper().parse(file)

    root.record.each{rec ->
        //String arxivID = rec.metadata.arXiv.id.text()
        String arxivID = rec.header.identifier
        String iSearchID = idMap[(arxivID)]

        if (iSearchID){

            // EXTRACT THE FULLTEXT FROM THE PDF
            def fulltext
            PDDocument pdfDoc = new PDDocument()
            String pdf = new File(pdfdir,"${iSearchID}.pdf").absoluteFile.toString()
            try{
                def stripper = new PDFTextStripper()
                fulltext = stripper.getText(pdfDoc.load(pdf))
            }
            catch(Exception e){
                println "There was a problem reading the PDF file ${pdf}"
            }
            finally {
                pdfDoc.close() // clean up!
            }

            // EXTRACT THE METADATA FROM THE ARXIV DUMP
            File outputFile = new File(output,"${iSearchID}.xml")
            def xmlWriter = new FileWriter(outputFile,false)  // no append
            def xml = new MarkupBuilder(xmlWriter)
            xml.mkp.xmlDeclaration version: '1.0', encoding: 'UTF-8'
            xml.add(){
                doc(){
                    field(name:'id',"isearch-${iSearchID}")
                    field(name:'collection',"isearch-PF")
                    field(name:'url', "http://arxiv.org/abs/${arxivID}")
                    field(name:'doi', rec.metadata.arXiv.doi.text())
                    field(name:'classification', rec.metadata.arXiv.categories.text())
                    field(name:'classification', rec.metadata.arXiv.'msc-class'.text())
                    field(name:'title',rec.metadata.arXiv.title.text())
                    field(name:'abstract',rec.metadata.arXiv.'abstract'.text().trim())
                    rec.metadata.arXiv.authors.author.each{author ->
                        field(name:'author',"${author.keyname}, ${author.forenames}")
                    }
                    field(name:'rawsource',rec.metadata.arXiv.'journal-ref'.text())
                    field(name:'rawsource',rec.metadata.arXiv.'comments'.text())

                    // SOURCE = journal nonly with alpha chars

                    def possibleSources = rec.metadata.arXiv.'journal-ref'.toString().split(',')
                    def possibleSource = possibleSources.first().replaceAll(/[^A-Za-z^&]/,' ')
                    possibleSource = possibleSource.replaceAll('vol','')
                    possibleSource = possibleSource.replaceAll('pp','')
                    field(name:'source',possibleSource.trim())

                    // PUBYEAR in parantheses
                    def maxYear = 2013
                    def possibleYears = rec.metadata.arXiv.'journal-ref'.toString().tokenize('(').toList()
                    if(possibleYears.size()>1) { // minimum of one split!
                        possibleYears.each{String possibleYear ->
                            def yearCands = possibleYear.tokenize(')')
                            yearCands.each{String possibleYear2 ->
                                if(possibleYear2.size()>=4){
                                    def yearCand = possibleYear2?.toString()?.replaceAll(/\W/,'')?.trim()
                                    if(yearCand.isInteger() & yearCand.length() == 4 && yearCand?.toInteger() <= maxYear){
                                        field(name:'pubyear',yearCand)
                                    }
                                }
                            }
                        }
                    }

                    // PUBYEAR at the end of journal field
                    def possibleYears2 = rec.metadata.arXiv.'journal-ref'.toString().split(",")
                    // get the last one and remove all non-word chars and test if it is 4 chars long
                    if(possibleYears2.size()>1) { // minimum of one split!
                        def yearCand2 = possibleYears2.last().toString().replaceAll(/\W/,'').trim()
                        if(yearCand2.isInteger() & yearCand2.size()==4 && yearCand2.toInteger() <= maxYear){
                            field(name:'pubyear',yearCand2)
                        }
                    }

                    idTopicMap[(iSearchID)].each {id ->
                        field(name:'topicid',id)
                    }
                    // very aggressive filtering: only include fulltext chars that are minimal ASCII
                    if(fulltext) field(name:'fulltext',fulltext.replaceAll(/[^A-Za-z0-9,.()@\\/\\=_:\- ]/,' '))
                }
            }
        }
    }
}

