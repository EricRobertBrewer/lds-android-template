package org.jdc.template.ui.menu

import android.content.Context
import android.view.MenuItem
import org.jdc.template.InternalIntents
import org.jdc.template.R
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonMenu
@Inject
constructor() {

    @Inject
    lateinit var internalIntents: InternalIntents

    fun onOptionsItemSelected(context: Context, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_search -> return true
            R.id.menu_item_settings -> {
                internalIntents.showSettings(context)
                return true
            }
            R.id.menu_item_about -> {
                internalIntents.showHelp(context)
                return true
            }
            else -> {
                Timber.i("Unknown common menu item id: ${item.itemId}, ignoring")
                return false
            }
        }
    }
}
