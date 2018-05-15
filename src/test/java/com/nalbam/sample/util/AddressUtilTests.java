package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class AddressUtilTests {

    @Test
    public void testAddress() {

        final String ip = AddressUtil.getAddress();

        log.info("# address : {}", ip);

        assertNotNull(ip);
    }

}
