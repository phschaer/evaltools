/**
 * @author <a href="mailto:philipp.schaer@gesis.org">Philipp Schaer</a>
 * @version 2012-08-13
 */

/**
 * To get this doggy rollin' you have to run this script with
 * a 32-bit Java (run Java with -d32 parameter) and you have
 * to put the libtje.jnilib into /usr/lib/java or any other
 * suitable path for java.library.path.
 *
 * Can be undone with a new compiled 64bit libjte.jnilib
 * See the new Makefile for building jte
 *
 * TODO: How to include the java.library.path in the class
 * call to put the libjte.jnilib into a system path like
 * /usr/lib/java ???*
 */

import it.unipd.dei.ims.jte.*
import java.text.NumberFormat
import rcaller.*
import groovy.util.logging.*
import org.apache.log4j.Level

@Log4j //http://marxsoftware.blogspot.de/2011/05/easy-groovy-logger-injection-and-log.html
class JTER {

    static String RScriptLocation = "C:/Program Files/R/R-2.15.0/bin/Rscript.exe"
    static String girtFolder = "D:/evaldata/girt"
    static String isearchFolder = "D:/evaldata/isearch-v1.0"
    static String outputRoot = "D:/evaldata/results"
    static String date = new Date().format('yyyy-MM-dd_HHmmss').toString()

    static main(args) {

		// read in the JTER.properties file and set config values
		try{
			def config = new ConfigSlurper().parse(new File('./JTER.properties').toURL())
			RScriptLocation = config.RScriptLocation			
			girtFolder = config.girtFolder
			isearchFolder = config.isearchFolder
			outputRoot = config.outputRoot
		}
		catch(e){
			println "Did not found a JTER.properties file - Using default values"	
			println e
		}
	
		def cli = new CliBuilder(usage: 'JTER.groovy -[htkpdcigd] folder')
		cli.with {
			h longOpt: 'help', 'Show usage information'
			t longOpt: 'treceval', 'Run typical trec_eval analysis'
			k longOpt: 'kendall', 'Run Kendalls tau analysis'
			p longOpt: 'powerlaw', 'Run PowerLaw analysis'
            c longOpt: 'calcpval', 'Calculate the pval for the PowerLaws'
			i longOpt: 'isearch', 'Use the iSearch corpus'
			g longOpt: 'girt', 'Use the GIRT corpus'
            d longOpt: 'debug', 'Print debug messages'
		}
		
		// init the command line options
        def options = cli.parse(args)
		if (!options.arguments() || options.h) {
			cli.usage()
			return
		}

        // setup the main logger
        options.d ? log.setLevel(Level.DEBUG) : log.setLevel(Level.INFO)

        // init some stuff
        log.debug "java.library.path: ${System.properties['java.library.path']}"
        log.debug("RScriptLocation: $RScriptLocation")
        log.debug("Arguments: $args")
        def jter = new JTER()
        File outputDir = new File(".")

        def extraArguments = options.arguments()
        if (extraArguments) {
            if (extraArguments.size() == 1) {
                outputDir = new File(extraArguments[0])
            }
            else {
                cli.usage()
                return
            }
        }
    
        def qrelsYears = jter.getQrelsYears(outputDir)
        def runList = jter.getRunList(qrelsYears, outputDir)
        
        // Start the main program
        if (options.t) {
            if (options.g){
                log.info "Start writing the TrecEval CSV file to ${outputDir}/results-${date}.csv"
                jter.runJavaTrecEvalGirt(qrelsYears, runList, outputDir)
                log.info "done"
            }
            else if (options.i){
                log.info "Start writing the TrecEval CSV file to ${outputDir}/results-${date}.csv"
                jter.runJavaTrecEvaliSearch(runList, outputDir)
                log.info "done"
            }
        }
        if (options.k) {
            log.info "Start writing the R output to ${outputDir}/kendall-${date}.csv"
            jter.runRKendall(runList, outputDir)
            log.info "done"
        }
        if (options.p) {
            log.info "Start writing the PowerLaw output to ${outputDir}/powerlaw-${date}.csv"
            if(options.c){
                jter.runRPowerLaw(runList, outputDir, true)                                
            }
            else{
                jter.runRPowerLaw(runList, outputDir)
            }
            log.info "done"
        }

    }

    def runRPowerLaw(List runs, File outputDir, boolean calcPval = false) {
        // init stuff
        def csv = new File(outputDir, "powerlaw-${date}.csv")
        csv.append "topic;run;n;alpha;D;xmin;pval;gof\n"

        //Iterate over the facet files and fill the facetMap
        def facetMap = [:]
        try {
            runs.each {run ->
                def facetFile = new File(outputDir, "facets-${run}.txt")
                // there are two types of facet files, so we have to make a difference here
				/*if(facetFile.readLines().getAt(0).count(";") == 2){
					facetFile.splitEachLine(";") {topic, code, count ->
						List<Integer> tempList = facetMap[("${topic}_${run}")] ?: []
						tempList.add(count.toInteger())
						facetMap[("${topic}_${run}")] = tempList // List in Map
					}
				}
				// old cvs line format
				else if ((facetFile.readLines().getAt(0).count(";") == 3)){
					facetFile.splitEachLine(";") {topic, name, code, count ->
					    List<Integer> tempList = facetMap[("${topic}_${run}")] ?: []
						tempList.add(count.toInteger())
						facetMap[("${topic}_${run}")] = tempList // List in Map
					}
                }
                // and maybe the lines are corrupted and we have to improvise
                else {*/
                    facetFile.eachLine {line ->
                        // first col contains the topic
                        def tokens = line.tokenize(";")
                        String topic = tokens.getAt(0)      
                        // last col contains the count
                        int count = tokens.getAt(tokens.size()-1).toInteger() 
					    List<Integer> tempList = facetMap[("${topic}_${run}")] ?: []
						tempList.add(count)
						facetMap[("${topic}_${run}")] = tempList // List in Map
					}
                //}
            }
        }
        catch (FileNotFoundException e) {
            log.error "No corresponging facet file found: ${e}"
        }

        log.debug facetMap

        facetMap.eachWithIndex {key, val, index ->
            List xvalues = val
            int[] x = xvalues
            def plResult = getPowerLawFit(x,calcPval)

            if (plResult) {
                def alpha = plResult.alpha
                def D = plResult.D
                def xmin = plResult.xmin
                def pval = plResult.pval
                def gof = plResult.gof

                csv.append "${key.split("_").getAt(0)};${key.split("_").getAt(1)};"
                csv.append "${NumberFormat.getInstance().format(x.size())};"
                csv.append "${NumberFormat.getInstance().format(alpha)};"
                csv.append "${NumberFormat.getInstance().format(D)};"
                csv.append "${NumberFormat.getInstance().format(xmin)};"
                csv.append "${NumberFormat.getInstance().format(pval)};"
                csv.append "${NumberFormat.getInstance().format(gof)};\n"
            }
        }

    }

    def runRKendall(List runs, File outputDir) {
        // init stuff
        def csv = new File(outputDir, "kendall-${date}.csv")
        csv.append "topic;run1;run2;size1;size2;overlapAbs;overlapPerc;tau;pvalue\n"

        // Fill up the kendalMap and the topics list
        def kendallMap = [:]
        def topics = [] as Set // unique topic numbers
        
        runs.each {run ->
            // extract all documents and their ranking per run and year
            // Watch out: we have to lowercase all run names because of
            // a possible mismatch between the filenames and the naming
            // of the runs in the top_files.
            def trecTopFile = new File(outputDir, "trec_top_file-${run}.txt")
            trecTopFile.splitEachLine(" ") {topic, runNum, docid, ranking, score, runType ->
                def tempMap = kendallMap[("${topic}_${runType}")] ?: [:]
                topics.add(topic)
                tempMap[(docid)] = ranking.toInteger() + 1    // Rank 0 is Rank 1... R wants it this way
                kendallMap[("${topic}_${runType}")] = tempMap // Map in Map
            }
        }
        log.debug "topics: $topics"
        log.debug "kendallMap: $kendallMap"

        def computedKendallRuns = []
        runs.eachWithIndex {String runx, int i ->
            runs.eachWithIndex {String runy, int j ->
                // since we want to iterate over all runs and compare each with each other, we have to check this here
                if (!(computedKendallRuns.contains("${runx}${runy}") || computedKendallRuns.contains("${runy}${runx}")) && runx != runy) {
                    topics.each {String topic ->
                        def mapX = kendallMap[("${topic}_${runs.getAt(i)}")] ?: [:]
                        def mapY = kendallMap[("${topic}_${runs.getAt(j)}")] ?: [:]
                        int sizeX = mapX.size() ?: 0
                        int sizeY = mapY.size() ?: 0
                        def listX = []
                        def listY = []
                        // It's getting tricky: mapX is the gold standard to which we have to correlate mapY
                        // in case one of the resulting Maps is bigger than the other: swap the both maps
                        if (sizeY > sizeX) {
                            def tempMap = mapX; mapX = mapY; mapY = tempMap
                            def tempSize = sizeX; sizeX = sizeY; sizeY = tempSize

                        }
                        // then we have to fill up all not corresponding rankings (due to different result set sizes or
                        // to missing documents) with -1 to make R compute the tau value
                        mapX.eachWithIndex {String docid, int ranking, int index ->
                            int alternativeRank = -1            // -1 will be interpreted as NA in RCaller (I hacked RCaller to
                            // to do so... :)
                            listX.add(ranking)                  // x-Ranking
                            listY.add(mapY.get(docid, alternativeRank))    // y-Ranking with alterantiveRank if doc is not in mapY

                            if (index < 10) {
                                log.debug "ranking for $docid: $ranking and ${mapY[docid]}"
                            }
                        }

                        int[] x = listX // cast to int array
                        int[] y = listY
                        int overlapAbs = listX.intersect(listY).size()
                        float overlapPer = 0.0
                        // failed run? beware of div by zero
                        if (listX.size() > 0 && listX.size()) {
                            overlapPer = listX.intersect(listY).size().div(listX.size())
                        }

                        // print the results and write the csv (casting lists x and y to arrays)
                        // make sure that Kendall can't be computed for very small lists (<3)
                        if (listX.size() < 3 || listY.size() < 3) {
                            log.error "Topic $topic [${runs.getAt(i)}|${runs.getAt(j)}] has less than 3 entries - Can't compute Kendall's Tau."
                            //Format: "topic;run1;run2;size1;size2;overlapAbs;overlapPerc;tau;pvalue\n"
                            csv.append "$topic;${runs.getAt(i)};${runs.getAt(j)};${sizeX};${sizeY};"
                            csv.append "${overlapAbs};"
                            csv.append "${NumberFormat.getInstance().format(overlapPer)};;" // no kendall tau and pval
                            csv.append "\n"

                        }
                        else {
                            def kendall = getKendallsTau(x, y)
                            float tau = kendall.tau
                            float pval = kendall.pvalue
                            log.debug "Topic $topic [${runs.getAt(i)}|${runs.getAt(j)}] got a Kendall's Tau of ${tau} with a p-value of ${pval}"
                            log.debug "Topic $topic [${runs.getAt(i)}|${runs.getAt(j)}] got an overlap count of ${overlapAbs}/${listX.size()} which equals $overlapPer"

                            //Format: "topic;run1;run2;size1;size2;overlapAbs;overlapPerc;tau;pvalue\n"
                            csv.append "$topic;${runs.getAt(i)};${runs.getAt(j)};${sizeX};${sizeY};"
                            csv.append "${overlapAbs};"
                            csv.append "${NumberFormat.getInstance().format(overlapPer)};"
                            csv.append "${NumberFormat.getInstance().format(tau)};"
                            csv.append "${NumberFormat.getInstance().format(pval)}"
                            csv.append "\n"

                        }
                        log.trace "listX (size of ${listX.size()}): $listX"
                        log.trace "ListY (size of ${listY.size()}): $listY"

                    }
                    computedKendallRuns.add("${runx}${runy}")    // this combination is computed and doen's have to be computed again
                    computedKendallRuns.add("${runy}${runx}")
                }

            }

        }

    }

    def getPowerLawFit(int[] xValues, boolean calcPval) {

        // Get the location to the RScript file
        String rScript = this.RScriptLocation

        // check if there are at least two unique values - otherwise plfit will panic
        if (xValues.toList().unique().size() <= 2) {
            return [alpha: -1, D: -1, xmin: -1, pval:-1, gof:-1]
        }

        try {
            //Creating an instance of class RCaller
            RCaller caller = new RCaller()
            caller.setRscriptExecutable(rScript)

            //Create a new RCode container
            RCode code = new RCode()

            //Include plfit.r from the resources folder
            def plfitFile = new File("lib/plfit.r")
            String plfitScript = plfitFile.getAbsolutePath().replace("${File.separator}", "/").toString()
            log.debug "plfitScript Location: $plfitScript"
            
            // Read in the PowerLawFit R-Scripts
            code.R_source(plfitScript)            

            // Read in the xValues and calculate the Power Law exponent
            code.addIntArray("xValues", xValues)

            // When the input sample size is small (e.g., < 50), the estimator is
            // known to be slightly biased (toward larger values of alpha). To
            // explicitly use an experimental finite-size correction, call PLFIT with finit=TRUE
            if (xValues.size() <= 50) {
                code.addRCode("temp <- plfit(xValues,finite=TRUE)")
            }
            else {
                code.addRCode("temp <- plfit(xValues)")
            }

            code.addRCode("output <- list(alpha=c(temp\$alpha), D=c(temp\$D), xmin=c(temp\$xmin))")

            caller.setRCode(code)
            caller.runAndReturnResult("output")

            // We are printing the content of our RCode and generated XML
            log.debug "getRCode():"
            log.debug "****************************"
            log.debug caller.getRCode()
            log.debug "****************************"
            log.debug "getXMLFileAsString():"
            log.debug caller.getParser().getXMLFileAsString()
            log.debug "****************************"
            log.debug "getNames(): ${caller.getParser().getNames()}"

            // Get the alpha value out of the generated XML
            double alpha = caller.getParser().getAsDoubleArray("alpha").toList().get(0)
            double D = caller.getParser().getAsDoubleArray("D").toList().get(0)
            int xmin = caller.getParser().getAsIntArray("xmin").toList().get(0)
            
            // init some dummy values for pval and gof, just in case we don't want to calc them
            double pval = -1.0
            double gof = -1.0
            
            if(calcPval == true){
                // And do it all again, if calcPval is true
                //Include plpva.r from the resources folder
                RCode code_plpva = new RCode()
                RCaller caller_plpva = new RCaller()
                caller_plpva.setRscriptExecutable(rScript)
                def plpvaFile = new File("lib/plpva.r")
                String plpvaScript = plpvaFile.getAbsolutePath().replace("${File.separator}", "/").toString()
                log.debug "plpvaScript Location: $plpvaScript"
                code_plpva.R_source(plpvaScript)            

                // Check is we really observed a PowerLaw
                // See Clauset et al (2009) - section 4.2
                // Setting the Bt to 100 (no. of iterations for the PL-check)
                // 1000 is more accurate, but takes ages
                int bt = 1000
                code_plpva.addIntArray("xValues", xValues)
                code_plpva.addRCode("library(VGAM)")
                code_plpva.addRCode("temp <- plpva(xValues,${xmin},Bt=${bt},quiet=TRUE)")
                code_plpva.addRCode("output2 <- list(pval=c(temp\$p), gof=c(temp\$gof))")
                caller_plpva.setRCode(code_plpva)
                caller_plpva.runAndReturnResult("output2")

                // We are printing the content of our RCode and generated XML
                log.debug "getRCode():"
                log.debug "****************************"
                log.debug caller_plpva.getRCode()	            
                log.debug "****************************"
                log.debug "getXMLFileAsString():"
                log.debug caller_plpva.getParser().getXMLFileAsString()
                log.debug "****************************"
                log.debug "getNames(): ${caller_plpva.getParser().getNames()}"

                // Get the pval from the generated XML
                pval = caller_plpva.getParser().getAsDoubleArray("pval").toList().get(0)
                gof = caller_plpva.getParser().getAsDoubleArray("gof").toList().get(0)
            }

            log.debug "xValues: ${xValues}"
            log.debug "alpha: ${alpha}, D: ${D}, xmin: ${xmin}, pval: ${pval}, gof: ${gof}"

            return [alpha: alpha, D: D, xmin: xmin, pval: pval, gof: gof]

        }

        catch (RCallerParseException) {
            log.error RCallerParseException
        }
        catch (Exception) {
            log.error Exception
        }
        finally {
            log.debug "Finished the plfit and plpva methods"
        }
    }

    /**
     * Compute the Kendall's tau and corresponding pValues for two given arrays of Integers
     * which represent two different rankings from two systems.
     *
     * @return tau , pvalue
     * @param x , y - two arrays of Integers with ranking positions of two different systems
     */
    def getKendallsTau(int[] x, int[] y) {

        // Get the location to the RScript file
        String rScript = this.RScriptLocation

        try {
            //Creating an instance of class RCaller
            RCaller caller = new RCaller()
            caller.setRscriptExecutable(rScript);

            //Create a new RCode container
            RCode code = new RCode()

            //Include dependency information
            code.R_require("Kendall")

            //Generating x and y vectors from
            code.addIntArray("x", x)
            code.addIntArray("y", y)

            // awkward way to bring the Kendall output into a parseable form
            // we have to extract each single value and put it into a new list
            code.addRCode("temp <- Kendall(x,y)")
            code.addRCode("output <- list(tau=c(temp\$tau),pvalue=c(temp\$sl))")

            //We are running the R code but we want code to send some result to us (java)
            //We want to handle the ols object generated in R side
            caller.setRCode(code);
            caller.runAndReturnResult("output")

            //We are printing the content of ols
            log.trace "****************************"
            log.trace caller.getRCode()
            log.trace caller.getParser().getXMLFileAsString()
            log.trace "****************************"

            caller.getParser().getNames().each {name ->
                log.trace "${name}: ${caller.getParser().getAsDoubleArray(name).toList().get(0)}"
            }

            double tau = caller.getParser().getAsDoubleArray("tau").toList().get(0)
            double pvalue = caller.getParser().getAsDoubleArray("pvalue").toList().get(0)

            return [tau: tau, pvalue: pvalue]

        }
        catch (RCallerParseException) {
            log.error RCallerParseException
        }
        catch (Exception) {
            log.error Exception
        }
    }

    def runJavaTrecEvaliSearch(ArrayList runList, File outputDir) {
        // Find out which paths are suitable for libjte.jnilib
        log.trace("System.getProperty: ${System.getProperty('java.library.path')}")

        int topicCounter = 66

        File csv = new File(outputDir, "results-${date}.csv")
        // define the CSV schema
        def headingList = ["topic",                // topic code
                "run",
                "relevant",             // absolute number of relevant docs
                "relevantRetrieved",    // how many correct docs did we find?
                "retrieved",            // how many docs did we find at all?
                "recall",
                "avgPrecision",         // MAP
                "rPrecision",           // r-Precision
				"bpref",				// binary preference
                "p@5",                  // P@n
                "p@10",
                "p@15",
                "p@20",
                "p@30",
                "p@100",
                "p@200",
                "p@500",
                "p@1000"]
        // build CSV heading from headingList
        headingList.eachWithIndex { heading, index ->
            (index < headingList.size() - 1) ? csv.append("${heading};") : csv.append("${heading}\n")
        }
        // build the NULL-line
        String nullLine = ""
        headingList.eachWithIndex { heading, index ->
            if (index == 0) {nullLine += "failedTopic;"}
            else if (index == 1) {nullLine += "failedRun;"}
            else {(index < headingList.size() - 1) ? (nullLine += "0;") : (nullLine += "0\n")}
        }

        runList.each {runName ->
            runName = runName.replace(".txt","") // to surpass bug in getRunList
            String qrels = "${isearchFolder}/assessments/graded-qrels.all-types.txt"
            String run = "${outputDir}/trec_top_file-${runName}.txt"
            int nextQuery = -1;

            try {
                List<Metric> metrics = JTEFactory.createTrecEval(qrels, run).compute();

                metrics.eachWithIndex {Metric m, int i ->
                    // JTE doesn't correctly compute OVERALL - therefore we have to skip it
                    if (!m.query.equals("OVERALL")) {
                        // Init nextQuery (only during the first iteration)
                        // Remember to get rid of the DOI prefix for the year 2007 and 2008 (10.2452/)
                        int tempQueryNum = m.query.toInteger()
                        if (i == 0) {
                            nextQuery = tempQueryNum - tempQueryNum  + 1
                        }
                        // Fill up empty results with the precomputed nullLine.
                        // In the case that there is more then one empty topic following, we
                        // iterate until the next valid topic is reached.
                        while (tempQueryNum != nextQuery) {
                            csv.append(nullLine)
                            nextQuery++;
                        }
                        // Print all the TrevEval standard measures
                        // Using NumberFormat instead of it.toString() because of locale sentivity of NumberFormat,
                        // so it is correctly converted to 1,0 instead of 1.0!
                        StandardMetric sm = JTEFactory.createStandardMetric(m)
                        csv.append "${m.query};${runName};"
                        csv.append "${sm.getRelevant()};"
                        csv.append "${sm.getRelevantRetrieved()};"
                        csv.append "${sm.getRetrieved()};"
                        csv.append "${NumberFormat.getInstance().format(sm.getRelevantRetrieved() / sm.getRelevant())};"
                        csv.append "${NumberFormat.getInstance().format(sm.getAvgPrec())};"
                        csv.append "${NumberFormat.getInstance().format(sm.getRPrec())};"
                        csv.append "${NumberFormat.getInstance().format(sm.getBpref())};"
                        sm.cutOffPrecisions.each {csv.append "${NumberFormat.getInstance().format(it);};"}
                        csv.append "\n"
                        nextQuery++;
                        // Print a human readable output (comparable to the original TrecEval)
                        log.trace(sm.toString(Locale.GERMANY))
                    }
                }
            }
            catch (Exception e) {
                log.error "Error in $run - $nextQuery"
                log.error e
            }

            // Fill up empty results with the precomputed nullLine.
            //while (nextQuery % topicsPerYear != 1) {
            //    csv.append(nullLine)
            //    nextQuery++;
            //}
        }

        // Add stats
        def stats = ["run", "recall", "avgPrecision", "rPrecison", "bpref", "p@5", "p@10", "p@15", "p@20", "p@30", "p@100", "p@200"]
        // build CSV heading from headingList
        csv.append "\n\n"
        stats.eachWithIndex { stat, index ->
            (index < stats.size() - 1) ? csv.append("${stat};") : csv.append("${stat}\n")
        }
        int startRow = 2
        int endRow = startRow + topicCounter - 1
        def rows = ["F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"]
        for (int x = 0; x < runList.size(); x++) {
            startRow = 2 + x * topicCounter
            endRow = startRow + topicCounter - 1
            csv.append "=B${startRow};"
            rows.each { row ->
                csv.append "=MITTELWERT(${row}${startRow}:${row}${endRow});"
            }
            csv.append "\n"
        }

    }

    def runJavaTrecEvalGirt(ArrayList qrelsList, ArrayList runList, File outputDir, int topicsPerYear = 25) {
        // Find out which paths are suitable for libjte.jnilib
        log.trace("System.getProperty: ${System.getProperty('java.library.path')}")
        int topicCounter = qrelsList.size() * topicsPerYear

        File csv = new File(outputDir, "results-${date}.csv")
        // define the CSV schema
        def headingList = ["topic",                // topic code
                "run",
                "relevant",             // absolute number of relevant docs
                "relevantRetrieved",    // how many correct docs did we find?
                "retrieved",            // how many docs did we find at all?
                "recall",
                "avgPrecision",         // MAP
                "rPrecision",           // r-Precision
				"bpref",           		// binary preference
                "p@5",                  // P@n
                "p@10",
                "p@15",
                "p@20",
                "p@30",
                "p@100",
                "p@200",
                "p@500",
                "p@1000"]
        // build CSV heading from headingList
        headingList.eachWithIndex { heading, index ->
            (index < headingList.size() - 1) ? csv.append("${heading};") : csv.append("${heading}\n")
        }
        // build the NULL-line
        String nullLine = ""
        headingList.eachWithIndex { heading, index ->
            if (index == 0) {nullLine += "failedTopic;"}
            else if (index == 1) {nullLine += "failedRun;"}
            else {(index < headingList.size() - 1) ? (nullLine += "0;") : (nullLine += "0\n")}
        }

        runList.each {runName ->
            qrelsList.each {year ->
                String qrels = "${girtFolder}/qrels/qrels_ds_DE_${year}.txt"
                String run = "${outputDir}/trec_top_file-${runName}-${year}.txt"
                int nextQuery = -1;

                try {
                    List<Metric> metrics = JTEFactory.createTrecEval(qrels, run).compute();

                    metrics.eachWithIndex {Metric m, int i ->
                        // JTE doesn't correctly compute OVERALL - therefore we have to skip it
                        if (!m.query.equals("OVERALL")) {
                            // Init nextQuery (only during the first iteration)
                            // Remember to get rid of the DOI prefix for the year 2007 and 2008 (10.2452/)
                            int tempQueryNum = m.query.replace("10.2452/", "").replace("-DS", "").toInteger()
                            if (i == 0) {
                                nextQuery = tempQueryNum - tempQueryNum % topicsPerYear + 1
                            }
                            // Fill up empty results with the precomputed nullLine.
                            // In the case that there is more then one empty topic following, we
                            // iterate until the next valid topic is reached.
                            while (tempQueryNum != nextQuery) {
                                csv.append(nullLine)
                                nextQuery++;
                            }
                            // Print all the TrevEval standard measures
                            // Using NumberFormat instead of it.toString() because of locale sentivity of NumberFormat,
                            // so it is correctly converted to 1,0 instead of 1.0!
                            StandardMetric sm = JTEFactory.createStandardMetric(m)
                            csv.append "${m.query};${runName};"
                            csv.append "${sm.getRelevant()};"
                            csv.append "${sm.getRelevantRetrieved()};"
                            csv.append "${sm.getRetrieved()};"
                            csv.append "${NumberFormat.getInstance().format(sm.getRelevantRetrieved() / sm.getRelevant())};"
                            csv.append "${NumberFormat.getInstance().format(sm.getAvgPrec())};"
                            csv.append "${NumberFormat.getInstance().format(sm.getRPrec())};"
							csv.append "${NumberFormat.getInstance().format(sm.getBpref())};"
                            sm.cutOffPrecisions.each {csv.append "${NumberFormat.getInstance().format(it);};"}
                            csv.append "\n"
                            nextQuery++;
                            // Print a human readable output (comparable to the original TrecEval)
                            log.trace(sm.toString(Locale.GERMANY))
                        }
                    }
                }
                catch (Exception e) {
                    log.error "Error in $run - $nextQuery"
                    log.error e
                }

                // Fill up empty results with the precomputed nullLine.
                while (nextQuery % topicsPerYear != 1) {
                    csv.append(nullLine)
                    nextQuery++;
                }
            }
        }
        // Add stats
        def stats = ["run", "recall", "avgPrecision", "rPrecison", "bpref", "p@5", "p@10", "p@15", "p@20", "p@30", "p@100", "p@200"]
        // build CSV heading from headingList
        csv.append "\n\n"
        stats.eachWithIndex { stat, index ->
            (index < stats.size() - 1) ? csv.append("${stat};") : csv.append("${stat}\n")
        }
        int startRow = 2
        int endRow = startRow + topicCounter - 1
        def rows = ["F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"]
        for (int x = 0; x < runList.size(); x++) {
            startRow = 2 + x * topicCounter
            endRow = startRow + topicCounter - 1
            csv.append "=B${startRow};"
            rows.each { row ->
                csv.append "=MITTELWERT(${row}${startRow}:${row}${endRow});"
            }
            csv.append "\n"
        }

    }

    def getQrelsYears(File outputDir) {
        // extract the years from the queryLog files
        def queryFiles = outputDir.list([accept: {d, f -> f ==~ /.*?queryLog.*/ }] as FilenameFilter).toList()
        def qrelsYears = []
        queryFiles.each {queryFile ->
            qrelsYears.add(queryFile.toString().replaceAll(/[\w]*-/, "").replace(".txt", ""))
        }
        qrelsYears.unique()
        log.info "qrelsYears: ${qrelsYears}"
        return qrelsYears
    }

    def getRunList(List qrelsYears, File outputDir) {
        // extract the tasks from the trec_top files
        def topFiles = outputDir.list([accept: {d, f -> f ==~ /.*?trec_top.*/ }] as FilenameFilter).toList()
        def runList = []
        topFiles.each {topFile ->
            def temp = topFile.toString().replace("trec_top_file-", "")
            temp = temp.replace(".txt", "")
//            qrelsYears.each {year ->
//                temp = temp.replace("-${year}", "")
//            }
            runList.add(temp)
        }
        runList = runList.unique()
        log.info "runList: $runList"
        return runList
    }


}
