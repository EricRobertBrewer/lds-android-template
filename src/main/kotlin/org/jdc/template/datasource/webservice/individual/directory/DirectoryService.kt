package org.jdc.template.datasource.webservice.individual.directory

import org.jdc.template.datasource.database.main.individual.directory.StarWarsDirectory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Fetches information about individuals.
 */
interface DirectoryService {

    @GET("/mobile/interview/{sub}")
    fun directory(@Path("sub") subUrl: String): Call<StarWarsDirectory>

    companion object {
        const val BASE_URL = "https://ldscdn.org"
    }
}
