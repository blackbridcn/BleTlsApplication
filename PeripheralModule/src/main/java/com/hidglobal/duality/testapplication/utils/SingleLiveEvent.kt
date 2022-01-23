package com.hidglobal.duality.testapplication.utils

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * File: SingleLiveEvent.java
 * Author: yuzhuzhang
 * Create: 2022/1/16 5:32 PM
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 * -----------------------------------------------------------------
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {


    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }


}