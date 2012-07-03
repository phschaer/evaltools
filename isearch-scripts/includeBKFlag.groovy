/**
* Dieses Skript sollte analog zu copyJudgedDocuments.groovy verwendet werden um in
* den Metadaten von PN und BK die Datensätze, die einmal bewertet wurden zu markieren.
*/
def startTime = System.currentTimeMillis()

// Put path to isearch files here
def base = "../../isearch-v1.0"
def assessments = new File(base, "assessments/graded-qrels.all-types.txt")
def inputPath = new File(base, "PN-solr")
def outputPath = new File(base, "PN-solr-judged")
//def inputPath = new File(base, "BK-solr")
//def outputPath = new File(base, "BK-solr-judged")
outputPath.mkdir()

// Generate id:topics map
def idTopicMap = [:]

assessments.splitEachLine(" "){topic, num, iSearchID, relevance ->

    List<String> tempList = idTopicMap[(iSearchID)] ?: []
    tempList.add(topic)
    idTopicMap[(iSearchID)] = tempList // List in Map
}

println idTopicMap
println "Found ${idTopicMap.size()} iSearch IDs Topic Mappings"

/*
idTopicMap.each {filename, topicList ->
    topicList.each {String topic ->
        def file = new File(inputPath,"${filename}.pdf")
        def output = new File(outputPath,topic)
        output.mkdirs()
        def tofile = new File(output,"${filename}.pdf")
        (new AntBuilder()).copy(file : file, tofile : tofile )
    }
}
*/

def xmlFiles = inputPath.list().toList()

// Die se Funktion implementiert den girt:true Eintrag nur, wenn die id auch irgendwann einmal bewertet wurde.
// mit der Änderung in der inneren Schleife werden alle iSearchIDs verwendet!

xmlFiles.each {xmlFile ->
    println "Including topicid flag for ${xmlFile}"

    try{
        def xml = new XmlParser().parse(new File(inputPath, xmlFile))
        xml.doc.each {doc ->
            String id = doc.field.find {it.@name == 'id'}.text().toString().replace("isearch-","")

            if (idTopicMap.containsKey(id)) {
                idTopicMap[(id)].each {topic ->
                    doc.appendNode('field', [name: 'topicid'], topic)
                }
            }

        }
        // write the updated XML file
        def writer = new PrintWriter(new File(outputPath, xmlFile))
        new XmlNodePrinter(writer).print(xml)
    }
    catch (Exception e){
        println e
    }

}

println "Converting duration in total: ${(System.currentTimeMillis() - startTime) / 1000} secs"
println "REMEMBER to rerun \"tidy \" on these files"