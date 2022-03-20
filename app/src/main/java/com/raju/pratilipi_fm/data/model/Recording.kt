package com.raju.pratilipi_fm.data.model

import android.os.Parcelable
import android.os.Parcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recording(
    var path: String,
    var length: Long
) : Parcelable {

}