#!/bin/bash

mvn clean package install

cd target
cp ehcache-bootstrap* ..
cd ..
mvn clean


