package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class PackageUtilTests {

    @Test
    public void testPackageDate() {
        log.debug("## PackageUtilTests ##");

        final Map<String, String> data = PackageUtil.getData(this.getClass());

        assertThat(data).isNotNull();

        log.info("testPackageDate : [{}] [{}]", data.get("artifactId"), data.get("version"));
    }

}
