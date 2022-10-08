package org.utlis;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: yuzzha
 * Date: 2019-11-14 17:08
 * Description:
 * Remark:
 */
public class StringUtils {
    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    private final static Pattern phone = Pattern
            .compile("(13\\d|14[57]|15[^4,\\D]|17[678]|18\\d)\\d{8}|170[059]\\d{7}");

    /**
     * 判断给定字符串是否空白串 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return cs != null && cs.length() > 0;
    }

    public static boolean isNotEmpty(CharSequence... sequences) {
        for (CharSequence mCharSequence : sequences)
            if (mCharSequence == null || mCharSequence.length() == 0) {
                return false;
            }
        return true;
    }

    /**
     * 设置为xxx xxxx xxxx 格式手机号格式显示
     *
     * @return
     */
    public static String setPhoneFormat(String phone) {
        StringBuilder sb = new StringBuilder();
        sb.append(phone.substring(0, 3)).append(" ").append(phone.substring(3, 7)).append(" ").append(phone.substring(7, 11));
        return sb.toString();
    }

    // 判断一个字符串是否含有数字
    public static boolean HasDigit(String content) {
        boolean flag = false;
        Pattern p = Pattern.compile(".*\\d+.*");
        Matcher m = p.matcher(content);
        if (m.matches()) {
            flag = true;
        }
        return flag;
    }

    public static boolean hasDigitChar(String content) {
        boolean isDigit = false;//定义一个boolean值，用来表示是否包含数字
        boolean isLetter = false;//定义一个boolean值，用来表示是否包含字母
        for (int i = 0; i < content.length(); i++) { //循环遍历字符串
            if (Character.isDigit(content.charAt(i))) {     //用char包装类中的判断数字的方法判断每一个字符
                isDigit = true;
            }
            if (Character.isLetter(content.charAt(i))) {   //用char包装类中的判断字母的方法判断每一个字符
                isLetter = true;
            }
        }
        return isDigit && isLetter;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }


    public static boolean isSpace(String string) {
        if (string == null || string.contains(" ")) {
            return true;
        }
        return false;
    }

    public static String getMaskValue(String content) {
        if (isEmpty(content)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(3);
        int len = content.length();
        if (len == 8) {
            sb.append(content.substring(0, 2)).append("****").append(content.substring(6, len));
            // String maskNumber = mobile.substring(0,3)+"****"+mobile.substring(7,mobile.length());
            return sb.toString();
        } else if (len == 11) {
            sb.append(content.substring(0, 3)).append("****").append(content.substring(8, len));
            return sb.toString();
        }
        int begin = 2;
        int end = 6;
        if (!TextUtils.isEmpty(content)) {

            if (begin >= content.length() || begin < 0) {
                return content;
            }
            if (end >= content.length() || end < 0) {
                return content;
            }
            if (begin >= end) {
                return content;
            }
            String starStr = "";
            for (int i = begin; i < end; i++) {
                starStr = starStr + "*";
            }
            return content.substring(0, begin) + starStr + content.substring(end, content.length());
            // String maskNumber = mobile.substring(0,3)+"****"+mobile.substring(7,mobile.length());
        }
        return content;
    }


    public static boolean verifyAllParamsNotNull(String... objects) {
        for (String object : objects) {
            if (StringUtils.isEmpty(object)) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyAllParamsNull(String... objects) {
        for (String object : objects) {
            if (!StringUtils.isEmpty(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
         */
        mobiles = mobiles.replace(" ", "");
        String telRegex = "[1][345678]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (null == mobiles || TextUtils.isEmpty(mobiles) || mobiles.length() != 11)
            return false;
        else
            return mobiles.matches(telRegex);
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     */
    public static boolean isEmail(CharSequence email) {
        return !isEmpty(email) && emailer.matcher(email).matches();
    }

    /**
     * 判断是不是一个合法的手机号码
     */
    public static boolean isPhone(CharSequence phoneNum) {
        return !isEmpty(phoneNum);
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    /**
     * 对象转整
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * String转long
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * String转double
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static double toDouble(String obj) {
        try {
            return Double.parseDouble(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0D;
    }

    /**
     * 字符串转布尔
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断一个字符串是不是数字
     */
    public static boolean isNumber(CharSequence str) {
        try {
            Integer.parseInt(str.toString());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * byte[]数组转换为16进制的字符串。
     *
     * @param data 要转换的字节数组。
     * @return 转换后的结果。
     */
    public static final String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            int v = b & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.getDefault());
    }

    /**
     * 16进制表示的字符串转换为字节数组。
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] d = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节
            d[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return d;
    }

    public static String[] substringsBetween(String str, String open, String close) {
        if (str == null || isEmpty(open) || isEmpty(close)) {
            return null;
        }
        int strLen = str.length();
        if (strLen == 0) {
            return null;
        }
        int closeLen = close.length();
        int openLen = open.length();
        List<String> list = new ArrayList<>();
        int pos = 0;
        while (pos < (strLen - closeLen)) {
            int start = str.indexOf(open, pos);

            if (start < 0) {
                break;
            }
            start += openLen;
            int end = str.indexOf(close, start);
            if (end < 0) {
                break;
            }
            list.add(str.substring(start, end));
            pos = end + closeLen;
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[list.size()]);
    }


    public static String randomID() {
        String str = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = new Date();
            Date date2 = formatter.parse("1900-01-01");
            long i = date1.getTime() - date2.getTime();
            str = String.valueOf(i);
        } catch (Exception e) {
            str = java.util.UUID.randomUUID().toString();
        }
        return str;
    }

    /**
     * 中文占两个字符，计算字符串长度（UTF-8下 中文默认是1)
     */
    public static int getLength(String s) {
        int length = 0;
        String chinese = "[\u4e00-\u9fa5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < s.length(); i++) {
            // 获取一个字符
            String temp = s.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
                // 中文字符长度为2
                length += 2;
            } else {
                // 其他字符长度为1
                length += 1;
            }
        }
        // 进位取整
        return length;
    }

    public static int indext(String src, String targ, int fromIndex) {
        return indextof(src.toCharArray(), src.length(), 0, targ.toCharArray(), targ.length(), 0, fromIndex);
    }

    public static int indextof(char[] scr, int srcLen, int srcOffset, char[] target, int targetLen, int targOffset, int fromIndex) {
        if (fromIndex >= srcLen) { //判断目标长度和源长度关系
            return targetLen == 0 ? srcLen : -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetLen == 0) {
            return fromIndex;
        }
        int max = srcLen - targetLen + srcOffset;
        char frist = target[targOffset];
        for (int i = srcOffset + fromIndex; i < max; i++) {
            /*   Look for first character. */
            if (scr[i] != frist) {
                while (++i <= max && scr[i] != frist) ;
            }
            /*  Found first character, now look at the rest of v2  */
            if (i <= max) {
                int j = i + 1;
                int end = targetLen + j - 1;
                /* 只有找到第二个也相同 才会进一步循环找下一个否则会跳出循环 */
                for (int k = targOffset + 1; j < end && scr[j] == target[k]; j++, k++) ;
                if (j == end) {
                    return i - srcOffset;
                }
            }
        }
        return -1;
    }

    /**
     * 判断字符串是否为URL
     * @param urls 用户头像key
     * @return true:是URL、false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//比对
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();//判断是否匹配
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

}
