package com.shahartal.myquietchannel

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal object HaftarahConnection {
    private const val TITLE = "על הקשר בין ההפטרה לפרשה"

    fun sourceUrl(parashaName: String): String {
        val slug = parashaName
            .replace(Regex("^פרשת\\s+"), "")
            .replace(Regex("[\\u0591-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4-\\u05C5\\u05C7]"), "")
            .replace(Regex("[־‐\\-‒–—―\\s]+"), "-")
            .trim('-')

        return "https://kol-kore.org/${encodePathSegment("פרשות")}/" +
            "${encodePathSegment("הפטרה-פרשת-$slug")}/"
    }

    // fun proxyUrl(sourceUrl: String): String =
    //     "https://myquietwave.lat-shahar.workers.dev/?url=${encodeQueryValue(sourceUrl)}"

    fun extract(html: String): String {
        val heading = Regex("(?is)<h[1-4]\\b[^>]*>(.*?)</h[1-4]>")
            .findAll(html)
            .firstOrNull { plainText(it.groupValues[1]).replace(Regex("\\s+"), " ").trim() == TITLE }
            ?: error("The requested heading was not found")

        val rowStart = Regex("(?is)<div\\b[^>]*class\\s*=\\s*(['\"])[^'\"]*\\brow_four\\b[^'\"]*\\1[^>]*>")
            .findAll(html, 0)
            .takeWhile { it.range.first < heading.range.first }
            .lastOrNull()
            ?.range?.first
            ?: error("The requested section was not found")
        val row = extractDiv(html, rowStart)
        val contentStart = Regex("(?is)<div\\b[^>]*class\\s*=\\s*(['\"])[^'\"]*\\bcontent_right\\b[^'\"]*\\1[^>]*>")
            .find(row)
            ?.range?.first
            ?: error("The requested content was not found")
        val content = extractDiv(row, contentStart)

        val text = content
            .replace(Regex("(?is)<br\\s*/?>"), "\n")
            .replace(Regex("(?is)</(?:p|li)\\s*>"), "\n\n")
            .let(::plainText)
            .replace('\u00a0', ' ')
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
        val relevantStart = text.indexOf("נושאים בפרשה:")
        return if (relevantStart >= 0) text.substring(relevantStart) else text
    }

    private fun extractDiv(html: String, start: Int): String {
        var depth = 0
        val divTag = Regex("(?is)</?div\\b[^>]*>")
        for (tag in divTag.findAll(html, start)) {
            if (tag.value.startsWith("</", ignoreCase = true)) depth-- else depth++
            if (depth == 0) return html.substring(start, tag.range.last + 1)
        }
        error("Unclosed div")
    }

    private fun plainText(html: String): String = html
        .replace(Regex("(?is)<[^>]+>"), "")
        .replace(Regex("&#(x?[0-9a-fA-F]+);")) { match ->
            val value = match.groupValues[1]
            val codePoint = if (value.startsWith("x", ignoreCase = true)) {
                value.substring(1).toInt(16)
            } else {
                value.toInt()
            }
            String(Character.toChars(codePoint))
        }
        .replace("&nbsp;", " ", ignoreCase = true)
        .replace("&amp;", "&", ignoreCase = true)
        .replace("&quot;", "\"", ignoreCase = true)
        .replace("&#39;", "'", ignoreCase = true)
        .replace("&lt;", "<", ignoreCase = true)
        .replace("&gt;", ">", ignoreCase = true)

    private fun encodePathSegment(value: String): String = encodeQueryValue(value).replace("+", "%20")

    private fun encodeQueryValue(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
}
