package com.amegane3231.qrshare.data

import com.google.firebase.storage.StorageReference

data class PageTaskResult(val items: List<StorageReference>, val pageToken: String?)
