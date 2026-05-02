package ms.mattschlenkrich.billsprojectionv2.common

enum class TimeRange {
    SHOW_ALL,
    LAST_MONTH,
    LAST_YEAR,
    DATE_RANGE
}

enum class AnalysisMode {
    BUDGET_RULE,
    ACCOUNT,
    SEARCH,
    NONE
}