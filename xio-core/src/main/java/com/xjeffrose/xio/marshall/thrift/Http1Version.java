/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * <p>DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *
 * @generated
 */
package com.xjeffrose.xio.marshall.thrift;

public enum Http1Version implements org.apache.thrift.TEnum {
  HTTP_1_0(0),
  HTTP_1_1(1);

  private final int value;

  private Http1Version(int value) {
    this.value = value;
  }

  /** Get the integer value of this enum value, as defined in the Thrift IDL. */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   *
   * @return null if the value is not found.
   */
  public static Http1Version findByValue(int value) {
    switch (value) {
      case 0:
        return HTTP_1_0;
      case 1:
        return HTTP_1_1;
      default:
        return null;
    }
  }
}