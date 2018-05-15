package com.nalbam.sample.controller;

import com.nalbam.sample.util.PackageUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<p>OK</p>";
    }

    @GetMapping("/package")
    public Map<String, String> health() {
        return PackageUtil.getData(this.getClass());
    }

}
