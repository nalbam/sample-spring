package com.nalbam.sample.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AmazonServiceImpl implements AmazonService {

    @Autowired
    private AmazonS3 amazonS3;

    public List<Map<String, Object>> listBuckets() {
        List<Bucket> buckets = this.amazonS3.listBuckets();

        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> map;

        for (Bucket b : buckets) {
            map = new HashMap<>();
            map.put("name", b.getName());

            results.add(map);
        }

        return results;
    }

}
