package com.xaluoqone.publisher.utils

import com.google.gson.Gson

val gson by lazy {
    Gson()
}

fun Any.toJson(): String = gson.toJson(this)