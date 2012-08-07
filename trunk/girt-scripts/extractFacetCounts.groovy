def startTime = System.currentTimeMillis()

// Put path to GIRT Files here
def folder = "C:\\Users\\sc\\Dropbox\\Dissertation\\results\\girt\\Facetten-Analyse"
def fileList = new File(folder).list()

def map = [:]

fileList.each{file ->
    if (file.startsWith("facets-")){
        new File(folder,file).splitEachLine(";"){topic,empty,name,count ->
            String key = "${topic}-${file.split("-").getAt(1)}"
            List tempList = map[(key)] ? map[(key)] : []
            tempList.add(count)
            map[(key)] = tempList
        }
    }
}

map.each {key, value ->
    def output = new File(folder,"${key.replace("10.2452/","")}.csv")
    String outputString = ""
    value.each {val ->
        outputString += "${val}\n"
    }
    output.append(outputString)
}

println "Extraction duration in total: ${(System.currentTimeMillis() - startTime)/1000} sec"