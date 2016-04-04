#!/usr/bin/env bash

# example of the run script for running the word count

#I'll execute my programs, with the input directory tweet_input and output the files in the directory tweet_output

javac -cp src/json-simple-1.1.1.jar src/Run.java src/ParseJSON.java
java -cp src/json-simple-1.1.1.jar:src Run tweet_input/tweets.txt tweet_output/output.txt
