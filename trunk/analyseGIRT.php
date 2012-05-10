<?php
/*
* Manual is at http://pear.php.net/manual/en/package.xml.xml-statistics.intro.php
*/

require_once "XML/Statistics.php";
$stat = new XML_Statistics(array("ignoreWhitespace" => true));

$dir = '/Users/schaer/Desktop/girt_test/solis_xhive';

//if ($handle = opendir('/Users/schaer/Dropbox/Dissertation/diss/data/GIRT4-XML')) {
if ($handle = opendir($dir)) {
    // echo "Directory handle: $handle\n";

	echo "Filename;"."Total tags;"."doc"."\n";

	while (false !== ($file = readdir($handle))) {
		
		 if ($file != "." && $file != "..") {
			
			echo "$file".";";
		
			$result = $stat->analyzeFile("$dir/"."$file");

			if ($stat->isError($result)) {
		    	die("Error: " . $result->getMessage());
			}

			// total amount of tags:
			echo $stat->countTag() . ";";
			// total amount of tags "doc"
//			echo $stat->countTag("add") . ";";
			echo $stat->countAttribute("field") . ";";


			// count total number of tags in depth 4
			echo "Amount of Tags in depth 4: " . $stat->countTagsInDepth(4) . "\n";

			//		echo "Occurences of PHP Blocks: " . $stat->countPI("PHP") . "\n";

			echo "Occurences of external entity 'bar': " . $stat->countExternalEntity("bar") . "\n";

			echo "Data chunks: " . $stat->countDataChunks() . "\n";

			echo "Length of all data chunks: " . $stat->getCDataLength() . "\n";	       	

			echo "\n";

		}
			
	   	}
	    
	closedir($handle);

}

?>