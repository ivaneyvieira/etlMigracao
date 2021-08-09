package etlMigracao

suspend fun main(args: Array<String>) {
  if (args.size == 1) {
    when (args[0]) {
      "ENDERECOS" -> ProcessaEndereco.execute()
    }
  }else ProcessaEndereco.execute()
}

