package com.nalbam.sample.controller;

import com.nalbam.sample.dao.CustomerRepository;
import com.nalbam.sample.model.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.sql.DataSource;
import java.util.List;

@Controller
public class CustomerController {

  @Autowired
  DataSource dataSource;

  @Autowired
  private CustomerRepository customerRepository;

  @RequestMapping(path = "/customer", method = RequestMethod.POST)
  public ResponseEntity<?> createCustomer(@RequestParam(required = true) String name,
      @RequestParam(required = true) String email) {

    customerRepository.addCustomer(name, email);

    return ResponseEntity.status(HttpStatus.CREATED).build();

  }

  @RequestMapping(path = "/customer", method = RequestMethod.GET)
  public ResponseEntity<List<Customer>> getCustomers() {

    return new ResponseEntity<>(customerRepository.findAll(), HttpStatus.OK);

  }

}
