package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import java.util.List;

public class HelperClass {
    String username, fullName, email, Country, password, contact, dateofbirth;
    boolean verified;

    // New profile fields
    String gender;
    List<String> teachingSkills;
    List<String> learningInterests;
    String education;
    String currentJob;
    String experience;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(String dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getTeachingSkills() {
        return teachingSkills;
    }

    public void setTeachingSkills(List<String> teachingSkills) {
        this.teachingSkills = teachingSkills;
    }

    public List<String> getLearningInterests() {
        return learningInterests;
    }

    public void setLearningInterests(List<String> learningInterests) {
        this.learningInterests = learningInterests;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public HelperClass(String username, String fullName, String email, String password, String contact, String dateofbirth, String country) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.Country = country;
        this.password = password;
        this.contact = contact;
        this.dateofbirth = dateofbirth;
        this.verified = false;
    }

    public HelperClass(String username, String fullName, String email, String password, String contact, String dateofbirth, String country, boolean verified) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.Country = country;
        this.password = password;
        this.contact = contact;
        this.dateofbirth = dateofbirth;
        this.verified = verified;
    }

    public HelperClass() {
    }
}
