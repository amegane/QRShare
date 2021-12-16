package com.amegane3231.qrshare.data

fun createUploadedQRCodeData(uid: String, qrCode: QRCode, tags: List<String>, date: String) = hashMapOf(
    "name" to qrCode.name,
    "uid" to uid,
    "url" to qrCode.url,
    "tags" to tags,
    "date" to date
)