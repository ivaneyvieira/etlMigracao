package etlMigracao

val dicionario = DicionarioBairro()

class Bairro(val nome: String) {
  fun processa(): String {
    return dicionario.localiza(nome) ?: ""
  }
}