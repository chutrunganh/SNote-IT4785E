package com.example.snote.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Entity(tableName = "notes")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val noteTitle: String = "",
    val noteDesc: String = ""
) : Parcelable

// Parcelable is a mechanism to convert complex objects into a simple format that can be transferred between activities/fragments.