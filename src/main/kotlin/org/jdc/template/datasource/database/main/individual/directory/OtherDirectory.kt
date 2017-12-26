package org.jdc.template.datasource.database.main.individual.directory

import org.jdc.template.datasource.database.main.individual.Individual

/**
 * A directory.
 */
interface OtherDirectory {
    fun getIndividuals(): Array<Individual>
}
