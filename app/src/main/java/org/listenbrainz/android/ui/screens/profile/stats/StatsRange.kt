package org.listenbrainz.android.ui.screens.profile.stats

enum class StatsRange (val rangeString: String, val apiIdenfier: String){
    THIS_WEEK("This Week", "this_week"),
    THIS_MONTH("This Month", "this_month"),
    THIS_YEAR("This Year", "this_year"),
    LAST_WEEK("Tast Week", "week"),
    LAST_MONTH("Last Month", "month"),
    LAST_YEAR("Last Year", "year"),
    ALL_TIME("All Time", "all_time"),
}