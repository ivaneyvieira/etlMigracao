SELECT name AS logradouro
FROM sqldados.ceplog
WHERE name REGEXP '^([A-Z ]+|[0-9]+[A-Z]*|[A-Z]+[0-9]+|[0-9]+ [A-Z ]+)$'
GROUP BY logradouro
ORDER BY logradouro
