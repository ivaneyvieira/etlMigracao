package etlMigracao

import java.io.File
import java.io.FileNotFoundException

class ArquivoTexto(val nome : String) {
  private val SEPARADOR = ","
  private val diretorio = "/root/etl/etlMigracao/arquivo"
  private val arquivo = File("$diretorio/$nome")

  fun ler(): List<List<String>> {
    return when {
      arquivo.exists() -> arquivo.readLines().map {
        it.split(SEPARADOR)
      }
      else -> emptyList()
    }
  }

  fun escrever(vararg linha : String){
    arquivo.appendText(linha.joinToString(SEPARADOR))
    arquivo.appendText("\n")
  }
}
