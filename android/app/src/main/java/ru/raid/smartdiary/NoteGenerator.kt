package ru.raid.smartdiary

import java.io.File
import java.util.*

object NoteGenerator {
    private val random = Random()

    fun generateNote(image: File) = ru.raid.smartdiary.db.Note(
        0,
        generateText(4, 8, random.nextInt(2) + 1),
        generateText(3, 10, random.nextInt(20) + 20),
        image.absolutePath,
        Calendar.getInstance().timeInMillis
    )

    private fun generateWord(minWordLen: Int, maxWordLen: Int, first_uppercase: Boolean = false): String {
        val result = StringBuilder()
        val len = random.nextInt(maxWordLen + 1 - minWordLen) + minWordLen
        for (j in 0 until len) {
            val alphabet = if (first_uppercase && j == 0) {
                ALPHABET_UPPERCASE
            } else {
                ALPHABET_LOWERCASE
            }
            result.append(alphabet[random.nextInt(alphabet.length)])
        }
        return result.toString()
    }

    private fun generateText(minWordLen: Int, maxWordLen: Int, words: Int): String {
        val result = StringBuilder()
        for (i in 0 until words) {
            if (i != 0)
                result.append(' ')
            result.append(generateWord(minWordLen, maxWordLen, i == 0 || random.nextBoolean()))
        }
        return result.toString()
    }

    private const val TOTAL_IMAGES_AVAILABLE = 20
    private const val ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
}
