package com.example.slowmotionapp.extras

class VideoPlayerState {

    private var a = 0
    private var b: String? = null
    private var c: String? = null
    private var d = 0
    private var e = 0

    fun getMessageText(): String? {
        return c
    }

    fun setMessageText(str: String?) {
        c = str
    }

    fun getFilename(): String? {
        return b
    }

    fun setFilename(str: String?) {
        b = str
    }

    fun getStart(): Int {
        return d
    }

    fun setStart(i: Int) {
        d = i
    }

    fun getStop(): Int {
        return e
    }

    fun setStop(i: Int) {
        e = i
    }

    fun reset() {
        e = 0
        d = 0
    }

    fun getDuration(): Int {
        return e - d
    }

    fun getCurrentTime(): Int {
        return a
    }

    fun setCurrentTime(i: Int) {
        a = i
    }

    fun isValid(): Boolean {
        return e > d
    }
}