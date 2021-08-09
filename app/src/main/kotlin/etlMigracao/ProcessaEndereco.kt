package etlMigracao

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.sql2o.Query

object ProcessaEndereco {
  val sizeThread = 5000

  val sql = """SELECT tipoShankhya, codigo, tipoPessoa, cpfCnpj, situacao, nome, 
    cep, EnderecoSaci, logradouroShankhya, numeroShankhya, complementoShankhya, BairroSaci, 
    bairroShankhya, cidadeSaci, cidadeShankhya, uf, tipoCadastro
    FROM migracao.T_RESUMO AS S
      WHERE  tipoCadastro = ''"""

  /*
    val sql = """SELECT * FROM  migracao.T_RESUMO
  WHERE tipoCadastro = 'ERRO'
    AND EnderecoSaci NOT LIKE ''
    AND EnderecoSaci NOT REGEXP '^[0-9]'
    AND codigo != 101334
    ORDER BY codigo"""
  */

  val updateResumo = """
UPDATE migracao.T_RESUMO
SET bairroShankhya=:bairroShankhya, 
    cidadeShankhya=:cidadeShankhya, 
    complementoShankhya=:complementoShankhya,  
    logradouroShankhya=:logradouroShankhya, 
    numeroShankhya=:numeroShankhya,  
    tipoShankhya=:tipoShankhya, 
    tipoCadastro=:tipoCadastro
WHERE codigo=:codigo 
  AND tipoPessoa=:tipoPessoa
  AND cpfCnpj=:cpfCnpj
"""

  suspend fun execute() {
    mysqlOCI.open().use { con ->
      con.createQuery(sql).executeAndFetchLazy(ResumoParceiro::class.java).use { tasks ->
        tasks.asSequence().organizaEnderecos()./*organizaBairros().*/agrupaParaSalvar(sizeThread).save() //
        // .debug()
      }
    }
  }

  private fun Sequence<ResumoParceiro>.debug() {
    forEachIndexed { index, resumo ->
      if (index % 1000 == 0) println(index)
      if (resumo.tipoCadastro == "UPDATE" && resumo.logradouroShankhya != "") println("${resumo.enderecoSaci} -> " + resumo.logradouroShankhya)
    }
  }

  private fun Sequence<ResumoParceiro>.organizaEnderecos(): Sequence<ResumoParceiro> = map { resumo ->
    if (resumo.logradouroShankhya == "") {
      val enderecoSaci = limpaString(resumo.enderecoSaci)
      if (enderecoSaci == "") resumo.copy(tipoCadastro = "ERRO")
      else {
        val endereco = Endereco.decompoem(enderecoSaci)
        if (endereco == null) resumo.copy(tipoCadastro = "ERRO")
        else {
          resumo.copy(tipoShankhya = endereco.tipo,
                      logradouroShankhya = endereco.logradouro,
                      numeroShankhya = endereco.numero,
                      complementoShankhya = endereco.complemento,
                      tipoCadastro = "UPDATE")
        }
      }
    }
    else resumo
  }

  private fun limpaString(texto: String): String {
    val resumoAjustado = texto.replace("[^A-Z0-9 ]+".toRegex(), " ").trim()
    val endereco = resumoAjustado.split(" ").flatMap { palavra ->
      val palavraMaiuscula = palavra.toUpperCase()
      val match1 = "^([0-9]+)([A-Z]+)$".toRegex().find(palavraMaiuscula)
      val match2 = "^([A-Z]+)([0-9]+)$".toRegex().find(palavraMaiuscula)
      when {
        match1 != null -> {
          val (parte1, parte2) = match1.destructured
          listOf(parte1, parte2)
        }
        match2 != null -> {
          val (parte1, parte2) = match2.destructured
          listOf(parte1, parte2)
        }
        else           -> {
          listOf(palavraMaiuscula)
        }
      }
    }.joinToString(" ")
    return endereco
  }

  private suspend fun Sequence<List<ResumoParceiro>>.save() {
    val requestSemaphore = Semaphore(8)
    val futures = this.mapIndexed { index, resumos ->
      GlobalScope.async {
        requestSemaphore.withPermit {
          println("Executando ${index * sizeThread} ...")
          mysqlOCI.beginTransaction().use { con ->
            val query = con.createQuery(updateResumo)
            resumos.forEach { resumo ->
              resumo.updateSql(query)
              query.addToBatch()
            }
            query.executeBatch()
            con.commit()
          }
        }
        println("Finalizado ${index * sizeThread}")
      }
    }
    futures.toList().awaitAll()
  }

  private fun Sequence<ResumoParceiro>.agrupaParaSalvar(size: Int): Sequence<List<ResumoParceiro>> {
    return this.filter { it.tipoCadastro == "UPDATE" }.windowed(size, step = size, partialWindows = true)
  }

  private fun ResumoParceiro.updateSql(query: Query) {
    query.addParameter("bairroShankhya", bairroShankhya)
    query.addParameter("cidadeShankhya", cidadeShankhya)
    query.addParameter("complementoShankhya", complementoShankhya)
    query.addParameter("logradouroShankhya", logradouroShankhya)
    query.addParameter("numeroShankhya", numeroShankhya)
    query.addParameter("tipoShankhya", tipoShankhya)
    query.addParameter("tipoCadastro", tipoCadastro)
    query.addParameter("codigo", codigo)
    query.addParameter("tipoPessoa", tipoPessoa)
    query.addParameter("cpfCnpj", cpfCnpj)
  }

  private fun Sequence<ResumoParceiro>.organizaBairros(): Sequence<ResumoParceiro> {

    return this.map { resumo ->
      if (resumo.bairroShankhya == "") {
        val bairro = limpaString(resumo.bairroSaci)
        if (bairro == "") resumo.copy(bairroShankhya = bairro)
        else {
          val bairroProcessado = Bairro(bairro).processa()
          resumo.copy(bairroShankhya = bairroProcessado)
        }
      }
      else resumo
    }
  }
}









