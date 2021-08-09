package etlMigracao

import java.io.File

class DicionarioLogradouro {
  private val TAMANHO_MINIMO = 3
  private val palavras: List<String> = load()

  private val abreviacoesTitulo = mapOf("ALF" to "ALFERES",                                        "ALM" to "ALMIRANTE",
                                        "ARQ" to "ARQUITETO",
                                        "BRIG" to "BRIGADEIRO",
                                        "CAP" to "CAPITÃO",
                                        "CEL" to "CORONEL",
                                        "CMDT" to "COMANDANTE",
                                        "COMEND" to "COMENDADOR",
                                        "CONS" to "CONSELHEIRO",
                                        "CEL" to "CORONEL",
                                        "D" to "DOM",
                                        "DES" to "DESEMBARGADOR",
                                        "DQ" to "DUQUE",
                                        "DR" to "DOUTOR",
                                        "DRA" to "DOUTORA",
                                        "EMB" to "EMBAIXADOR",
                                        "ENG" to "ENGENHEIRO",
                                        "FCO" to "FRANCISCO",
                                        "FR" to "FREI",
                                        "GEN" to "GENERAL",
                                        "GOV" to "GOVERNADOR",
                                        "INF" to "INFANTE",
                                        "IND" to "INDUSTRIAL",
                                        "NSA" to "NOSSA",
                                        "MQ" to "MARQUÊS",
                                        "PRES" to "PRESIDENTE",
                                        "PROF" to "PROFESSOR",
                                        "SARG" to "SARGENTO",
                                        "STA" to "SANTA",
                                        "STO" to "SANTO", 
                                        "TEN" to "TENENTE",
                                        "VISC" to "VISCONDE")

  private val arquivoAprendizado = ArquivoTexto("aprendizadoLogradouro.txt")
  private val aprendizadoPalavra = mutableMapOf<String, String>().loadAprendizado(arquivoAprendizado)

  private fun load(): List<String> {
    println("carregando logradouro")
    val logradouros = File("/tmp/logradouros.txt").bufferedReader().readLines()
    val palavras = logradouros.flatMap {
      it.split(" +".toRegex())
    }.filter { it.contains("[^0-9]".toRegex()) && it.length > TAMANHO_MINIMO }.distinct().sorted()
    println("pronto")
    return palavras
  }

  fun localiza(logradouro: String): String {
    val retSplit = logradouro.split(" +".toRegex()).mapIndexed() { index, palavraLogradouro ->
      val palavra = localizaPalavra(palavraLogradouro,
                                    index,
                                    logradouro) //if (palavra == null) println("$palavraLogradouro --> $logradouro")
      palavra
    }
    return when {
      retSplit.any { it == null } -> ""
      else -> retSplit.joinToString(separator = " ")
    }
  }

  private fun localizaPalavra(palavra: String, index: Int, logradouro: String): String? {
    return when {
      palavra.matches("[0-9O]".toRegex()) -> palavra.replace("O", "0")
      abreviacoesTitulo.containsKey(palavra) && index <= 1 -> abreviacoesTitulo[palavra] ?: palavra
      containsPalavra(palavra) -> palavra
      aprendizadoPalavra.containsKey(palavra) -> aprendizadoPalavra[palavra] ?: palavra
      palavra.length <= TAMANHO_MINIMO -> palavra
      else -> {
        val candidatos = findPalavrasCandidatas(palavra).sortedWith(compareBy(DifText::matchInicial,
                                                                              DifText::dif,
                                                                              DifText::candidato)).map {
          it.candidato
        }
        val palavraEscolhida = candidatos.firstOrNull()
        arquivoAprendizado.escrever(palavra, logradouro, candidatos.take(5).joinToString(","))
        if (palavraEscolhida != null) {
          aprendizadoPalavra[palavra] = palavraEscolhida
        }
        palavraEscolhida
      }
    }
  }

  private fun findPalavrasCandidatas(palavra: String): List<DifText> {
    val numErro = ((palavra.length - 1) * 20.0 / 100.0).toInt() + 1
    val len = palavra.length
    val prefixo = palavra.substring(0, 1)
    val candidatos = palavras.asSequence().filter { log ->
      val lenI = log.length - numErro
      val lenF = log.length + numErro
      len in lenI..lenF && prefixo == log.substring(0, 1)
    }.mapNotNull {
      val dif = levenshtein(palavra, it)
      if (dif <= numErro) DifText(dif, palavra, it) else null
    }
    return candidatos.toList()
  }

  data class DifText(val dif: Int, val texto: String, val candidato: String) {
    val matchInicial: Int
      get() {
        val lenMin = minOf(texto.length, candidato.length) - 1
        for (i in 0..lenMin) {
          if (texto[i] != candidato[i]) return i
        }
        return lenMin
      }
  }

  private fun containsPalavra(palavra: String): Boolean = palavras.contains(palavra)

  private fun MutableMap<String, String>.loadAprendizado(arquivoAprendizado: ArquivoTexto): MutableMap<String, String> {
    arquivoAprendizado.ler().forEach { linha ->
      this[linha.getOrNull(0) ?: ""] = linha.getOrNull(2) ?: ""
    }
    return this
  }
}

