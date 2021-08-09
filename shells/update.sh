#!/bin/bash

cat logradouros.sql | mysql -h172.16.52.5 -uroot -h localhost sqldados > /tmp/logradouros.txt
cat bairros.sql | mysql -h172.16.52.5  -uroot -h localhost   migracao > /tmp/bairros.txt
#cat ../arquivo/bairros.txt > /tmp/bairros.txt
