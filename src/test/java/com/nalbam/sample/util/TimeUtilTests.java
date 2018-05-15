package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.Assert.assertNotNull;

@Slf4j
public class TimeUtilTests {

    @Test
    public void test() {
        final Calendar c = Calendar.getInstance();
        c.set(2017, Calendar.APRIL, 28);

        final String ago = TimeUtil.ago(c);

        final SimpleDateFormat f = new SimpleDateFormat("yyyy년 MM월 dd일");

        log.debug("## ago : {} 은 {} 입니다.", f.format(c.getTime()), ago);

        assertNotNull(ago);
    }

}
