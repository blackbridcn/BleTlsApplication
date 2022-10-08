package org.tls.protocol;

import org.bouncycastle.tls.ECCurveType;
import org.bouncycastle.tls.NamedGroup;

/**
 * File: EccExchangeKey.java
 * Author: yuzhuzhang
 * Create: 2022/1/11 10:10 PM
 * <p>
 * -----------------------------------------------------------------
 * Description:
 * <p>
 * <p>
 * -----------------------------------------------------------------
 */
public class EccExchangeKey implements TlsProtoBodyData {

    public static EccExchangeKey init(byte[] values) {
        EccExchangeKey eccExchangeKey = new EccExchangeKey(values);
        return eccExchangeKey;
    }

    public EccExchangeKey(byte[] values) {
        processECDHEServerKeyChange(values);
    }


    private short curveType;
    private int namedGroup;

    private void processECDHEServerKeyChange(byte[] values) {
        this.curveType = readUint8(values, 9);
        if (curveType == ECCurveType.named_curve) {
            int namedGroup = readUint16(values, 10);
            if (NamedGroup.refersToAnECDHCurve(namedGroup)) {
                this.namedGroup=namedGroup;
            }
        } else {
            //throw new TlsFatalAlert(AlertDescription.handshake_failure);
        }
    }

    @Override
    public String bodyToString() {
        return null;
    }

    public static short readUint8(byte[] buf, int offset) {
        return (short) (buf[offset] & 0xff);
    }

    public static int readUint16(byte[] buf, int offset) {
        int n = (buf[offset] & 0xff) << 8;
        n |= (buf[++offset] & 0xff);
        return n;
    }


}
