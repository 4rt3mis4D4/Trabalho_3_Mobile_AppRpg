package com.example.app_rpg.data.dice

data class DiceStats(
    val expression: DiceExpression,
    val outcomes: List<Outcome>
) {
    val median: Int = outcomes.median()
}

data class DiceExpression(
    val count: Int,
    val sides: Int,
    val modifier: Int = 0
) {
    val min: Int get() = count + modifier
    val max: Int get() = count * sides + modifier
    val mean: Double get() = count * (sides + 1) / 2.0 + modifier

    override fun toString(): String = buildString {
        append(count).append('d').append(sides)
        when {
            modifier > 0 -> append(" + ").append(modifier)
            modifier < 0 -> append(" - ").append(-modifier)
        }
    }
}

data class Outcome(val total: Int, val probability: Double)

private val DICE_REGEX = Regex("""^\s*(\d+)?\s*[dD]\s*(\d+)\s*(?:([+-])\s*(\d+)\s*)?$""")

private const val MAX_DICE = 100
private const val MAX_SIDES = 1000

fun parseDiceExpression(input: String): Result<DiceExpression> {
    val match = DICE_REGEX.matchEntire(input)
        ?: return fail("Expressão inválida. Use algo como \"2d6 + 3\".")

    val (countText, sidesText, sign, constantText) = match.destructured

    val count = countText.ifEmpty { "1" }.toIntOrNull()
        ?: return fail("Quantidade de dados grande demais.")

    val sides = sidesText.toIntOrNull()
        ?: return fail("Número de faces grande demais.")

    if (count !in 1..MAX_DICE) return fail("A quantidade de dados deve estar entre 1 e $MAX_DICE.")
    if (sides !in 2..MAX_SIDES) return fail("O dado deve ter entre 2 e $MAX_SIDES faces.")

    val modifier = if (constantText.isEmpty()) 0 else {
        val magnitude = constantText.toIntOrNull() ?: return fail("Modificador grande demais.")
        if (sign == "-") -magnitude else magnitude
    }

    return Result.success(DiceExpression(count, sides, modifier))
}

private fun fail(message: String): Result<DiceExpression> =
    Result.failure(IllegalArgumentException(message))

fun DiceExpression.distribution(): List<Outcome> {
    val p = 1.0 / sides
    var probs = doubleArrayOf(1.0)

    repeat(count) {
        val next = DoubleArray(probs.size + sides)
        for (i in probs.indices) {
            val carried = probs[i]
            if (carried == 0.0) continue
            for (face in 1..sides) {
                next[i + face] += carried * p
            }
        }
        probs = next
    }

    return (count..count * sides).map { raw ->
        Outcome(total = raw + modifier, probability = probs[raw])
    }
}

private const val EPSILON = 1e-9

fun List<Outcome>.median(): Int {
    require(isNotEmpty()) { "Cannot take the median of an empty distribution." }

    var cumulative = 0.0
    for (outcome in this) {
        cumulative += outcome.probability
        if (cumulative >= 0.5 - EPSILON) return outcome.total
    }
    return last().total
}
