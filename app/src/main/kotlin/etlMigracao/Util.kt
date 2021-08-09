package etlMigracao

data class TimeResult(val tempoExecucao: Int, val tempoRecuperacao: Int)

fun timeMetric(block: () -> Unit): Int {
  val time1 = System.nanoTime()
  block()
  val time2 = System.nanoTime()
  return ((time2 * 1.0 - time1 * 1.0) / 1000000.00).toInt()
}

fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
  if (lhs == rhs) {
    return 0
  }
  if (lhs.isEmpty()) {
    return rhs.length
  }
  if (rhs.isEmpty()) {
    return lhs.length
  }
  val lhsLength = lhs.length
  val rhsLength = rhs.length

  var cost = IntArray(lhsLength + 1) { it }
  var newCost = IntArray(lhsLength + 1) { 0 }

  for (i in 1..rhsLength) {
    newCost[0] = i

    for (j in 1..lhsLength) {
      val editCost = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

      val costReplace = cost[j - 1] + editCost
      val costInsert = cost[j] + 1
      val costDelete = newCost[j - 1] + 1

      newCost[j] = minOf(costInsert, costDelete, costReplace)
    }

    val swap = cost
    cost = newCost
    newCost = swap
  }

  return cost[lhsLength]
}
