package org.tls.protocol;

/**
 * File: ECCurveType.java
 * Author: yuzhuzhang
 * Create: 2022/1/9 9:42 PM
 * <p>
 * -----------------------------------------------------------------
 * Description:
 * <p>
 * <p>
 * -----------------------------------------------------------------
 */
public class ECCurveType {

    /**
     * Indicates the elliptic curve domain parameters are conveyed verbosely, and the
     * underlying finite field is a prime field.
     */
    public static final short explicit_prime = 1;

    /**
     * Indicates the elliptic curve domain parameters are conveyed verbosely, and the
     * underlying finite field is a characteristic-2 field.
     */
    public static final short explicit_char2 = 2;

    /**
     * Indicates that a named curve is used. This option SHOULD be used when applicable.
     */
    public static final short named_curve = 3;

    /*
     * Values 248 through 255 are reserved for private use.
     */
}
