#!/bin/bash

rm arquivo/*.txt

cd shells

./update.sh

cd ..

./gradlew run
