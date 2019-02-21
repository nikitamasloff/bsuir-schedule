package com.nikitamaslov.bsuirschedule.data.local.database.dao

import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Update

interface BaseDao<T> {

    /*
     * Insert an object in the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(obj: T)

    /*
     * Insert an array of objects in the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg obj: T)

    /*
     * Update an object from the database.
     */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(obj: T)

    /*
    * Update an array of objects from the database
    */
    @Delete
    fun update(vararg obj: T)

    /*
     * Delete an object from the database
     */
    @Delete
    fun delete(obj: T)

    /*
    * Delete an array of objects from the database
    */
    @Delete
    fun delete(vararg obj: T)

}