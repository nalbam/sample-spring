package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class PackageUtilTests {

    @Test
    public void testPackageData() {
        final Map<String, Object> data = PackageUtil.getData(this.getClass());

        assertNotNull(data);
        assertNotNull(data.get("artifactId"));
        assertNotNull(data.get("version"));

        log.info("# [{}] [{}]", data.get("artifactId"), data.get("version"));
    }

}
