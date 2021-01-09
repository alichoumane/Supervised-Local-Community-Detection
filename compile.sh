#!/bin/sh

if [ -d "bin" ]
then
	rm -R bin
fi

mkdir bin

echo "Compiling to bin ..."

javaFiles=""
for file in $(find src -name "*.java" -type f)
do
	echo $file
	javaFiles=$javaFiles" "$file
done

javac -cp "./lib/javaml-0.1.7/*" -d bin $javaFiles

echo "Creating jar ..."

cd bin
jar cfm ../LocalCommunity.jar ../Manifest programs/*.class learning/*.class helpers/*.class similarity/*.class utils/*.class
