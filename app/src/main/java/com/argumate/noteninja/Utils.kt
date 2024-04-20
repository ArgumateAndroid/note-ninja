package com.argumate.noteninja

import android.content.Context
import android.media.MediaPlayer

object Utils {
    fun getDropDownRange(start: Int = 0): List<Int> {
        return (start..48).toList()
    }

    fun mapToNote(note: Int, scale:Int): String {
        val getList = getList(scale)
        return getList[note % 7] + " " + (note / 7 + 1).toString()
    }

    fun playMusic(context: Context, musicFileName: String) {
        val resId = context.resources.getIdentifier(musicFileName, "raw", context.packageName)
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener { mp -> mp.release() })
        mediaPlayer.start()
    }

    fun getList(ind: Int): List<String> {
        if (ind == 2) {
            return listOf("Sa", "Re", "Ga", "Ma", "Pa", "Dha", "Ni")
        }
        if (ind == 0) {
            return listOf("C", "D", "E", "F", "G", "A", "B")
        }
        if (ind == 1) {
            return listOf("Do", "Re", "Mi", "Fa", "So", "La", "Ti")
        }
        return listOf()

    }
}