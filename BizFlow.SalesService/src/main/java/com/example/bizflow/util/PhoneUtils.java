package com.example.bizflow.util;

public final class PhoneUtils {

    private PhoneUtils() {}

    public static String normalize(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("\\D", "");
    }
}
