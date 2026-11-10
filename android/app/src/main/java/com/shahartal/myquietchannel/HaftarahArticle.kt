package com.shahartal.myquietchannel

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

internal data class HaftarahArticle(
    val sourceUrl: String,
    val text: String,
)

internal object HaftarahArticleParser {
    private const val BASE_URL = "https://www.haftara.co.il/"

    fun sourceUrl(parashaName: String): String {
        val slug = parashaName
            .replace(Regex("^פרשת\\s+"), "")
            .replace(Regex("[\\u0591-\\u05BD\\u05BF\\u05C1-\\u05C2\\u05C4-\\u05C5\\u05C7]"), "")
            .replace(Regex("[־‐\\-‒–—―\\s]+"), "-")
            .trim('-')
        val articlePath = URLEncoder.encode("הפטרת-פרשת-$slug", StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
        return BASE_URL + articlePath + "/"
    }

    fun extract(html: String): String {
        val entryContent = Regex("(?is)<div\\b[^>]*>")
            .findAll(html)
            .firstOrNull { match ->
                Regex("(?is)\\bclass\\s*=\\s*(['\"])[^'\"]*\\bentry-content\\b[^'\"]*\\1")
                    .containsMatchIn(match.value)
            }
            ?.range?.first
            ?: error("The article content was not found")

        val content = extractDiv(html, entryContent)
            .replace(Regex("(?is)<script\\b[^>]*>.*?</script\\s*>"), "")
            .replace(Regex("(?is)<style\\b[^>]*>.*?</style\\s*>"), "")
            .replace(Regex("(?is)<br\\s*/?>"), "\n")
            .replace(Regex("(?is)</(?:p|li|h[1-6])\\s*>"), "\n\n")

        val text = plainText(content)
            .replace('\u00a0', ' ')
            .lines()
            .joinToString("\n") { it.trim() }
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
        return text.ifBlank { error("The article content was empty") }
    }

    private fun extractDiv(html: String, start: Int): String {
        var depth = 0
        val divTag = Regex("(?is)</?div\\b[^>]*>")
        for (tag in divTag.findAll(html, start)) {
            if (tag.value.startsWith("</", ignoreCase = true)) depth-- else depth++
            if (depth == 0) return html.substring(start, tag.range.last + 1)
        }
        error("Unclosed article content")
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
}

internal class CachedHaftarahArticleRepository(
    private val client: TextHttpClient,
) {
    private val cache = ConcurrentHashMap<String, HaftarahArticle>()

    fun article(parashaName: String): HaftarahArticle {
        val url = HaftarahArticleParser.sourceUrl(parashaName)
        return cache[url] ?: synchronized(cache) {
            cache[url] ?: HaftarahArticleParser.extract(client.get(url))
                .let { HaftarahArticle(url, it) }
                .also { cache[url] = it }
        }
    }
}
