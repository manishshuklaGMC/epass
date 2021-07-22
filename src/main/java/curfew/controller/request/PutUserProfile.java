package curfew.controller.request;

import curfew.model.Name;
import curfew.model.ProofType;
import curfew.model.RoleType;

public class PutUserProfile {
  private Name name;
  private int age;
  private String profession;
  private String mobileNumber;
  private String govtIDNumber;
  private ProofType govtIDType;
  private String authToken;
  private String dob;
  private String gender;
  private String city;
  private RoleType roleType;

  public PutUserProfile(
      Name name,
      int age,
      String profession,
      String mobileNumber,
      String govtIDNumber,
      ProofType govtIDType,
      String authToken,
      String dob,
      String gender,
      String city,
      RoleType roleType) {
    this.name = name;
    this.age = age;
    this.profession = profession;
    this.mobileNumber = mobileNumber;
    this.govtIDNumber = govtIDNumber;
    this.govtIDType = govtIDType;
    this.authToken = authToken;
    this.dob = dob;
    this.gender = gender;
    this.city = city;
    this.roleType = (roleType == null) ? RoleType.ADMIN : RoleType.USER;
  }

  public Name getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public String getProfession() {
    return profession;
  }

  public String getMobileNumber() {
    return mobileNumber;
  }

  public String getGovtIDNumber() {
    return govtIDNumber;
  }

  public String getAuthToken() {
    return authToken;
  }

  public String getDob() {
    return dob;
  }

  public ProofType getGovtIDType() {
    return govtIDType;
  }

  public String getGender() {
    return gender;
  }

  public String getCity() {
    return city;
  }

  public RoleType getRoleType() {
    return roleType;
  }
}
