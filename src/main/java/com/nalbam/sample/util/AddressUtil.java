package com.nalbam.sample.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class AddressUtil {

    public static String getAddress() {
        try {
            InetAddress ip;
            for (final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); ((Enumeration) e).hasMoreElements(); ) {
                for (final Enumeration<InetAddress> a = e.nextElement().getInetAddresses(); a.hasMoreElements(); ) {
                    ip = a.nextElement();
                    if (!ip.isLoopbackAddress() && !ip.isLinkLocalAddress() && ip.isSiteLocalAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
            ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
