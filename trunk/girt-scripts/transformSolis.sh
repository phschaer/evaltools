#!/bin/bash
START=$(date +%s)
# -s: is the path to your original XML files
java -jar saxon9he.jar -s:../../solis/2012-3-8_iz-solis -xsl:./xHive2Solr.xsl -o:../Solis-solr-temp
# your logic ends here
END=$(date +%s)
DIFF=$(( $END - $START ))
echo "It took $DIFF seconds to convert"