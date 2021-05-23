package com.amegane3231.qrshare.data

sealed class StateURL {
    object invalidURL : StateURL()
    object failureConnectURL : StateURL()
    object successConnectURL : StateURL()
}
