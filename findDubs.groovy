def file = new File(args.getAt(0))
def idMap = [:]
file.splitEachLine(" "){topic, runnum, id, rank, points, runname ->
	
	def key = "$topic-$id"
	if (idMap[(key)] == 1){println "found dublicate: $key"}
	else (idMap[(key)] = 1) 
	
}