import groovy.xml.StreamingMarkupBuilder

/**
* Die Idee ist (weil Solr ja keine updates für Datensätze zulässt, sondern nur 
* einen kompletten Overwrite), alle bewerteten Dokumente aus PF in ein seperates
* Verzeichnis zu kopieren und dann mit einem neuen Flag für die relevanten Topics 
* zu reindexieren
*/
def startTime = System.currentTimeMillis()

// Put path to isearch files here
def base = "../../isearch-v1.0"
def assessments = new File(base, "assessments/graded-qrels.PF.txt")
def inputPath = new File(base, "documents/PF")
def outputPath = new File(base, "PF-judged")
outputPath.mkdir()

// Generate id:topics map
def idTopicMap = [:]

assessments.splitEachLine(" "){topic, num, iSearchID, relevance ->

    List<String> tempList = idTopicMap[(iSearchID)] ?: []
    tempList.add(topic)
    idTopicMap[(iSearchID)] = tempList // List in Map

    //idTopicMap[(iSearchID)] = idTopicMap[(iSearchID)].getClass() == List ? idTopicMap[(iSearchID)].add(topic) : [topic]
}

println idTopicMap
println "Found ${idTopicMap.size()} iSearch IDs Topic Mappings"

idTopicMap.each {filename, topicList ->
    topicList.each {String topic ->
        def file = new File(inputPath,"${filename}.pdf")
        def output = new File(outputPath,topic)
        output.mkdirs()
        def tofile = new File(output,"${filename}.pdf")
        (new AntBuilder()).copy(file : file, tofile : tofile )
    }
}

/*
def xHiveFiles = inputPath.list().toList()

// Die se Funktion implementiert den girt:true Eintrag nur, wenn die id auch irgendwann einmal bewertet wurde.
// mit der Änderung in der inneren Schleife werden alle iSearchIDs verwendet!

xHiveFiles.each {xHiveFile ->
    println "Including GIRT Flag for ${xHiveFile}"

    def xml = new XmlParser().parse(new File(inputPath, xHiveFile))

    xml.doc.each {doc ->
        def id = doc.field.find {it.@name == 'acquisition_id'}.text().toString().trim()

        //TODO: Auch hier ist das Gemunkel mit der CSV totaler Quatsch... s.o.
        //if (idTopicMap.keySet().contains("GIRT-DE" + id)) {
        if (iSearchIDs.contains("GIRT-DE" + id)) {
            doc.appendNode('field', [name: 'girt'], "true")
            idTopicMap["GIRT-DE" + id]?.tokenize(";").each {topicNum ->
                doc.appendNode('field', [name: 'girt_topic'], topicNum)
            }
        }
        else {
            doc.appendNode('field', [name: 'girt'], "false")
        }

    }

    // write the updated XML file
    def writer = new PrintWriter(new File(outputPath, xHiveFile))
    new XmlNodePrinter(writer).print(xml)


}

println "Converting duration in total: ${(System.currentTimeMillis() - startTime) / 1000} secs"
  */