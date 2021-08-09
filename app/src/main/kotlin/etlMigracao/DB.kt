package etlMigracao

import org.sql2o.Sql2o

val mysqlLocal: Sql2o = Sql2o(
  "jdbc:mysql://10.1.10.242:3306/migracao?useSSL=false",
  "root", "musabela"
)


val mysqlOCI: Sql2o = Sql2o(
  "jdbc:mysql://localhost/migracao?allowPublicKeyRetrieval=true&useSSL=false",
  "root", ""
)

