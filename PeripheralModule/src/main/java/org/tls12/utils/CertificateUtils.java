package org.tls12.utils;

import org.bouncycastle.asn1.x509.Certificate;

import java.util.Date;

/**
 * Author: yuzzha
 * Date: 2021/12/22 12:13
 * Description:
 * Remark:
 */
public class CertificateUtils {


    public static boolean checkValidity(Certificate mCertificate) {
        return checkValidity(mCertificate.getStartDate().getDate(), mCertificate.getEndDate().getDate());
    }

    public static boolean checkValidity(Date startDate, Date endDate) {
        Date now = new Date(System.currentTimeMillis());
        if (now.getTime() < endDate.getTime()) {
            return false;
        }
        if (now.getTime() > startDate.getTime()) {
            return false;
        }
        return true;
    }

}
