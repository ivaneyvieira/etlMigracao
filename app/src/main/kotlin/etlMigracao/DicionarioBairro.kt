package etlMigracao

import java.io.File

class DicionarioBairro {
  private val abreviacoes = mapOf("FCO" to "FRANCISCO",
                                  "STA" to "SANTA",
                                  "SACY" to "SACI",
                                  "STO" to "SANTO",
                                  "TEN" to "TENENTE",
                                  "VISC" to "VISCONDE",
                                  "VL" to "VILA")

  private val listaBairros: List<String> = load()

  private val arquivoAprendizado = ArquivoTexto("aprendizadoBairro.txt")
  private val aprendizadoBairro = mutableMapOf<String, String>().loadAprendizado(arquivoAprendizado)

  private fun load(): List<String> {
    println("carregando Bairros")
    val bairros = File("/tmp/bairros.txt").bufferedReader().readLines().distinct().sorted()
    println("pronto")
    return bairros
  }

  fun localiza(bairro: String): String? {
    val bairroSemAbreviacao = explodeAbreviacoes(bairro)
    return when {
      listaBairros.contains(bairroSemAbreviacao)         -> bairroSemAbreviacao
      aprendizadoBairro.containsKey(bairroSemAbreviacao) -> aprendizadoBairro[bairroSemAbreviacao]
                                                            ?: bairroSemAbreviacao
      else                                               -> {
        val candidatos = findBairroCandidatas(bairroSemAbreviacao).sortedWith(compareBy(DifText::matchInicial,
                                                                                        DifText::dif,
                                                                                        DifText::candidato))
        val bairroEscolhido = candidatos.map { it.candidato }.firstOrNull()
        arquivoAprendizado.escrever(bairroSemAbreviacao, candidatos.take(5).joinToString(",") { difText ->
          "${difText.candidato} (${difText.matchInicial},${difText.dif})"
        })
        if (bairroEscolhido != null) {
          aprendizadoBairro[bairroSemAbreviacao] = bairroEscolhido
        }
        bairroEscolhido
      }
    }
  }

  private fun explodeAbreviacoes(bairro: String): String {
    return abreviacoes.entries.fold(bairro) { bairroExp, ent ->
      bairroExp.replace("\b${ent.key}\b".toRegex(), ent.value)
    }
  }

  private fun findBairroCandidatas(palavra: String): List<DifText> {
    val numErro = ((palavra.length - 1) * 90.0 / 100.0).toInt() + 1
    val len = palavra.length
    val prefixo = palavra.substring(0, 1)
    val candidatos = listaBairros.asSequence().filter { log ->
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
      get() = 100 - contaSemelhanca(texto, candidato)
    val difRelativa get() = dif * 100.00 / (matchInicial + 1)
    private fun contaSemelhanca(txt1: String, txt2: String): Int {
      if(txt1.isEmpty()) return 0
      if(txt2.isEmpty()) return 0
      if(txt1[0] != txt2[0]) return 0
      val txtMaior = if (txt1.length > txt2.length) txt1 else txt2
      val txtMenor = if (txt1.length <= txt2.length) txt1 else txt2
      for (i in txtMenor.indices) {
        if (txtMaior[i] != txtMenor[i]) {
          val lenAtual = i + 1
          val len1 = contaSemelhanca(txtMaior.substring(i + 1, txtMaior.length), txtMenor.substring(i, txtMenor.length))
          val len2 = contaSemelhanca(txtMaior.substring(i, txtMaior.length), txtMenor.substring(i + 1, txtMenor.length))
          return lenAtual + maxOf(len1, len2)
        }
      }
      return txtMenor.length
    }
  }

  private fun MutableMap<String, String>.loadAprendizado(arquivoAprendizado: ArquivoTexto): MutableMap<String, String> {
    arquivoAprendizado.ler().forEach { linha ->
      this[linha.getOrNull(0) ?: ""] = linha.getOrNull(1) ?: ""
    }
    return this
  }
}

