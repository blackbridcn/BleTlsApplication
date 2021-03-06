package org.utils;

import android.text.TextUtils;
import android.util.Log;

/**
 * Author: yuzzha
 * Date: 2019-06-26 11:41
 * Description: ${DESCRIPTION}
 * Remark:
 */
public class LogUtils {

    public static int LOG_LEVEL = 5;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final String SEPARATOR = ",";

    public static void setLogLevel(int logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static void v(String message) {
        if (LOG_LEVEL >= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.v(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void v(String tag, String message) {
        if (LOG_LEVEL >= VERBOSE) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.v(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void d(String message) {
        if (LOG_LEVEL >= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.d(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void d(String tag, String message) {
        if (LOG_LEVEL >= DEBUG) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.d(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void i(String message) {
        if (LOG_LEVEL >= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.i(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void i(String tag, String message) {
        if (LOG_LEVEL >= INFO) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.i(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void w(String message) {
        if (LOG_LEVEL >= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.w(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    public static void w(String tag, String message) {
        if (LOG_LEVEL >= WARN) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.w(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }


    public static void e( String message) {
        if (LOG_LEVEL >= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            String tag = getDefaultTag(stackTraceElement);
            Log.e(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }


    public static void e(String tag, String message) {
        if (LOG_LEVEL >= ERROR) {
            StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
            if (TextUtils.isEmpty(tag)) {
                tag = getDefaultTag(stackTraceElement);
            }
            Log.e(tag, getLogInfo(stackTraceElement) + "\n" + message);
        }
    }

    /**
     * ???????????????TAG??????.
     * ?????????MainActivity.java????????????????????????.
     * ???TAG???MainActivity
     */
    private static String getDefaultTag(StackTraceElement stackTraceElement) {
        String fileName = stackTraceElement.getFileName();
        String tag;
        if (fileName == null) {
            tag = "Unknown";
        } else {
            String stringArray[] = fileName.split("\\.");
            tag = stringArray[0];
        }
        return tag;
    }

    /**
     * ??????????????????????????????
     */
    private static String getLogInfo(StackTraceElement stackTraceElement) {
        StringBuilder logInfoStringBuilder = new StringBuilder();
        // ???????????????
        String threadName = Thread.currentThread().getName();
        // ????????????ID
        long threadID = Thread.currentThread().getId();
        // ???????????????.???xxx.java
        String fileName = stackTraceElement.getFileName();
        // ????????????.?????????+??????
        String className = stackTraceElement.getClassName();
        // ??????????????????
        String methodName = stackTraceElement.getMethodName();
        // ????????????????????????
        int lineNumber = stackTraceElement.getLineNumber();

        logInfoStringBuilder.append("[ ");
        logInfoStringBuilder.append("threadID=").append(threadID).append(SEPARATOR);
        logInfoStringBuilder.append("threadName=").append(threadName).append(SEPARATOR);
        logInfoStringBuilder.append("fileName=").append(fileName).append(SEPARATOR);
        logInfoStringBuilder.append("className=").append(className).append(SEPARATOR);
        logInfoStringBuilder.append("methodName=").append(methodName).append(SEPARATOR);
        logInfoStringBuilder.append("lineNumber=").append(lineNumber);
        logInfoStringBuilder.append(" ] ");
        return logInfoStringBuilder.toString();
    }
}
