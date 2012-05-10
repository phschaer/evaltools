#!/bin/bash
START=$(date +%s)
# -s: is the path to your original XML files
java -jar saxon9he.jar -s:../documents/BK-clean -xsl:./BK2Solr.xsl -o:../BK-solr
# your logic ends here
END=$(date +%s)
DIFF=$(( $END - $START ))
echo "It took $DIFF seconds to convert"