/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {
    //using AndroidViewModel because it gives us access to the application context variable
    //we need this variable because it gives us access to resources and styles

    //using coroutines because clicking the start button triggers database actions like
    //creating and updating a sleep night and we dont want to slow down our user interface
    //manage all the coroutines
    //this allows us to cancel all coroutines started by this view model when it is no
    // longer used and destroyed so we dont have coroutines with nowhere to return to
    /*
    NO LONGER NEEDED - private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }*/

    //scope determines which thread coroutines will run on
    //coroutines launched in the uiScope will be run on the main thread
    //NO LONGER NEEDED - private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    //its live data because we want to be able to observe it and change it
    private var tonight = MutableLiveData<SleepNight?>()

    //since nights will be a mutable list of nights, we can just assign the button to nights but it
    //would only show a reference so we need to do some SimpleDateFormatting
    val nights = database.getAllNights()

    val nightsString = nights.map { nights ->
        formatNights(
            nights,
            application.resources
        ) //.resources gives us access to the string resources
    }

    val startButtonVisible = tonight.map {
        null == it
    }
    val stopButtonVisible = tonight.map {
        null != it
    }
    val clearButtonVisible = nights.map {
        it?.isNotEmpty()
    }

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }

    private val _navigateToSleepDataQuality = MutableLiveData<Long>()
    val navigateToSleepDataQuality
        get() = _navigateToSleepDataQuality

    //snackbar is a UI thing and should happen in the fragment but deciding to
    // show it happens in the viewmodel

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    init {
        //we need the night set so we initialize it as soon as possible to work with it
        initializeTonight()
    }

    private fun initializeTonight() {
        //gonna use coroutines to get data from the database so we are 
        //not blocking the main ui while waiting for the result
        viewModelScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    //mark as suspend because we want to call it from inside the coroutines and not block
    //You can call suspend functions only from other suspend functions or by using a coroutine
    // builder such as launch to start a new coroutine.
    //suspend pauses the execution of the current coroutine, saving all local variables.
    private suspend fun getTonightFromDatabase(): SleepNight? {
        var night = database.getTonight() //returns the latest night from the database
        if (night?.endTimeMilli != night?.startTimeMilli) {
            night = null
        }
        return night
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val newNight = SleepNight() //captures the current time as the start time
            insert(newNight)
            tonight.value = getTonightFromDatabase() //set tonight to the new night
        }
    }

    private suspend fun insert(night: SleepNight) {
        database.insert(night)
    }

    private suspend fun update(night: SleepNight) {
        database.update(night)
    }

    private suspend fun clear() {
        database.clear()
    }

    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = tonight.value ?: return@launch //used for which fun among several
            // which the statement returns from, to return from launch, not the lambda
            // (not exactly sure what the is means)
            oldNight.endTimeMilli = System.currentTimeMillis()
            update(oldNight)
            _navigateToSleepQuality.value = oldNight
        }
    }

    fun onClear() {
        viewModelScope.launch {
            clear()
            //also clear tonight since it's no longer in the database
            tonight.value = null
            _showSnackbarEvent.value = true
        }
    }

    fun onSleepNightClicked(id: Long) {
        _navigateToSleepDataQuality.value = id
    }

    //tells the view model that you are done with the navigation
    fun onSleepDataQualityNavigated() {
        _navigateToSleepDataQuality.value = null
    }
    /*
    *
    Dispatchers.Main - Use this dispatcher to run a coroutine on the main Android thread. This
    * should be used only for interacting with the UI and performing quick work. Examples include
    * calling suspend functions, running Android UI framework operations, and updating
    * LiveData objects.
    Dispatchers.IO - This dispatcher is optimized to perform disk or network I/O outside of the
    * main thread. Examples include using the Room component, reading from or writing to files,
    * and running any network operations.
    Dispatchers.Default - This dispatcher is optimized to perform CPU-intensive work outside of
    * the main thread. Example use cases include sorting a list and parsing JSON.
 */
}

