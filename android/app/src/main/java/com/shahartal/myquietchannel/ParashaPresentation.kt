package com.shahartal.myquietchannel

internal data class ParashaNames(
    val primary: String,
    val secondary: String?,
)

internal object ParashaPresentation {
    private val maqafOnlyNames = setOf("כי־תצא", "כי־תבוא", "שלח־לך", "לך־לך")

    fun names(hebrew: String): ParashaNames {
        var primary = hebrew
        var secondary: String? = null

        if (primary.contains("-")) {
            val parts = primary.split("-", limit = 2)
            primary = parts[0]
            secondary = "פרשת ${parts[1]}"
        }

        if (primary in maqafOnlyNames) {
            primary = primary.replace("־", "_")
            secondary = null
        } else if (primary.contains("־")) {
            val parts = primary.split("־", limit = 2)
            primary = parts[0]
            secondary = "פרשת ${parts[1]}"
        }

        return ParashaNames(primary = primary, secondary = secondary)
    }
}
