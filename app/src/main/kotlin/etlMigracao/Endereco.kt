package etlMigracao

val dicionarioLogradouro = DicionarioLogradouro()

enum class RegexTipos(val regex: Regex) {
  AVENIDA("A|AV|AVE[NIDA]*|RAV|RV".toRegex()),
  ASSENTAMENTO("ASS[ENTAMO]*|ACEN[TAMENO]*|ASEN[TAMENO]*".toRegex()),
  ALAMEDA("ALAM[EDA]*|AL".toRegex()),
  BECO("BECO|BEC".toRegex()),
  RODOVIA("BR|ROD[OVIA]*|RD".toRegex()),
  CHACARA("CHACARA".toRegex()),
  EDFICIO("ED|EDIFICIO".toRegex()),
  CONJUNTO("CJ|CONJ|COJ|CNJ|CONJUNTO".toRegex()),
  CONDOMINIO("COD|CODN|CON|COND|COMD|CONDOMINIO".toRegex()),
  ESTRADA("EST[RAD]*|ES".toRegex()),
  LOTEAMENTO("LOT|LT|LOTEAMENTO".toRegex()),
  LOCALIDADE("LOC[ALIDE]*|LC".toRegex()),
  PRACA("PC|PCA|PRA[CA]*|PRCA".toRegex()),
  POVOADO("POV[OAD]*|PV".toRegex()),
  PARQUE("PQ|PRQ".toRegex()),
  RESIDENCIAL("RES[IDENCAL]*|RS|RED".toRegex()),
  RUA("R[UA]*".toRegex()),
  SITIO("SITIO".toRegex()),
  TRAVESSA("TRAV[ESA]*|TRV|TV".toRegex()),
  VILA("VL|VILA".toRegex()),
  VIA("VIA".toRegex())
}

data class Endereco(val tipo: String, val logradouro: String, val numero: String, val complemento: String) {
  companion object {
    val regExTipo = RegexTipos.values().joinToString(separator = "|", prefix = "(", postfix = ")") {
      it.regex.pattern
    }
    val regExLog = "([A-Z ]+?|[0-9]+?[A-Z\\-]*?|[A-Z\\-]+?[0-9]+?|[0-9O]+? [A-Z\\- ]+?)"
    val regExNumero = "(S N|SN|S.N|N[0-9O]+|N [0-9O]+|[0-9O]+)"
    val regexComplemento = listOf("QD .+|Q .+|Q[UADRA]* .+|RQ",
                                  "C[ASA]* .+",
                                  "APTO .+|APT .+|AP .+",
                                  "LOT .+|LT .+|LOTE .+",
                                  "BR .+",
                                  "SETOR .+",
                                  "KM.+",
                                  "B .+|BL[OCO]* .+",
                                  "ST .+",
                                  "R .+").joinToString(separator = "|", prefix = "(", postfix = ")")
    val regexComplementoGeral = "(.+)"

    val regExpSemNumero = "^$regExTipo $regExLog $regexComplemento$".toRegex()
    val regExpSemComplemento = "^$regExTipo $regExLog $regExNumero$".toRegex()
    val regExpCompleto = "^$regExTipo $regExLog $regExNumero $regexComplementoGeral$".toRegex()
    val regExpSemNumeroComplemento = "^$regExTipo $regExLog$".toRegex()
    val regExpSomenteComplemento = "^$regexComplemento$".toRegex()

    fun decompoem(enderecoSaci: String): Endereco? {
      return when {
        regExpSemNumero.containsMatchIn(enderecoSaci)            -> decompoemSemNumero(enderecoSaci)
        regExpSemComplemento.containsMatchIn(enderecoSaci)       -> decompoemSemComplemento(enderecoSaci)
        regExpCompleto.containsMatchIn(enderecoSaci)             -> decompoemCompleto(enderecoSaci)
        regExpSemNumeroComplemento.containsMatchIn(enderecoSaci) -> decompoemSemNumeroComplemento(enderecoSaci)
        regExpSomenteComplemento.containsMatchIn(enderecoSaci)   -> decompoemSomenteComplemento(enderecoSaci)
        else                                                     -> null
      }
    }

    val mapEndereco = mutableMapOf<String, Endereco?>()

    fun decompoemCompleto(enderecoSaci: String): Endereco? {
      if(mapEndereco.containsKey(enderecoSaci)) return mapEndereco[enderecoSaci]
      else {
        val match = regExpCompleto.find(enderecoSaci) ?: return null
        val (tipo, logradouro, numero, complemento) = match.destructured
        val endereco = Endereco(tipo = trataTipo(tipo),
                        logradouro = trataLogradoutro(logradouro),
                        numero = trataNumero(numero),
                        complemento = trataComplemento(complemento))
        mapEndereco[enderecoSaci] = endereco
        return endereco
      }
    }

    fun decompoemSemComplemento(enderecoSaci: String): Endereco? {
      val match = regExpSemComplemento.find(enderecoSaci) ?: return null
      val (tipo, logradouro, numero) = match.destructured
      return Endereco(tipo = trataTipo(tipo),
                      logradouro = trataLogradoutro(logradouro),
                      numero = trataNumero(numero),
                      complemento = "")
    }

    fun decompoemSemNumeroComplemento(enderecoSaci: String): Endereco? {
      val match = regExpSemNumeroComplemento.find(enderecoSaci) ?: return null
      val (tipo, logradouro) = match.destructured
      return Endereco(tipo = trataTipo(tipo), logradouro = trataLogradoutro(logradouro), numero = "", complemento = "")
    }

    fun decompoemSemNumero(enderecoSaci: String): Endereco? {
      val match = regExpSemNumero.find(enderecoSaci) ?: return null
      val (tipo, logradouro, complemento) = match.destructured
      return Endereco(tipo = trataTipo(tipo),
                      logradouro = trataLogradoutro(logradouro),
                      numero = "",
                      complemento = trataComplemento(complemento))
    }

    fun decompoemSomenteComplemento(enderecoSaci: String): Endereco? {
      val match = regExpSomenteComplemento.find(enderecoSaci) ?: return null
      val (complmento) = match.destructured
      return Endereco(tipo = "", logradouro = "", numero = "", complemento = complmento)
    }

    private fun trataComplemento(complemento: String): String {
      return complemento
    }

    private fun trataNumero(numero: String): String {
      return numero.replace("[^0-9O]".toRegex(), "").replace("O", "0")
    }

    private fun trataLogradoutro(logradouro: String): String {
      return logradouro.trataLogradouro()
    }

    private fun trataTipo(tipo: String): String {
      return RegexTipos.values().firstOrNull { rTipo ->
        rTipo.regex.matches(tipo)
      }?.toString() ?: ""
    }

    private fun String.trataLogradouro(): String {
      return dicionarioLogradouro.localiza(this)
    }
  }
}

