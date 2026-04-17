package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import java.util.List;

public class HelperClass {
    String username, fullName, email, Country, password, contact, dateofbirth;
    boolean verified;

    // Signup progress fields
    String signupStage; // SIGN_UP, CNIC_PENDING, DATA_PENDING, SKILLS_PENDING, ACCOUNT_PENDING, COMPLETED
    boolean isSignedUp;
    boolean isCNICVerified;
    boolean isDataUpdated;
    boolean isSkillsTested;
    boolean isAccountSet;

    // New profile fields
    String gender;
    List<String> teachingSkills;
    List<String> learningInterests;
    String education;
    String currentJob;
    String experience;
    String profileImageUrl;
    List<String> passedSkills;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCountry() { return Country; }
    public void setCountry(String country) { Country = country; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getDateofbirth() { return dateofbirth; }
    public void setDateofbirth(String dateofbirth) { this.dateofbirth = dateofbirth; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getSignupStage() { return signupStage; }
    public void setSignupStage(String signupStage) { this.signupStage = signupStage; }
    public boolean isSignedUp() { return isSignedUp; }
    public void setSignedUp(boolean signedUp) { isSignedUp = signedUp; }
    public boolean isCNICVerified() { return isCNICVerified; }
    public void setCNICVerified(boolean CNICVerified) { isCNICVerified = CNICVerified; }
    public boolean isDataUpdated() { return isDataUpdated; }
    public void setDataUpdated(boolean dataUpdated) { isDataUpdated = dataUpdated; }
    public boolean isSkillsTested() { return isSkillsTested; }
    public void setSkillsTested(boolean skillsTested) { isSkillsTested = skillsTested; }
    public boolean isAccountSet() { return isAccountSet; }
    public void setAccountSet(boolean accountSet) { isAccountSet = accountSet; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public List<String> getTeachingSkills() { return teachingSkills; }
    public void setTeachingSkills(List<String> teachingSkills) { this.teachingSkills = teachingSkills; }
    public List<String> getLearningInterests() { return learningInterests; }
    public void setLearningInterests(List<String> learningInterests) { this.learningInterests = learningInterests; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(String currentJob) { this.currentJob = currentJob; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public List<String> getPassedSkills() { return passedSkills; }
    public void setPassedSkills(List<String> passedSkills) { this.passedSkills = passedSkills; }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
        
        // Default stages
        this.signupStage = "CNIC_PENDING";
        this.isSignedUp = true;
        this.isCNICVerified = false;
        this.isDataUpdated = false;
        this.isSkillsTested = false;
        this.isAccountSet = false;
    }

    public HelperClass() {
    }
}
