package ch.bildspur.realsense.utils

class RangeFinder {
    var min = Int.MAX_VALUE
    var max = Int.MIN_VALUE

    fun update(value : Int)
    {
        min = Math.min(value, min)
        max = Math.max(value, max)
    }
}