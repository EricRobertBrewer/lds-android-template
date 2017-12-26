package org.jdc.template.ux.startup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.analytics.HitBuilders
import com.google.gson.Gson
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import okhttp3.*
import org.jdc.template.Analytics
import org.jdc.template.BuildConfig
import org.jdc.template.R
import org.jdc.template.datasource.database.main.individual.Individual
import org.jdc.template.datasource.database.main.individual.IndividualDao
import org.jdc.template.datasource.database.main.individual.directory.OtherDirectory
import org.jdc.template.datasource.database.main.individual.directory.StarWarsDirectory
import org.jdc.template.datasource.webservice.ServiceModule
import org.jdc.template.inject.Injector
import org.jdc.template.prefs.base.PrefsContainer
import org.jdc.template.util.CoroutineContextProvider
import org.jdc.template.ux.directory.DirectoryActivity
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class StartupActivity : Activity() {

    @Inject
    lateinit var analytics: Analytics

    @Inject
    @field:Named(ServiceModule.STANDARD_CLIENT)
    lateinit var standardClient: OkHttpClient

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var cc: CoroutineContextProvider

    @Inject
    lateinit var individualDao: IndividualDao

    private val compositeJob = Job()

    private val debugStartup = false

    init {
        Injector.get().inject(this)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        @Suppress("ConstantConditionIf") // used for debugging
        if (debugStartup) {
            devPauseStartup()
        } else {
            startUp()
        }
    }

    override fun onStop() {
        compositeJob.cancel()
        super.onStop()
    }

    private fun startUp() {
        analytics.send(HitBuilders.EventBuilder()
                .setCategory(Analytics.CATEGORY_APP)
                .setAction(Analytics.ACTION_APP_LAUNCH)
                .setLabel(BuildConfig.BUILD_TYPE)
                .build())

        val dirString = "https://ldscdn.org/mobile/interview/directory"
        // Maintains downloaded static directories which have been inserted into the database.
        val staticDirectoryPrefsContainer = object: PrefsContainer("static_directory") {}
        val manager = staticDirectoryPrefsContainer.preferenceManager
        // Ensure that each individual in the directory is only added to the database once.
        if (!manager.contains(dirString) || !manager.getBoolean(dirString, false)) {
            fetchAndAddDirectory<StarWarsDirectory>(dirString) {
                manager.edit().putBoolean(dirString, true).apply()
            }
        }
        launch(UI, parent = compositeJob) {
            run(CommonPool) {
                // do some startup stuff
            }

            showStartActivity()
        }
    }

    private inline fun <reified T: OtherDirectory> fetchAndAddDirectory(
            urlString: String, noinline completion: () -> Unit) = launch(cc.network) {
        val httpUrl = HttpUrl.parse(urlString)!!
        val request = Request.Builder()
                .url(httpUrl)
                .get()
                .build()
        val call = standardClient.newCall(request)
        call.enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json = response?.body()?.string()
                if (json != null) {
                    val otherDirectory = gson.fromJson(json, T::class.java)
                    val individuals = otherDirectory.getIndividuals()
                    addIndividuals(individuals, completion)
                }
            }
        })
    }

    private fun addIndividuals(individuals: Array<Individual>, completion: () -> Unit) = launch(cc.commonPool) {
        individuals.forEach { individual: Individual -> run {
            // Ignore ID since it will be auto-generated.
            individual.id = 0L
            individualDao.insert(individual)
        } }

        completion()
    }

    private fun showStartActivity() {
        val intent = Intent(application, DirectoryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.nothing) // no animation
    }

    private fun devPauseStartup() {
        MaterialDialog.Builder(this)
                .content("Paused for debugger attachment")
                .positiveText("OK")
                .onPositive { _, _ -> startUp() }
                .show()
    }
}
