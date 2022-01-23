package org.ble.exception

/**
 * Author: yuzzha
 * Date: 2019/4/25 15:13
 * Description: ${DESCRIPTION}
 * Remark:
 */
class BleException : RuntimeException {

    constructor() : super() {}
    constructor(message: String?) : super(message) {}

}