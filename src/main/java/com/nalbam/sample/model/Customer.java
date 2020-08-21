package com.nalbam.sample.model;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Customer {

  @Id
  int id;
  String name;
  String email;

  @Column(name = "created_date")
  Date date;

  public Customer(int id, String name, String email, Date date) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.date = date;
  }

  @Override
  public String toString() {
    return String.format("Customer[id=%d, name='%s', email='%s', date='%s']", id, name, email, date);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

}
