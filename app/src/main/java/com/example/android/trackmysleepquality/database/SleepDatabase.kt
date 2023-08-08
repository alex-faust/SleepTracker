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

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
//we only have 1 table, which is sleepnight. if we have more, add them to the array that is inside []
abstract class SleepDatabase : RoomDatabase() {

    abstract val sleepDatabaseDao: SleepDatabaseDao

    //allows clients to access the methods for creating or getting the database without
    // instantiating the class
    companion object {
        //INSTANCE will keep a ref to the database once we have one so we wont keep opening
        // references to the database, which can be expensive
        //@Volatile makes sure the value of instance is always up to date and the same to all
        // execution threads. Volatile variable values will never be cached and all -r and -w
        // will be done to and from the main memory. Changes made by one threads will be updated
        //immediately
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getInstance(context: Context): SleepDatabase {
            //multiple threads can potentially ask for a database instance at the same time
            //leaving us with 2 instead of 1. More likely in more complicated apps
            //THis means only 1 thread of execution can enter this block of code at a time
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext, SleepDatabase::class.java,
                        "sleep_history_database"
                    ).fallbackToDestructiveMigration() //for updating schemas/versions, how info will be migrated
                    //since its a simple app, we will just wipe data and rebuild instead
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}
