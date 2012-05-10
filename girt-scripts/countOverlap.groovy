
/**
 * @author schaer
 * @version 31.05.11 13:16
 */
def brad = new File("/Users/schaer/Desktop/girt_test/old-results/good/results-bradford.txt")
def lotka = new File("/Users/schaer/Desktop/girt_test/old-results/good/results-lotka.txt")
def solr = new File("/Users/schaer/Desktop/girt_test/old-results/good/results-solr.txt")

def bradList = []
def topicBradCount = []
brad.splitEachLine(" ") {topic, num, doc, rank, score, algo ->
    topicBradCount.add(topic)
    if (topicBradCount.count(topic)<=10){
        bradList.add("$topic-$doc")
    }
}

def topicLotkaCount = []
def lotkaList = []
lotka.splitEachLine(" ") {topic, num, doc, rank, score, algo ->
    topicLotkaCount.add(topic)
    if (topicLotkaCount.count(topic)<=10){
        lotkaList.add("$topic-$doc")
    }
}

def topicSolrCount = []
def solrList = []
lotka.splitEachLine(" ") {topic, num, doc, rank, score, algo ->
    topicSolrCount.add(topic)
    if (topicSolrCount.count(topic)<=10){
        solrList.add("$topic-$doc")
    }
}

def bradLotkaCounter = 0
bradList.each{ if (lotkaList.contains(it)){bradLotkaCounter++} }
def bradSolrCounter = 0
bradList.each{ if (solrList.contains(it)){bradSolrCounter++} }
def solrLotkaCounter = 0
solrList.each{ if (lotkaList.contains(it)){solrLotkaCounter++} }
def totalCounter = 0
solrList.each{ if (lotkaList.contains(it) && bradList.contains(it)){totalCounter++} }

println "bradLotka    : ${bradLotkaCounter}"
println "bradSorl     : ${bradSolrCounter}"
println "solrLotka    : ${solrLotkaCounter}"
println "totalCounter : ${totalCounter}"