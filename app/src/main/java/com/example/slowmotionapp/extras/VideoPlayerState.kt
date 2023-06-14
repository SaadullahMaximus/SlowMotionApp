package com.example.slowmotionapp.extras

class VideoPlayerState {

    private var b: String? = null
    private var d = 0
    private var e = 0

    fun setFilename(str: String?) {
        b = str
    }

    fun getStart(): Int {
        return d
    }

    fun getStop(): Int {
        return e
    }

    fun setStop(i: Int) {
        e = i
    }

    fun getDuration(): Int {
        return e - d
    }
}