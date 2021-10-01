package com.springsource.petclinic.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Data Transfer Object of Owner Entity
 */
@XmlRootElement(name = "ownerInfo", namespace = "http://domain.petclinic.springsource.com/")
@XmlType(name = "OwnerInfo", propOrder = {"id", "firstName", "lastName", "address", "city", "telephone",
    "homePage", "email", "birthDay"}, namespace = "http://domain.petclinic.springsource.com/")
@XmlAccessorType(XmlAccessType.FIELD)
public class OwnerInfo {

  @XmlElement(name = "id")
  private final Long id;
  
  @XmlElement(name = "firstName")
  private final String firstName;
  
  @XmlElement(name = "lastName")
  private final String lastName;
  
  @XmlElement(name = "address")
  private final String address;
  
  @XmlElement(name = "city")
  private final String city;
  
  @XmlElement(name = "telephone")
  private final String telephone;
  
  @XmlElement(name = "homePage")
  private final String homePage;
  
  @XmlElement(name = "email")
  private final String email;
  
  @XmlElement(name = "birthDay")
  private final Date birthDay;
  
  public OwnerInfo(){
    this.id = null;
    this.firstName = null;
    this.lastName = null;
    this.address = null;
    this.city = null;
    this.telephone = null;
    this.homePage = null;
    this.email = null;
    this.birthDay = null;
  }

  public OwnerInfo(Long id, String firstName, String lastName, String address, String city, String telephone,
      String homePage, String email, Date birthDay) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.address = address;
    this.city = city;
    this.telephone = telephone;
    this.homePage = homePage;
    this.email = email;
    this.birthDay = birthDay;
  }

  public Long getId(){
    return this.id;
  }
  
  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public String getAddress() {
    return this.address;
  }

  public String getCity() {
    return this.city;
  }

  public String getTelephone() {
    return this.telephone;
  }

  public String getHomePage() {
    return this.homePage;
  }

  public String getEmail() {
    return this.email;
  }

  public Date getBirthDay() {
    return this.birthDay;
  }

}
