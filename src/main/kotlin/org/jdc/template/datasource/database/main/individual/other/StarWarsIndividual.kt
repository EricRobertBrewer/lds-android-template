package org.jdc.template.datasource.database.main.individual.other

import com.google.gson.annotations.SerializedName
import org.jdc.template.datasource.database.main.individual.Individual
import org.threeten.bp.LocalDate

/**
 * An individual fetched from the given REST directory.
 */
class StarWarsIndividual(
        var id: Long = 0,
        var firstName: String = "",
        var lastName: String = "",
        @SerializedName("birthdate") var birthDate: LocalDate? = null,
        var profilePicture: String = "",
        var forceSensitive: Boolean = false,
        var affiliation: String = AFFILIATION_UNKNOWN
) : OtherIndividual {

    companion object {
        val AFFILIATION_UNKNOWN = "UNKNOWN"
        val AFFILIATION_JEDI = "JEDI"
        val AFFILIATION_RESISTANCE = "RESISTANCE"
        val AFFILIATION_FIRST_ORDER = "FIRST_ORDER"
        val AFFILIATION_SITH = "SITH"
    }

    override fun toIndividual(): Individual {
        val individual = Individual()
        individual.id = id
        individual.firstName = firstName
        individual.lastName = lastName
        individual.birthDate = birthDate
        individual.profilePicture = profilePicture
        return individual
    }
}
