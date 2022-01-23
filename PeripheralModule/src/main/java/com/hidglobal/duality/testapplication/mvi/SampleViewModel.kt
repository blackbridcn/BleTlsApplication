package com.hidglobal.duality.testapplication.mvi

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.hidglobal.duality.testapplication.utils.*
import kotlinx.coroutines.launch

/**
 * File: SampleViewModel.java
 * Author: yuzhuzhang
 * Create: 2022/1/15 9:19 PM
 *
 *
 * -----------------------------------------------------------------
 * Description:
 *
 *
 *
 *
 * -----------------------------------------------------------------
 */
class SampleViewModel : ViewModel() {

    private val repository: Repository = Repository.getInstance()

    private val _viewStates: MutableLiveData<SampleViewStates>
    = MutableLiveData(SampleViewStates())

    val viewStates = _viewStates.asLiveData()

    private val _viewEvents: SingleLiveEvent<SampleEvent> = SingleLiveEvent() //一次性的事件，与页面状态分开管理

    val viewEvents = _viewEvents.asLiveData()


    fun dispatch(action: SampleAction) {
        when (action) {
            is SampleAction.ItemClick -> onItemClick(action.item)
            is SampleAction.FabClick -> onFabClickMethod()
            is SampleAction.FetchNews-> onNewData()
        }

    }

    fun onItemClick(itemData: SampleMviData) {

    }

    fun onFabClickMethod() {
        _viewEvents.setEvent(SampleEvent.ShowToast("Replace with your own action"))
    }

    fun onNewData(){
        _viewStates.setState {
            copy(fetchStatus = FetchStatus.Fetching)
        }
        viewModelScope.launch {

        }
        viewModelScope.launch {
            when (val result = repository.getMockApiResponse()) {
                is PageState.Error -> {
                    _viewStates.setState {
                        copy(fetchStatus = FetchStatus.Fetched)
                    }
                    //_viewEvents.setEvent(MainViewEvent.ShowToast(message = result.message))
                }
                is PageState.Success -> {
                   /* _viewStates.setState {
                        copy(fetchStatus = FetchStatus.Fetched, newsList = result.data)
                    }*/
                }
            }
        }

    }
}