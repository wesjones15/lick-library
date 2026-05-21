#!/bin/bash
set -e
cd /Users/wesleyjones/Documents/lick_library
lsof -ti:8080 | xargs kill -9 2>/dev/null; true
mvn package -DskipTests
mvn spring-boot:run
