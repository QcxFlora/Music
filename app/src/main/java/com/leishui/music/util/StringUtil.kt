package com.leishui.music.util

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.widget.TextView
import com.leishui.music.result.SongBean.Track

object StringUtil {
    fun musicName(data: Track,tv:TextView) {
        val str: String
        val t1 = data.name
        if (data.ar[0].tns.size == 1)
            str = toHtml(t1, data.ar[0].tns.toString())
        else if (data.al.tns.size == 1)
            str = toHtml(t1, data.al.tns.toString())
        else if (data.tns?.size == 1)
            str = toHtml(t1, data.tns[0])
        else if (data.alia.size == 1)
            str = toHtml(t1, data.alia.toString())
        else
            str = t1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            tv.text = Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY)
        else
            tv.text = Html.fromHtml(str)
    }
    fun musicName(data: Track): String{
        val str: String
        val t1 = data.name
        if (data.ar[0].tns.size == 1)
            str = format(t1, data.ar[0].tns.toString())
        else if (data.al.tns.size == 1)
            str = format(t1, data.al.tns.toString())
        else if (data.tns?.size == 1)
            str = format(t1, data.tns.toString())
        else if (data.alia.size == 1)
            str = format(t1, data.alia.toString())
        else
            str = t1
        return str
    }

    private fun toHtml(t1: String, t2: String): String {
        val t3 = "(" + t2.substring(1, t2.length - 1) + ")"
        return "$t1<font color='#A9A9A9'>$t3</font>"
    }

    private fun format(t1: String, t2: String): String {
        val t3 = "(" + t2.substring(1, t2.length - 1) + ")"
        return t1 + t3
    }

    fun arName(track: Track, tvArName: TextView) {
        tvArName.text = track.ar[0].name
    }
}