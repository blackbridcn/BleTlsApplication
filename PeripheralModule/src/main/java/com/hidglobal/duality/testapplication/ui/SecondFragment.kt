package com.hidglobal.duality.testapplication.ui


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hidglobal.duality.testapplication.R
import com.hidglobal.duality.testapplication.databinding.FragmentSecondBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.tls.ByteQueue
import org.bouncycastle.tls.TlsFatalAlert
import org.bouncycastle.tls.TlsUtils
import org.e.ble.utils.HexStrUtils
import org.tls12.TlsClientUtils
import org.tls12.TlsServerUtils
import org.utils.LogUtils
import java.io.IOException


class SecondFragment : Fragment() {

    private val TAG = SecondFragment::class.java.simpleName


    private var _binding: FragmentSecondBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    val backScope = CoroutineScope(Dispatchers.IO)

    val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        binding.textviewSecond.setOnClickListener {

            backScope.launch {
                var clientHello = TlsClientUtils.startHandshake()
                //LogUtils.e(TAG, "---------------->clientHello : " + HexStrUtils.byteArrayToHexString(clientHello))

               // var data = TlsProtocolData.initData(clientHello)
                //LogUtils.e(TAG,"${data}")

                var serverHello = TlsServerUtils.receiverClientHello(clientHello)

               LogUtils.e(TAG, "---------------->serverHello : " + HexStrUtils.byteArrayToHexString(serverHello))

                //client_key_exchange
                //change_cipher_spec

                var result = TlsClientUtils.continueHandshake(serverHello)

                LogUtils.e(TAG, "---------------->result : " + HexStrUtils.byteArrayToHexString(result))


                var set = TlsServerUtils.reciver(result)
                //LogUtils.e(TAG, "---------------->offerInput : " + HexStrUtils.byteArrayToHexString(set))

                var cleintInput = TlsClientUtils.offerInput(set)

               // LogUtils.e(TAG, "---------------->cleintInput : " + HexStrUtils.byteArrayToHexString(cleintInput))


                /*if (TlsClientUtils.handshakeFinised()) {
                    var wrapData =
                        TlsClientUtils.wrapData(HexStrUtils.hexToByteArray("7EABCDEF010203040506070809"))

                    var unwrapper = TlsServerUtils.unWrapData(wrapData)
                    LogUtils.e(
                        TAG,
                        "---------------->unwrapper : " + HexStrUtils.byteArrayToHexString(unwrapper)
                    )
                }*/


            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    val LENGTH_OFFSET = 3
    val FRAGMENT_OFFSET = 5
    var hasHead = true


    var lastHead: ByteArray? = null


    fun readBleTlsData(value: ByteArray) {
        var length = value.size
        if (length > 0) {
            var values = value
            lastHead?.let {
                length += it.size

                var newValue = ByteArray(length)
                System.arraycopy(lastHead, 0, newValue, 0, it.size)
                System.arraycopy(values, 0, newValue, it.size, values.size)
                values = newValue
                lastHead = null
            }
            var index = 0
            while (index <= length && (length - index > 0) && hasHead) {
                if (length - index >= FRAGMENT_OFFSET) {

                    var bodyLen =
                        TlsUtils.readUint16(values, index + LENGTH_OFFSET) + FRAGMENT_OFFSET
                    if (index + bodyLen <= length) {
                        readTlsPackage(values, index, bodyLen)
                        index += bodyLen
                    } else {
                        hasHead = false
                        lastHead = ByteArray(length - index)
                        System.arraycopy(values, index, lastHead, 0, length - index);
                    }
                } else {
                    hasHead = false
                    lastHead = ByteArray(length - index)
                    System.arraycopy(values, index, lastHead, 0, length - index);
                }
            }
            hasHead = true
            lastHead ?: let {
                try {
                    //var clientKeyExchange = TlsClientUtils.continueHandshake(handshakeQueue)

                    LogUtils.e(TAG, "-------------> ")
                } catch (e: IOException) {
                    LogUtils.e(TAG, "-------------> ${e.message}")
                    if (e is TlsFatalAlert) {
                        var alert = Integer.toHexString(e.alertDescription.toInt())
                        while (alert.length < 4) {
                            alert = "0${alert}"
                        }


                    } else {


                    }

                }
            }
        }
    }

    private val handshakeQueue = ByteQueue(0)

    open fun readTlsPackage(source: ByteArray, index: Int, bodyLen: Int) {


        var dest = ByteArray(bodyLen)

        System.arraycopy(source, index, dest, 0, bodyLen)



        LogUtils.e(TAG, "${HexStrUtils.byteArrayToHexString(dest)}")
    }


}


