package androidx.shade.migrate

interface DataMigrateInfo {
    val range: IntRange
    val action: suspend () -> Unit
}