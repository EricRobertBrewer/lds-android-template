package org.jdc.template.prefs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import org.jdc.template.prefs.base.PrefsContainer
import org.jdc.template.prefs.base.PrefsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Prefs @Inject constructor() : PrefsContainer(PrefsManager.COMMON_NAMESPACE)  {
    private val _sortLiveData = MutableLiveData<Sort>()
    val sortLiveData: LiveData<Sort>
        get() = _sortLiveData

    var version by SharedPref(0, key = PREF_VERSION)
    var developerMode by SharedPref(false, key = PREF_DEVELOPER_MODE)
    var sort by EnumPref(Sort.ALPHA, key = PREF_SORT, liveData = _sortLiveData)

    init {
        _sortLiveData.value = sort
    }

    fun toggleDeveloperMode(): Boolean {
        developerMode = !developerMode

        return developerMode
    }

    fun toggleSort() {
        sort = when (sort) {
            Sort.ALPHA -> Sort.BETA
            Sort.BETA -> Sort.ALPHA
        }
    }

    companion object {
        private const val PREF_VERSION = "PREF_VERSION"
        private const val PREF_DEVELOPER_MODE = "PREF_DEVELOPER_MODE"
        private const val PREF_SORT = "PREF_SORT"
    }
}

enum class Sort {
    ALPHA,
    BETA
}
