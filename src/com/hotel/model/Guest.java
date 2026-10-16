package com.hotel.model;

import java.io.Serializable;

/**
 * Represents guest / customer contact and identity information.
 */
public class Guest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fullName;
    private String email;
    private String phoneNumber;
    private String idProof;

    public Guest(String fullName, String email, String phoneNumber, String idProof) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.idProof = idProof;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public String toDataString() {
        return String.format("%s#%s#%s#%s",
                fullName.replace("#", " "),
                email.replace("#", " "),
                phoneNumber.replace("#", " "),
                idProof.replace("#", " ")
        );
    }

    public static Guest fromDataString(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        String[] parts = text.split("#");
        if (parts.length < 4) return null;
        return new Guest(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
    }

    @Override
    public String toString() {
        return String.format("%s (Phone: %s, Email: %s)", fullName, phoneNumber, email);
    }
}
