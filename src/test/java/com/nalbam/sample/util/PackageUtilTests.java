package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class PackageUtilTests {

    @Test
    public void testPackageData() {
        log.debug("## PackageUtilTests ##");

        final Map<String, String> data = PackageUtil.getData(this.getClass());

        assertNotNull(data);
        assertNotNull(data.get("artifactId"));
        assertNotNull(data.get("version"));

        log.info("testPackageData : [{}] [{}]", data.get("artifactId"), data.get("version"));
    }

}
