package com.nalbam.sample.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

    public static Long spent(final Date from) {
        return ((new Date()).getTime() - from.getTime()) / 1000;
    }

    public static String ago(final Date c) {
        final Date n = new Date();

        final Long nt = n.getTime() / 1000;
        final Long ct = c.getTime() / 1000;

        return ago(nt, ct);
    }

    public static String ago(final Calendar c) {
        final Calendar n = Calendar.getInstance();

        final long nt = n.getTimeInMillis() / 1000;
        final long ct = c.getTimeInMillis() / 1000;

        return ago(nt, ct);
    }

    private static String ago(final Long nt, final Long ct) {
        final long diff = nt - ct;

        if (ct > nt) {
            return "방금";
        } else {
            if (diff < 3) {
                return "방금";
            } else if (diff < 60) {
                return diff + "초 전";
            } else if (diff < 3600) {
                return diff / 60 + "분 전";
            } else if (diff < 3600 * 24) {
                return diff / 60 / 60 + "시간 전";
            } else if (diff < 3600 * 24 * 30) {
                final long diffDay = diff / 3600 / 24;
                if (diffDay == 1) {
                    return "하루 전";
                } else {
                    return diffDay + "일 전";
                }
            } else if (diff < 3600 * 24 * 365) {
                final long diffMonth = diff / 3600 / 24 / 30;
                if (diffMonth == 1) {
                    return "한달 전";
                } else {
                    return diffMonth + "개월 전";
                }
            } else {
                final long diffYear = diff / 3600 / 24 / 365;
                if (diffYear == 1) {
                    return "작년";
                } else {
                    return diffYear + "년 전";
                }
            }
        }
    }

}
