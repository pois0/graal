#!/bin/bash

export GRAAL_PRJ_HOME=~/Programs/graal
export JAVA_HOME="$GRAAL_PRJ_HOME/graal/sdk/latest_graalvm_home"

# COMMAND="/home/pois/Programs/graal/graal/compiler/mxbuild/linux-amd64/graaljdks/jdk1.8-cmp/bin/java -server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64 -XX:+UseJVMCICompiler -XX:+UseJVMCINativeLibrary -cp $GRAAL_PRJ_HOME/graal/truffle/mxbuild/src/com.oracle.truffle.sl/bin/:/home/pois/Programs/graal/graal/truffle/mxbuild/src/com.oracle.truffle.sl.launcher/bin:/home/pois/.mx/cache/ANTLR4_e27d8ab4f984f9d186f54da984a6ab1cccac755e/antlr4.jar:/home/pois/.mx/cache/GUAVA_60458f877d055d0c9114d9e1a2efb737b4bc282c/guava.jar com.oracle.truffle.sl.launcher.SLMain"

CLASS_PATH="-Dtruffle.class.path.append=$GRAAL_PRJ_HOME/graal/truffle/mxbuild/src/com.oracle.truffle.sl/bin/:/home/pois/.mx/cache/ANTLR4_e27d8ab4f984f9d186f54da984a6ab1cccac755e/antlr4.jar:/home/pois/.mx/cache/GUAVA_60458f877d055d0c9114d9e1a2efb737b4bc282c/guava.jar -cp /home/pois/Programs/graal/graal/truffle/mxbuild/src/com.oracle.truffle.sl.launcher/bin"

JAVA_COMMAND="$JAVA_HOME/bin/java"

JAVA_ARGS+=("-XX:+UnlockExperimentalVMOptions" "-XX:+EnableJVMCI" "-d64" "-XX:+UseJVMCICompiler" "-XX:+UseJVMCINativeLibrary")

if [ $# = 0 ]; then
    $JAVA_COMMAND "${JAVA_ARGS[@]}" $CLASS_PATH "com.oracle.truffle.sl.launcher.SLMain"
else
    $JAVA_COMMAND "${JAVA_ARGS[@]}" $CLASS_PATH "com.oracle.truffle.sl.launcher.SLMain" $*
fi
