/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dasburo.spring.cache.dynamo.helper;

import java.io.Serializable;

/**
 * Simple Serializable Test Class representing a Company.
 *
 * @author Georg Zimmermann
 */
public class Company implements Serializable {
  private static final long serialVersionUID = 1L;

  private String name;
  private String industry;

  private Integer established;
  private Address address;

  public Company() {}

  public Company(String name, String industry, int established) {
    this(name, industry, established, null);
  }

  public Company(String name, String industry, int established, Address address) {
    super();
    this.name = name;
    this.industry = industry;
    this.established = established;
    this.address = address;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIndustry() {
    return industry;
  }

  public void setIndustry(String industry) {
    this.industry = industry;
  }

  public int getEstablished() {
    return established;
  }

  public void setEstablished(int established) {
    this.established = established;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((established == null) ? 0 : established.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((industry == null) ? 0 : industry.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof Company))
      return false;
    Company other = (Company) obj;
    if (address == null) {
      if (other.address != null)
        return false;
    } else if (!address.equals(other.address))
      return false;
    if (established == null) {
      if (other.established != null)
        return false;
    } else if (!established.equals(other.established))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (industry == null) {
      return other.industry == null;
    } else return industry.equals(other.industry);
  }
}