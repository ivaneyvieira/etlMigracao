package etlMigracao

data class ResumoParceiro(
  val codigo: Int,
  val tipoPessoa: String,
  val cpfCnpj: String,
  val situacao: String,
  val nome: String,
  val cep: String,
  val enderecoSaci: String,
  val tipoShankhya: String,
  val logradouroShankhya: String,
  val numeroShankhya: String,
  val complementoShankhya: String,
  val bairroSaci: String,
  val bairroShankhya: String,
  val cidadeSaci: String,
  val cidadeShankhya: String,
  val uf: String,
  val tipoCadastro: String,
                         )