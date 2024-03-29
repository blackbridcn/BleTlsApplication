package com.train.peripheral.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.train.peripheral.contetant.Constants

import com.train.peripheral.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class HomeFragment : Fragment() {

    private val TAG = HomeFragment::javaClass.name

    lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentFirstBinding? = null

    private val databind get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return databind.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        databind.btnValue.setOnClickListener {
            homeViewModel.startGattServer(
                Constants.BASE_SERVICE_UUID,
                Constants.BASE_RX_CHAR_UUID,
                Constants.BASE_TX_CHAR_UUID ,
                Constants.BASE_CCCD_DESC_UUID
            )
        }
        databind.btnTask.setOnClickListener {
            homeViewModel.sendMsg()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


