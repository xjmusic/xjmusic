package io.outright.xj.core.model.account;

import io.outright.xj.core.app.exception.BusinessException;

public class Account {

  // Name
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.getName() == null || this.getName().length() == 0) {
      throw new BusinessException("Account name is required.");
    }
  }

  @Override
  public String toString() {
    return "{" +
      "name:" + this.name +
      "}";
  }

}
