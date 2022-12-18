#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/openjdk-11/
export EXTRA_JAVA_HOMES=/usr/lib/jvm/openjdk-8/
export GRAAL_PRJ_HOME=~/Programs/graal

COMMAND="/usr/lib64/openjdk-11/bin/java -Xmx36g --add-modules=org.graalvm.truffle --module-path=/home/pois/Programs/graal/graal/truffle/mxbuild/dists/jdk11/truffle-api.jar:$GRAAL_PRJ_HOME/graal/sdk/mxbuild/dists/jdk11/ -cp $GRAAL_PRJ_HOME/graal/sdk/mxbuild/dists/jdk11/graal-sdk.jar -cp $GRAAL_PRJ_HOME/graal/truffle/mxbuild/src/com.oracle.truffle.sl/bin/:/home/pois/Programs/graal/graal/truffle/mxbuild/src/com.oracle.truffle.sl.launcher/bin:/home/pois/.mx/cache/ANTLR4_e27d8ab4f984f9d186f54da984a6ab1cccac755e/antlr4.jar:/home/pois/.mx/cache/GUAVA_60458f877d055d0c9114d9e1a2efb737b4bc282c/guava.jar com.oracle.truffle.sl.launcher.SLMain"

if [ $# = 0 ]; then
    $COMMAND
else
    $COMMAND $*
fi
