package com.train.peripheral.utils

import androidx.lifecycle.*
import kotlin.reflect.KProperty1


/**
 * File: ExtensionMethods.java
 * Author: yuzhuzhang
 * Create: 2022/1/16 5:39 PM
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 * -----------------------------------------------------------------
 */
fun <T, A> LiveData<T>.observeState(
    lifecycleOwner: LifecycleOwner,
    prop1: KProperty1<T, A>,
    action: (A) -> Unit
) {
    this.map {
        StateTuple1(prop1.get(it))
    }.distinctUntilChanged().observe(lifecycleOwner) { (a) ->
        action.invoke(a)
    }
}

fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> {
    return this
}


fun <T> SingleLiveEvent<T>.setEvent(value: T) {
    this.value = value

}

fun <T> MutableLiveData<T>.setState(reducer: T.() -> T) {
    this.value = this.value?.reducer()
}

//LCE -> Loading/Content/Error
sealed class PageState<out T> {
    data class Success<T>(val data: T) : PageState<T>()
    data class Error<T>(val message: String) : PageState<T>() {
        constructor(t: Throwable) : this(t.message ?: "")
    }
}


internal data class StateTuple1<A>(val a: A)
internal data class StateTuple2<A, B>(val a: A, val b: B)
internal data class StateTuple3<A, B, C>(val a: A, val b: B, val c: C)