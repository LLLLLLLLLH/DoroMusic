package com.doro.music.player.util

object FractionalIndexer {

    private const val BASE_62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val MID = BASE_62.length / 2

    fun generateBetween(before: String?, after: String?): String {
        if (before == null && after == null) return "a0"
        if (before == null) return generateBefore(after!!)
        if (after == null) return generateAfter(before)
        return generateBetweenInternal(before, after)
    }

    private fun generateBetweenInternal(before: String, after: String): String {
        require(before < after) { "before must be < after, got before=$before, after=$after" }

        val commonPrefix = before.commonPrefixWith(after)
        val beforeRest = before.substring(commonPrefix.length)
        val afterRest = after.substring(commonPrefix.length)

        if (beforeRest.isEmpty()) {
            val afterFirst = BASE_62.indexOf(afterRest.first())
            return if (afterFirst > 0) {
                commonPrefix + BASE_62[afterFirst / 2]
            } else {
                before + BASE_62[MID]
            }
        }

        if (afterRest.isEmpty()) {
            return before + BASE_62[MID]
        }

        val beforeFirst = BASE_62.indexOf(beforeRest.first())
        val afterFirst = BASE_62.indexOf(afterRest.first())

        if (afterFirst - beforeFirst > 1) {
            return commonPrefix + BASE_62[beforeFirst + (afterFirst - beforeFirst) / 2]
        }

        return commonPrefix + beforeRest.first() + BASE_62[MID]
    }

    private fun generateBefore(after: String): String {
        val firstChar = after.first()
        val firstIdx = BASE_62.indexOf(firstChar)
        if (firstIdx > 1) {
            return BASE_62[firstIdx - 1] + BASE_62.last().toString()
        }
        return firstChar + generateBefore(after.substring(1))
    }

    private fun generateAfter(before: String): String {
        val lastChar = before.last()
        val lastIdx = BASE_62.indexOf(lastChar)
        if (lastIdx < BASE_62.length - 2) {
            return before.dropLast(1) + BASE_62[lastIdx + 1]
        }
        return before + BASE_62[0]
    }

    fun generateInitialList(count: Int): List<String> {
        return (0 until count).map { "a$it" }
    }

    // 只有业务保证所有参数和返回值非空时再用
    fun generateBetweenList(before: String, after: String?, count: Int): List<String> =
        generateSequence(before) { prev -> generateBetween(prev, after) }
            .take(count)
            .toList()

}
