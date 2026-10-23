package com.carrental.model;

public class Customer {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String drivingLicenceNumber;

    public Customer() {
    }

    public Customer(String id,
                    String name,
                    String email,
                    String phone,
                    String drivingLicenceNumber) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.drivingLicenceNumber = drivingLicenceNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDrivingLicenceNumber() {
        return drivingLicenceNumber;
    }

    public void setDrivingLicenceNumber(String drivingLicenceNumber) {
        this.drivingLicenceNumber = drivingLicenceNumber;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", drivingLicenceNumber='" + drivingLicenceNumber + '\'' +
                '}';
    }
}
