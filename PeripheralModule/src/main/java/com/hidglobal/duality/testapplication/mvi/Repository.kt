package com.hidglobal.duality.testapplication.mvi

import com.hidglobal.duality.testapplication.utils.PageState
import kotlinx.coroutines.delay


/**
 * File: Repository.java
 * Author: yuzhuzhang
 * Create: 2022/1/17 11:28 PM
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 * -----------------------------------------------------------------
 */
class Repository {


    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: Repository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Repository().also { instance = it }
            }
    }


    suspend fun getMockApiResponse(): PageState<List<SampleMviData>>? {
        /*val articlesApiResult = try {
            delay(2000)
            MockApi.create().getLatestNews()
        } catch (e: Exception) {
            return PageState.Error(e)
        }

        articlesApiResult.articles?.let { list ->
            return PageState.Success(data = list)
        } ?: run {
            return PageState.Error("Failed to get News")
        }*/
        return  null
    }


}