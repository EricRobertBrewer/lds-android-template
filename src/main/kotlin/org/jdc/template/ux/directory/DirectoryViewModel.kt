package org.jdc.template.ux.directory

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.jdc.template.datasource.database.main.individual.IndividualDao
import org.jdc.template.livedata.SingleLiveEvent
import javax.inject.Inject

class DirectoryViewModel
@Inject constructor(private val individualDao: IndividualDao) : ViewModel() {

    val onNewIndividualEvent = SingleLiveEvent<Void>()

    val allDirectoryList: LiveData<List<IndividualDao.DirectoryListItem>>
    val directoryList: MutableLiveData<List<IndividualDao.DirectoryListItem>> = MutableLiveData()

    init {
        allDirectoryList = loadDirectoryList()
    }

    private fun loadDirectoryList(): LiveData<List<IndividualDao.DirectoryListItem>> {
        return individualDao.findAllDirectoryListItemsLiveData()
    }

    fun addIndividual() {
        onNewIndividualEvent.call()
    }
}