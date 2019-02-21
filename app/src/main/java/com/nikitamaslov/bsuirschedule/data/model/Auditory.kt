package com.nikitamaslov.bsuirschedule.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.nikitamaslov.bsuirschedule.data.remote.exclude.Exclude

@Entity(tableName = "auditory")
class Auditory {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int? = null

    @Exclude
    var number: String? = null

    @Exclude
    var pavilion: String? = null

}