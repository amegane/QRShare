package com.amegane3231.qrshare.data

fun createUploadedQRCodeData(uid: String, qrCode: QRCode, tags: List<String>) = hashMapOf(
    "name" to qrCode.name,
    "uid" to uid,
    "url" to qrCode.url,
    "tags" to tags
)