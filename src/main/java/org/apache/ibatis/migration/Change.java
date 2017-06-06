/**
 *    Copyright 2010-2017 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.migration;

import java.math.BigDecimal;

public class Change implements Comparable<Change>, Cloneable {

  private BigDecimal id;
  private String description;
  private String appliedTimestamp;
  private String filename;

  public Change() {
  }

  public Change(BigDecimal id) {
    this.id = id;
  }

  public Change(BigDecimal id, String appliedTimestamp, String description) {
    this.id = id;
    this.appliedTimestamp = appliedTimestamp;
    this.description = description;
  }

  public BigDecimal getId() {
    return id;
  }

  public void setId(BigDecimal id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAppliedTimestamp() {
    return appliedTimestamp;
  }

  public void setAppliedTimestamp(String appliedTimestamp) {
    this.appliedTimestamp = appliedTimestamp;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Override
  public String toString() {
    return id + " " + (appliedTimestamp == null ? "   ...pending...   " : appliedTimestamp) + " " + description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Change change = (Change) o;

    return (id.equals(change.getId()));
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public int compareTo(Change change) {
    return id.compareTo(change.getId());
  }

  @Override
  public Change clone() {
    try {
      return (Change) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }
  }
}
