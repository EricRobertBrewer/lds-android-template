package org.jdc.template.datasource.database.main.household

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface HouseholdDao {
    @Insert
    fun insert(vararg household: Household)

    @Insert
    fun update(household: Household)

    @Query("DELETE FROM household")
    fun deleteAll()
}
