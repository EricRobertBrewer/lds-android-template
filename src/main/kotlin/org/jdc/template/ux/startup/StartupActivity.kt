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
import org.jdc.template.Analytics
import org.jdc.template.BuildConfig
import org.jdc.template.R
import org.jdc.template.datasource.database.main.individual.Individual
import org.jdc.template.datasource.database.main.individual.IndividualDao
import org.jdc.template.datasource.database.main.individual.directory.StarWarsDirectory
import org.jdc.template.datasource.webservice.individual.directory.DirectoryService
import org.jdc.template.inject.Injector
import org.jdc.template.prefs.base.PrefsContainer
import org.jdc.template.util.CoroutineContextProvider
import org.jdc.template.ux.directory.DirectoryActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class StartupActivity : Activity() {

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var cc: CoroutineContextProvider

    @Inject
    lateinit var individualDao: IndividualDao

    @Inject
    lateinit var directoryService: DirectoryService

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

        val subUrl = "directory"
        // Maintains downloaded static directories which have been inserted into the database.
        val staticDirectoryPrefsContainer = object: PrefsContainer("mobile_interview") {}
        val manager = staticDirectoryPrefsContainer.preferenceManager
        // Ensure that each individual in the directory is only added to the database once.
        if (!manager.contains(subUrl) || !manager.getBoolean(subUrl, false)) {
            fetchDirectory(subUrl) {
                it?.let {
                    val individuals = it.getIndividuals()
                    // Add individuals to the database.
                    addIndividuals(individuals)
                    manager.edit().putBoolean(subUrl, true).apply()
                }
            }
        }
        launch(UI, parent = compositeJob) {
            run(CommonPool) {
                // do some startup stuff
            }

            showStartActivity()
        }
    }

    private fun fetchDirectory(subUrl: String, completion: (StarWarsDirectory?) -> Unit) = launch(cc.network) {
        val call = directoryService.directory(subUrl)
        call.enqueue(object: Callback<StarWarsDirectory> {
            override fun onFailure(call: Call<StarWarsDirectory>, t: Throwable) {
                launch(CommonPool) {
                    completion(null)
                }
            }

            override fun onResponse(call: Call<StarWarsDirectory>, response: Response<StarWarsDirectory>) {
                launch(CommonPool) {
                    completion(response.body())
                }
            }
        })
    }

    private fun addIndividuals(individuals: Array<Individual>) {
        individuals.forEach { individual: Individual -> run {
            // Ignore ID since it will be auto-generated.
            individual.id = 0L
            individualDao.insert(individual)
        } }
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
