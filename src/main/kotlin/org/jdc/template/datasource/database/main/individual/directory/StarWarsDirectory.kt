package org.jdc.template.datasource.database.main.individual.directory

import org.jdc.template.datasource.database.main.individual.Individual
import org.jdc.template.datasource.database.main.individual.other.StarWarsIndividual

/**
 * Represents the root object of the REST directory.
 */
class StarWarsDirectory(
        var individuals: Array<StarWarsIndividual>
) : OtherDirectory {

    override fun getIndividuals(): Array<Individual> {
        return individuals.map { it.toIndividual() }.toTypedArray()
    }
}
