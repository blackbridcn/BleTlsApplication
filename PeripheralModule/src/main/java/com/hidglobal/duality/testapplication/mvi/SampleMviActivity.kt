package com.hidglobal.duality.testapplication.mvi

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.whenResumed
import com.hidglobal.duality.testapplication.databinding.ActivitySampleMviBinding
import com.hidglobal.duality.testapplication.utils.observeState

/**
 * https://juejin.cn/post/6844903640927322126
 *
 *
 * https://juejin.cn/post/7053985196906905636?utm_source=gold_browser_extension
 */

class SampleMviActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleMviBinding

    private val viewModel: SampleViewModel by viewModels()

    private val adapter by lazy {

        SampleAdapter(object : AdapterItemClickListener {
            override fun onItemClick(itemView: View, item: SampleMviData) {
                viewModel.dispatch(SampleAction.ItemClick(item))
            }

            override fun onItemViewClick(itemView: View) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleMviBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)


        binding.fab.setOnClickListener { view ->
            viewModel.dispatch(SampleAction.FabClick)

        }

        setupAdapter()



        initWidget()

    }

    fun initWidget() {
        viewModel.viewStates.run {
            observeState(this@SampleMviActivity, SampleViewStates::list) {
                adapter.submitList(it)
            }
            observeState(this@SampleMviActivity, SampleViewStates::fetchStatus) {
                when (it) {
                    is FetchStatus.Fetched -> {
                        viewModel.dispatch(SampleAction.FetchNews)
                    }
                    else -> {}
                }
            }
        }

        viewModel.viewEvents.observe(this, {
            renderViewEvent(it)
        })
    }


    fun setupAdapter() {
        binding.contentHome.rvTest.adapter = adapter

    }

    fun renderViewEvent(event: SampleEvent) {
        when (event) {
            is SampleEvent.ShowToast -> {
                Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
    }

}