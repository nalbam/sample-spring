package com.nalbam.sample.domain;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Queue {

    private String key;

    private List<String> tokens;

    private Character type;

    private Integer delay; // push 의 예약 시간 + delay = 발송 시간

    private Date registered;
    private Date reserved;

    private Map<String, Object> Data;

    public String toJson() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }

}
