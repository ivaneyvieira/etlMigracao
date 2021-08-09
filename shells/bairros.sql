select TRIM(REGEXP_REPLACE(BairroSaci, '[^A-Z0-9 ]+', ' '))  as bairro
from migracao.T_RESUMO
GROUP BY bairro
HAVING  COUNT(*) >= 30 AND length(bairro) > 3
