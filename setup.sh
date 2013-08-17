#!/bin/sh

shopt -s nullglob
set -e

function setup() {
    local proj=$1

    mkdir -p $proj/libs
    cd $proj/libs
    for f in *
    do
        if [[ $f != *android-support* ]]
        then
            echo "Removing $f"
            rm $f
        fi
    done

    cd ..
    mvn dependency:copy-dependencies -DoutputDirectory=libs

    cd libs
    for f in *.jar
    do
        if [[ $f != "*android-support*" ]]
        then
            echo "Creating $f.properties"
            echo "src=../libsrc/${f%.*}-sources.jar" > $f.properties
        fi
    done

    cd ..
    mvn dependency:copy-dependencies -DoutputDirectory=libsrc -Dclassifier=sources

    cd ..
}

setup files-lib
setup files-test
