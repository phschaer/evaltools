def startTime = System.currentTimeMillis()

// Put path to GIRT qrels files here
def qrels = "C:\\evaldata\\isearch-v1.0\\assessments"
def fileList = new File(qrels).list().toList()
def output = new File("C:\\evaldata\\results\\isearch-relevance.txt")

int totalRelevanceCounter = 0
def relevanceMap = [:]
def nonRelevanceMap = [:]

fileList.each {file ->
    def qrel = new File(qrels, file)
    qrel.splitEachLine(" ") {topic, version, docid, relevance ->

        totalRelevanceCounter++

        if (!relevanceMap[(topic)]) {relevanceMap[(topic)] = 0}
        if (!nonRelevanceMap[(topic)]) {nonRelevanceMap[(topic)] = 0}

        if (relevance != "0") {
            relevanceMap[(topic)] = relevanceMap[(topic)] + 1
        }
        if (relevance == "0") {
            nonRelevanceMap[(topic)] = nonRelevanceMap[(topic)] + 1
        }

    }
}

println "Extraction duration in total: ${(System.currentTimeMillis() - startTime) / 1000} sec"

println "Total relevance assessments: $totalRelevanceCounter"
println "Relevance count per topic: $relevanceMap"
println "Non-Relevance count per topic: $nonRelevanceMap"

output << "topic;rel;nrel\n"

relevanceMap.each {topic, count ->
    output << "$topic;${relevanceMap[(topic)]};${nonRelevanceMap[(topic)]}\n"
}