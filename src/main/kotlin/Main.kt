package com.nschulzke

enum class Stars {
    One, Two, Three;
}

private val mancalaIndices = setOf(6, 13)

data class Puzzle(
    private val board: List<Int>,
    private val turns: Int,
    private val targets: List<Pair<Stars, Int>>,
    val steps: List<Int> = emptyList(),
) {
    fun onBottom(pit: Int): Boolean =
        pit in 0..5

    fun onTop(pit: Int): Boolean =
        pit in 7..12

    fun opposite(pit: Int): Int =
        when (pit) {
            0 -> 12
            1 -> 11
            2 -> 10
            3 -> 9
            4 -> 8
            5 -> 7
            7 -> 5
            8 -> 4
            9 -> 3
            10 -> 2
            11 -> 1
            12 -> 0
            else -> throw IllegalArgumentException("Pit $pit has no opposite")
        }

    fun move(pit: Int): Puzzle {
        if (board[pit] == 0) {
            throw Error("Cannot move empty pit")
        }
        if (pit in mancalaIndices) {
            throw Error("Cannot move mancala")
        }
        val mutableBoard = board.toMutableList()
        val stones = mutableBoard[pit]
        mutableBoard[pit] = 0
        var index = pit
        for (i in 1..stones) {
            index = (index + 1) % mutableBoard.size
            mutableBoard[index]++
        }
        // If the final pit is opposite of another pit that is not empty, capture all pieces
        if (index !in mancalaIndices) {
            val oppositeIndex = opposite(index)
            val mancala = if (onBottom(index)) {
                6
            } else {
                13
            }
            if (mutableBoard[index] == 1 && mutableBoard[oppositeIndex] > 0) {
                mutableBoard[mancala] += mutableBoard[oppositeIndex] + 1
                mutableBoard[index] = 0
                mutableBoard[oppositeIndex] = 0
            }
        }
        return this.copy(
            board = mutableBoard,
            turns = if (index !in mancalaIndices) {
                turns - 1
            } else {
                turns
            },
            steps = steps + pit
        )
    }

    private fun pitToString(index: Int): String =
        board[index].toString().padStart(2, ' ')

    fun score(): Int =
        board[6] + board[13]

    fun starRating(): Stars? =
        targets.lastOrNull { score() >= it.second }?.first

    fun isComplete(): Boolean =
        turns <= 0 || starRating() == Stars.Three

    fun nonEmptyPitIndices(): Iterable<Int> =
        board.indices.filter { board[it] > 0 && !mancalaIndices.contains(it) }

    override fun toString(): String {
        return """
            |   ${pitToString(12)} ${pitToString(11)} ${pitToString(10)} ${pitToString(9)} ${pitToString(8)} ${pitToString(7)}
            |${pitToString(13)}                   ${pitToString(6)}
            |   ${pitToString(0)} ${pitToString(1)} ${pitToString(2)} ${pitToString(3)} ${pitToString(4)} ${pitToString(5)}
            |${turns} turns left
        """.trimMargin()
    }
}

class Solver {
    // Use a search tree to find the minimum number of moves to get 3 stars' worth of stones in the mancalas.
    fun solve(startingPuzzle: Puzzle): Puzzle? {
        val queue = mutableListOf(startingPuzzle)
        val visited = mutableSetOf(startingPuzzle)
        while (queue.isNotEmpty()) {
            val puzzle = queue.removeAt(0)
            if (puzzle.isComplete()) {
                if (puzzle.starRating() == Stars.Three) {
                    return puzzle
                }
            } else {
                for (pit in puzzle.nonEmptyPitIndices()) {
                    val nextGame = puzzle.move(pit)
                    if (nextGame !in visited) {
                        queue.add(nextGame)
                        visited.add(nextGame)
                    }
                }
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val puzzle = Puzzle(
                board = mutableListOf(
                    1, 0, 3, 0, 4, 0, 0,
                    1, 0, 0, 0, 2, 0, 0,
                ),
                turns = 3,
                targets = listOf(
                    Stars.One to 8,
                    Stars.Two to 9,
                    Stars.Three to 10,
                ),
            )
            println(puzzle)
            val solver = Solver()
            val solution = solver.solve(puzzle)
            if (solution == null) {
                println("No solution found.")
            } else {
                solution.steps.fold(puzzle) { acc, next ->
                    acc.move(next).also { println("\nAfter moving $next:\n$it") }
                }
            }
        }
    }
}
