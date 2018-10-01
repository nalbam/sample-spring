package com.nalbam.sample.service;

import com.amazonaws.services.s3.model.Bucket;

import java.util.List;

public interface AmazonService {

    List<Bucket> listBuckets();

}
