package com.amegane3231.qrshare.extentionFunction

import android.text.Spannable
import androidx.core.text.getSpans

fun CharSequence.isURL(): Boolean {
    val regex = "(http://|https://|www.)[\\w.\\-/:#?=&;%~+]+"
    val urls = regex.toRegex(RegexOption.IGNORE_CASE).findAll(this).map { it.value }
    return urls.toList().isNotEmpty()
}