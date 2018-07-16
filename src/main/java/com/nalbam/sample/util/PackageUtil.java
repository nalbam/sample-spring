package com.nalbam.sample.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PackageUtil {

    public static Map<String, Object> getData(final Class<?> c) {
        final Map<String, Object> data = new ConcurrentHashMap<>();
        final Package p = c.getPackage();
        if (p != null) {
            if (p.getImplementationTitle() != null) {
                data.put("artifactId", p.getImplementationTitle());
            } else if (p.getSpecificationTitle() != null) {
                data.put("artifactId", p.getSpecificationTitle());
            } else {
                data.put("artifactId", "");
            }
            if (p.getImplementationVersion() != null) {
                data.put("version", p.getImplementationVersion());
            } else if (p.getSpecificationVersion() != null) {
                data.put("version", p.getSpecificationVersion());
            } else {
                data.put("version", "");
            }
        }
        return data;
    }

}
