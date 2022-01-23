package com.hidglobal.duality.testapplication.mvi


/**
 * File: SampleStates.java
 * Author: yuzhuzhang
 * Create: 2022/1/16 11:51 AM
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 * -----------------------------------------------------------------
 */
sealed class SampleEvent {
    data class ShowSnackbar(var message: String) : SampleEvent()

    data class ShowToast(val message: String) : SampleEvent()

}

sealed class SampleAction {

    data class ItemClick(val item: SampleMviData) : SampleAction()

    object FabClick : SampleAction()

    object OnSwipeRefresh : SampleAction()

    object FetchNews : SampleAction()

}

data class SampleViewStates(
    val list: List<SampleMviData> = emptyList(),
    val fetchStatus: FetchStatus = FetchStatus.NotFetched,
)


sealed class FetchStatus {
    object Fetching : FetchStatus()
    object Fetched : FetchStatus()
    object NotFetched : FetchStatus()
}