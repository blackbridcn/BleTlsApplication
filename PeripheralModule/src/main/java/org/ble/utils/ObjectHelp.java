package org.ble.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

public class ObjectHelp {

    @NonNull
    public static <T> T checkNotNull(@Nullable T arg) {
        return checkNotNull(arg, "Argument must not be null");
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T arg, @NonNull String message) {
        nullPointerException(arg == null).throwMessage(message);
        return arg;
    }

    @NonNull
    public static String checkNotEmpty(@Nullable String string, String message) {
        illegalArgumentException(TextUtils.isEmpty(string)).throwMessage(message + "Must not be null or empty");
        return string;
    }

    @NonNull
    public static String checkNotEmpty(@Nullable String string) {
        return checkNotEmpty(string, "");
    }


    public static boolean checkNotNullEmpty(String... args) {
        boolean result = true;
        int length = args.length;
        for (int i = 0; i < length; i++) {
            if (null == args[i] || args[i].length() == 0) {
                result = false;
                break;
            }
        }
        return result;
    }


    @NonNull
    public static <T extends Collection<Y>, Y> T checkNotEmpty(@NonNull T collection) {
        illegalArgumentException(collection.isEmpty()).throwMessage("Must not be empty.");
        return collection;
    }

    public static ThrowExceptionFunction runtimeException(boolean b) {
        return (errorMessage) -> {
            if (b) {
                throw new RuntimeException(errorMessage);
            }
        };
    }

    public static ThrowExceptionFunction nullPointerException(boolean b) {
        return (errorMessage) -> {
            if (b) {
                throw new NullPointerException(errorMessage);
            }
        };
    }

    public static ThrowExceptionFunction illegalArgumentException(boolean b) {
        return (errorMessage) -> {
            if (b) {
                throw new IllegalArgumentException(errorMessage);
            }
        };
    }

}
