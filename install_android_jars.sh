#!/bin/bash
# Use this script to install the official Android jars to local Maven repo.

if [ -z "$ANDROID_HOME" ]; then
    echo "$ANDROID_HOME not set"
    exit 1
fi

DEFAULT_API_LEVEL=17

echo "This script uploads the local Android jars to local Maven repo."
echo -n "Enter the API level to be uploaded [$DEFAULT_API_LEVEL]: "
read API_LEVEL
if [ -z "$API_LEVEL" ]; then
    API_LEVEL=$DEFAULT_API_LEVEL
fi

function install_android_jar() {

    # Go to Android jar directory

    echo
    cd "$ANDROID_HOME/platforms/android-$API_LEVEL"
    if [ "$?" -eq 0 ]; then
	echo `pwd`
    else
	exit 1
    fi

    # Install Android jar

    local MVN_JAR="mvn install:install-file
  -Dfile=android.jar
  -DgroupId=com.google.android
  -DartifactId=android
  -Dversion=$API_LEVEL
  -Dpackaging=jar
"
    echo
    echo "$MVN_JAR"
    local MVN_JAR_OUTPUT=`$MVN_JAR`
    local MVN_JAR_STATUS=$?
    echo "$MVN_JAR_OUTPUT"
    if [ $MVN_JAR_STATUS -ne 0 ]; then
	exit 1
    fi

    # Go to source directory

    echo
    cd "$ANDROID_HOME/sources"
    if [ "$?" -eq 0 ]; then
	echo `pwd`
    else
	exit 1
    fi

    # Create Android source jar

    local ZIP="zip -r android-$API_LEVEL-sources.jar android-$API_LEVEL"
    echo
    echo "$ZIP"
    local ZIP_OUTPUT=`$ZIP`
    local ZIP_STATUS=$?
    echo "$ZIP_OUTPUT"
    if [ $ZIP_STATUS -ne 0 ]; then
	exit 1
    fi

    # Install Android source jar

    local MVN_SRC="mvn install:install-file
  -Dfile=android-$API_LEVEL-sources.jar
  -DgroupId=com.google.android
  -DartifactId=android
  -Dversion=$API_LEVEL
  -Dpackaging=jar
  -Dclassifier=sources
"
    echo
    echo "$MVN_SRC"
    local MVN_SRC_OUTPUT=`$MVN_SRC`
    local MVN_SRC_STATUS=$?
    echo "$MVN_SRC_OUTPUT"
    if [ $MVN_SRC_STATUS -ne 0 ]; then
	exit 1
    fi
}

function install_android_support_v4_jar() {

    # Go to Android support v4 jar directory

    echo
    cd "$ANDROID_HOME/extras/android/support/v4"
    if [ "$?" -eq 0 ]; then
	echo `pwd`
    else
	exit 1
    fi

    # Install Android support v4 jar

    local MVN_SUPPORT_V4_JAR="mvn install:install-file
  -Dfile=android-support-v4.jar
  -DgroupId=com.google.android
  -DartifactId=android-support
  -Dversion=v4
  -Dpackaging=jar
"
    echo
    echo "$MVN_SUPPORT_V4_JAR"
    local MVN_SUPPORT_V4_JAR_OUTPUT=`$MVN_SUPPORT_V4_JAR`
    local MVN_SUPPORT_V4_JAR_STATUS=$?
    echo "$MVN_SUPPORT_V4_JAR_OUTPUT"
    if [ $MVN_SUPPORT_V4_JAR_STATUS -ne 0 ]; then
	exit 1
    fi

    # Create Android support v4 source jar

    local ZIP_SUPPORT_V4="zip -r android-support-v4-sources.jar src"
    echo
    echo "$ZIP_SUPPORT_V4"
    local ZIP_SUPPORT_V4_OUTPUT=`$ZIP_SUPPORT_V4`
    local ZIP_SUPPORT_V4_STATUS=$?
    echo "$ZIP_SUPPORT_V4_OUTPUT"
    if [ $ZIP_SUPPORT_V4_STATUS -ne 0 ]; then
	exit 1
    fi

    # Install Android support v4 source jar

    local MVN_SUPPORT_V4_SRC="mvn install:install-file
  -Dfile=android-support-v4-sources.jar
  -DgroupId=com.google.android
  -DartifactId=android-support
  -Dversion=v4
  -Dpackaging=jar
  -Dclassifier=sources
"
    echo
    echo "$MVN_SUPPORT_V4_SRC"
    local MVN_SUPPORT_V4_SRC_OUTPUT=`$MVN_SUPPORT_V4_SRC`
    local MVN_SUPPORT_V4_SRC_STATUS=$?
    echo "$MVN_SUPPORT_V4_SRC_OUTPUT"
    if [ $MVN_SUPPORT_V4_SRC_STATUS -ne 0 ]; then
	exit 1
    fi
}

install_android_jar
install_android_support_v4_jar
