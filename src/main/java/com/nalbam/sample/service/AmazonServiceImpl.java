package com.nalbam.sample.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AmazonServiceImpl implements AmazonService {

    @Autowired
    private AmazonS3 amazonS3;

    public List<String> listBuckets() {
        List<Bucket> buckets = this.amazonS3.listBuckets();

        List<String> results = new ArrayList<>();

        for (Bucket b : buckets) {
            results.add(b.getName());
        }

        return results;
    }

}
