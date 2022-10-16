package com.train.base.application

import android.app.Application
import android.content.Context

/**
 * Author: yuzzha
 * Date: 2021-12-09 12:10
 * Description:
 * Remark:
 */
open class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = this
    }


    companion object{
        @JvmStatic
        var context: BaseApplication? = null
            private set

        fun getInstance(): BaseApplication? {
            return context
        }
    }
}