export JAVA_OPTS="$JAVA_OPTS -Xmx512M"
groovy -cp "lib/jte.jar:lib/log4j-1.2.16.jar:lib/RCaller-2.1.1-SNAPSHOT.jar" JTER.groovy $@