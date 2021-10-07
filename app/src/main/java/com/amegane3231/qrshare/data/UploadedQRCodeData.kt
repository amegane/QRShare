package com.amegane3231.qrshare.data

data class UploadedQRCodeData(val uid: String, val qrCode: QRCode, val tags: List<String>)