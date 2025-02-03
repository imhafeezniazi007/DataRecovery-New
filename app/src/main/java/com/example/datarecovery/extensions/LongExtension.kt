package com.example.datarecovery.extensions

fun Long.getFileSize(): String {

    val n: Long = 1000
    var s = ""
    val kb = this / n.toDouble()
    val mb = kb / n
    val gb = mb / n
    val tb = gb / n
    if (this < n) {
        s = "$this Bytes"
    } else if (this >= n && this < n * n) {
        s = String.format("%.2f", kb) + " KB"
    } else if (this >= n * n && this < n * n * n) {
        s = String.format("%.2f", mb) + " MB"
    } else if (this >= n * n * n && this < n * n * n * n) {
        s = String.format("%.2f", gb) + " GB"
    } else if (this >= n * n * n * n) {
        s = String.format("%.2f", tb) + " TB"
    }
    return s

}

