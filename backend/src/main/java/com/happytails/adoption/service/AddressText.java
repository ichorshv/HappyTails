package com.happytails.adoption.service;

/** Builds the text that is sent to the geocoding service for an applicant's address. */
final class AddressText {

  private AddressText() {}

  /**
   * The shelter works in Bulgaria, so the country is added to avoid matches abroad.
   *
   * @param street street and number
   * @param city city or village
   * @return text for the geocoding search
   */
  static String of(String street, String city) {
    return street + ", " + city + ", България";
  }
}
