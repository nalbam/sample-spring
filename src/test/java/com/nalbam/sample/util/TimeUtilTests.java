package com.nalbam.sample.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class TimeUtilTests {

    private final SimpleDateFormat f = new SimpleDateFormat("yyyy년 MM월 dd일");

    @Test
    public void testDate() {
        final Date d = new Date();
        d.setTime(d.getTime() - 100000);

        final String ago = TimeUtil.ago(d);

        log.info("# ago : {} 은 {} 입니다.", f.format(d.getTime()), ago);

        assertNotNull(ago);
        assertEquals(ago, "1분 전");
    }

    @Test
    public void testCalendar() {
        final Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR) - 1, Calendar.JANUARY, 1);

        final String ago = TimeUtil.ago(c);

        log.info("# ago : {} 은 {} 입니다.", f.format(c.getTime()), ago);

        assertNotNull(ago);
        assertEquals(ago, "작년");
    }

}
