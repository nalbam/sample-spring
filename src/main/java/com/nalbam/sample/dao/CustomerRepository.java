package com.nalbam.sample.dao;

import com.nalbam.sample.model.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class CustomerRepository {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<Customer> findAll() {

    List<Customer> result = jdbcTemplate.query("SELECT id, name, email, created_date FROM customer",
        (rs, rowNum) -> new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email"),
            rs.getDate("created_date")));

    return result;

  }

  public void addCustomer(String name, String email) {

    jdbcTemplate.update("INSERT INTO customer(name, email, created_date) VALUES (?,?,?)", name, email, new Date());

  }

}
