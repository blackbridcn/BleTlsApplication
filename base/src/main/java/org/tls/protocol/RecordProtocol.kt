package org.tls.protocol

import androidx.lifecycle.ViewModel
import org.bouncycastle.tls.RecordFormat
import org.bouncycastle.tls.TlsUtils
import org.e.ble.utils.HexStrUtils
import org.tls.peer.server.TlsServer
import org.tls.peer.server.TlsServerUtils
import org.tls.utils.BytesQueue
import org.utlis.LogUtils

open abstract class RecordProtocol : ViewModel() {


    open val TAG: String = RecordProtocol::javaClass.name

    //偏移量 ,fragment每条tls命令 除去header头数据后的，数据段长度 length数据位的偏移量
    val FRAGMENT_OFFSET = 5

    var packHeader = false

    private var packageCacheQueue = BytesQueue(0)

    var cacheHeader: ByteArray? = null

    var packSize: Int = 0
    var packCursor: Int = 0

    var lastType = byteArrayOf(
        HandshakeType.client_hello,
        HandshakeType.server_hello_done,
        HandshakeType.hello_request,
        HandshakeType.finished
    )

    //接收ble 中所有消息
    fun parseMsg(msg: ByteArray) {
        var source = checkCacheHeader(msg)
        var length = source.size
        if (length > 0) {
            //是否已经接收到一包数据的头部数据(包括Tls长度部分)
            if (!packHeader) {
                //source 必须包括完整的 length（表示数据包长度的字节位）数据位，
                // TLV（Type-Length-Value）中至少要包含完整T（Type）V（Length）数据；
                if (length > FRAGMENT_OFFSET) {
                    packSize = TlsUtils.readUint16(
                        source,
                        RecordFormat.LENGTH_OFFSET
                    ) + RecordFormat.FRAGMENT_OFFSET
                    //LogUtils.e(TAG, "----->> parseMsg Handshake Type : ${HandshakeType.getName(source[RecordFormat.FRAGMENT_OFFSET])}, packSize:${packSize}")
                    packHeader = true
                    buildPackageValue(source, length)
                } else {
                    //不足Length数据位， 拼接到下一帧数据，继续
                    cacheHeader = ByteArray(length);
                    cacheHeader?.let { System.arraycopy(source, 0, it, 0, length) };
                }
            } else {
                buildPackageValue(source, length)
            }
        }
    }

    private fun buildPackageValue(source: ByteArray, length: Int) {
        if (packSize - packCursor > length) {
            //新的一帧数据中还是只是之前数据包中的一部分，还没有拼接完一包完整的数据包
            packCursor += length
            packageCacheQueue.addData(source, 0, length)
        } else if (packSize - packCursor == length) {
            //1.本次通讯中并不是最后一帧，而且其他包最后一帧刚好是接收到一帧数据
            //2.刚好是本次通讯中的最后一帧数据
            packCursor += length
            packageCacheQueue.addData(source, 0, length)

            var cacheLast = ByteArray(packSize);
            var size = packageCacheQueue.available();

            packageCacheQueue.read(cacheLast, 0, packSize, size - packSize);

            if (cacheLast[0] == ContentType.handshake && lastType.contains(cacheLast[FRAGMENT_OFFSET])) {
                //最后一帧
                var buff = packageCacheQueue.availableByteArray()
                LogUtils.e("----------->tls offerInput :${org.ble.utils.HexStrUtils.byteArrayToHexString(buff)}")
                packageCacheQueue.clear();

                var msg = TlsServerUtils.offerInput(buff)
                if (msg != null) {
                    outputHandshakeAvailable(msg)

                } else {

                }
            }
            resetState()

        } else {
            LogUtils.e("TAG", "buildPackageValue -------> 03, packCursor:${packCursor}")
            //新的一帧数据中，不仅包含之前的一包数据，而且还包含了下包数据中的部分数据
            var lastCount = packSize - packCursor
            packCursor += lastCount
            packageCacheQueue.addData(source, 0, lastCount)
            LogUtils.e(TAG, "------------->> buildPackageValue lastCount: ${lastCount}")
            if (length - lastCount >= RecordFormat.FRAGMENT_OFFSET) {
                LogUtils.e("TAG", "buildPackageValue -------> 03-1")
                // 如果剩下数据长度超过了 TLV中L所需的长度就必须需要直接递归处理，
                // 因为如果最后这个包数据刚好是V为空（TLV中的Value）的数据包package，
                // 即本次RTT的最后通知类数据包再没有数据传输，不递归则永远不会触发对这包数据处理了，不会触发下一步动作了
                var lastPack = ByteArray(length - lastCount);
                System.arraycopy(source, lastCount, lastPack, 0, length - lastCount)
                resetState()
                parseMsg(lastPack)

            } else {
                //剩下的数据（ lastCount～ length ） 新的数据包数据，直接交给下一帧去处理，Length数据位， 拼接到下一帧数据，继续解析
                cacheHeader = ByteArray(length - lastCount);
                System.arraycopy(source, lastCount, cacheHeader, 0, length - lastCount)
                resetState()
            }
        }
    }

    private fun resetState() {
        packHeader = false
        packSize = 0
        packCursor = 0
    }

    private fun checkCacheHeader(source: ByteArray): ByteArray {
        var value = source

        cacheHeader?.let {
            // 将上次不足Length数据位的部分 字节数据， 拼接到下一帧数据，作为一个新一帧，继续处理
            var newValue = ByteArray(it.size + source.size)
            //拼接数据array
            System.arraycopy(it, 0, newValue, 0, it.size)
            System.arraycopy(value, 0, newValue, it.size, value.size)
            value = newValue
            cacheHeader = null
        }
        return value
    }

    //tls handshake
    abstract fun outputHandshakeAvailable(tlsPackage: ByteArray)

    abstract fun onHandshakeComplete()
}