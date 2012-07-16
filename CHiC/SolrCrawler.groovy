/**
 * Simple Solr Crawler for the CHiC topics.
 *
 * @author <a href="mailto:philipp.schaer@gesis.org">Philipp Schaer</a>
 * @version 2012-06-27
 */

import groovy.util.logging.*
import org.apache.log4j.Level
import java.math.MathContext

@Log4j //http://marxsoftware.blogspot.de/2011/05/easy-groovy-logger-injection-and-log.html

class SolrCrawler {

    static main(args) {

        def cli = new CliBuilder(usage: 'SolrCrawler.groovy -[hdorn] -t [xml|csv] solrURL topicFile')
        cli.with {
            h longOpt: 'help', required: false, 'Show usage information'
            d longOpt: 'debug', required: false, 'Print debug messages'
            o args: 1, longOpt: 'out', required: false, 'output folder (will be created during runtime)'
            r args: 1, longOpt: 'run', required: false, 'custom runName scheme (like "gesis-adhoc")'
            n args: 1, longOpt: 'num', required: false, 'number of documents returned and included in Trec-File (default=10)'
            t args: 1, longOpt: 'toptype', required: true, '[xml|csv] original XML (default) file or CSV with extra data'
            el args: 1, longOpt: 'lang', required: false, '[en|de|fr] which start lang (default=de)'
            gl args: 1, longOpt: 'lang', required: false, '[en|de|fr] which goal lang (default=de)'
            e args: 1, longOpt: 'exp', required: false, '[combo|str|wiki_entity|wiki_sim|wiki_back] expansion method (default = wiki_entity)'
        }

        // init the command line options
        def options = cli.parse(args)
        if (!options || options.h) {
            cli.usage()
            return
        }

        // setup the main logger
        options.d ? log.setLevel(Level.DEBUG) : log.setLevel(Level.INFO)

        // init some stuff
        def crawler = new SolrCrawler()

        // read in the command line options
        String runName = options.r ? options.r : "run" // is there a custom runName?
        String entryLang = options.el ? options.el : "de" // which entry language for the runs?
        String goalLang = options.gl ? options.gl : "de" // which goal language for runs?
        int numResults = options.n ? options.n.toInteger() : 10 // return a different number of documents?
        String expMethod = options.e ? options.e : "wiki_entity"
        String printType = options.o ? "file" : "stdout" // show we print the results into files?

        String folderName = "TASK1-${expMethod}_${entryLang}_${goalLang}"
        File outputFolder = options.o ? new File(new File(options.o.toString()),folderName) : new File("./results")
        options.o ? outputFolder.mkdir() : null

        // read in the command line arguments
        String solrURL
        File topicFile
        def extraArguments = options.arguments()
        if (extraArguments && extraArguments.size() == 2) {
            solrURL = extraArguments.get(0)
            topicFile = new File(extraArguments.get(1))
        }
        else {
            log.error "Not the correct number of arguments: ${extraArguments.size()}"
            log.error "$extraArguments"
            cli.usage()
            return
        }

        def topicIDMap = [:]
        if (options.t.toString().toLowerCase() == "csv") {
            // parse the CSV topic file with extra data
            topicIDMap = crawler.parseChicCSV(topicFile)
        }
        else {
            // parse the XML topic file
            def topicsXML = new XmlSlurper().parse(topicFile)
            topicsXML.topic.each { topic ->
                // double map in map because of the more complex csv file
                topicIDMap[(topic.identifier)] = ["title_de": topic.title]
            }
        }

        // start the crawler
        crawler.crawlSolr(solrURL, topicIDMap, runName, numResults, printType, outputFolder, entryLang, expMethod)

    }

    def parseChicCSV(File topicFile) {
        def topicMap = [:]
        topicFile.eachLine {line ->
            def map = [:]
            try {
                // tokenize() und splitEachLine(liefern keine leeren Elemente zurück, daher split())
                String id = line?.split(";")?.collect()?.getAt(0)
                if (id.startsWith("CHIC")) {
                    map["title_de"] = line?.split(";")?.collect()?.getAt(2)
                    map["title_en"] = line?.split(";")?.collect()?.getAt(3)
                    map["title_fr"] = line?.split(";")?.collect()?.getAt(4)
                    map["str_de"] = line?.split(";")?.collect()?.getAt(8)?.toString()?.tokenize(",")
                    map["str_en"] = line?.split(";")?.collect()?.getAt(9)?.toString()?.tokenize(",")
                    map["wiki"] = line?.split(";")?.collect()?.getAt(10)?.toString()?.tokenize(",")
                    map["wiki_entity_en"] = line?.split(";")?.collect()?.getAt(11)?.toString()?.tokenize(",")
                    map["wiki_entity_de"] = line?.split(";")?.collect()?.getAt(12)?.toString()?.tokenize(",")
                    map["wiki_sim_en"] = line?.split(";")?.collect()?.getAt(13)?.toString()?.tokenize(",")
                    map["wiki_sim_de"] = line?.split(";")?.collect()?.getAt(14)?.toString()?.tokenize(",")
                    map["wiki_back_en"] = line?.split(";")?.collect()?.getAt(15)?.toString()?.tokenize(",")
                    topicMap[(id)] = map // put it all together
                }
            }
            catch (Exception e) {
                log.error e
            }
        return // ?!?!? Warum hier return?
        }
        log.info topicMap
        return topicMap
    }

    def constructQuery(Map topicIDMap, String lang, String expMethod){
        def queries = [:]
        topicIDMap.each {topicid,Map map ->
            String language = "dc_language_s:${lang} AND "
            String title = ""
            String expansion = ""

            // build the title terms
            //TODO: remove stopwords
            def titleWords = map[("title_${lang}")]?.split()
            String titleString = ""
            titleWords.eachWithIndex {titleWord, index ->
                titleString += titleWord
                if (index+1 < titleWords.size()){titleString += " OR "}
            }
            title += "chic_all-${lang}:(${titleString})^2"

            // build the expansion terms
            String expString = ""
            if(expMethod.equals("combo")){
                def strWords = map[("str_${lang}")]
                def wikiEntityWords = map[("wiki_entity_${lang}")]
                def expList = []
                strWords.eachWithIndex {String expWord, index ->
                    expList += expWord.trim().startsWith('\"') ? expWord : "\"${expWord}\""
                }
                wikiEntityWords.eachWithIndex {String expWord, index ->
                    expList += expWord.trim().startsWith('\"') ? expWord : "\"${expWord}\""
                }
                expList.eachWithIndex {expWord, index ->
                    expString += expWord
                    if (index+1 < expList.size()){expString += " OR "}
                }

            }
            else{
                def expWords = map[("${expMethod}_${lang}")]
                expWords.eachWithIndex {String expWord, index ->
                    expString += expWord.startsWith('\"') ? expWord : "\"${expWord}\""
                    if (index+1 < expWords.size()){expString += " OR "}
                }
            }

            expansion += "chic_all-${lang}:(${expString})"

            // sum it all up
            if(expString.size()>1){
                queries[(topicid)] = "${title} OR ${expansion}"
            }
            else{
                queries[(topicid)] = "${title}"
            }
        }

        return queries
    }

    def crawlSolr(String solrURL, Map topicIDMap, String runName, int numResults, String printType, File outputFolder, String lang, String expMethod) {
        def queries = constructQuery(topicIDMap,lang,expMethod)

        File logFile = new File(outputFolder,"queryLog.txt")

        queries.each {topicid, query ->
            log.debug "Get query for topicID ${topicid}: ${query}"

            String solr = "${solrURL}/select?q=${query}&fl=score,id&rows=${numResults}"
            logFile << "$topicid;$query;$solr\n"
            log.debug solr

            def solrResponse = new XmlSlurper().parse(solr)
            log.info "Query on \"${query}\" returned ${solrResponse.result.@numFound} documents."

            def results = []
            solrResponse.result.doc.eachWithIndex {doc, int i ->
                if (i < 1000) {
                    String id = doc.str.findAll {it.@name == "id"}
                    double score = doc.float.findAll {it.@name == "score"}.toString().toFloat()
                    String resultString = "${topicid} Q0 ${id} ${i} ${score.trunc(Double.SIZE)} ${runName}"
                    results.add(resultString)
                }
            }

            if (printType == "file") {
                //logFile << "$topicid $query\n" // log the query
                File resultFile = new File(outputFolder, "${runName}-${topicid}.txt")
                results.each {resultFile << it << "\n"}

                log.debug "Wrote the TrecFile for Topic ${topicid} to ${resultFile}"
            }
            else { // print on stdout
                results.each {
                    log.info it
                }
            }


        }

    }

}
