package com.sharjeelsoft.skillswapagenzskillexchanger.auth;

import java.util.List;
import java.util.Map;

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
    String fcmToken;
    int reportCount;
    
    // Complex fields mapping
    Map<String, Object> matchRequests;
    Map<String, Object> notifications;
    Map<String, Object> allConnections;
    Map<String, Object> recentChats;

    public String getUsername() { return username; }
    public void setUsername(Object username) { this.username = username == null ? null : String.valueOf(username); }
    
    public String getFullName() { return fullName; }
    public void setFullName(Object fullName) { this.fullName = fullName == null ? null : String.valueOf(fullName); }
    
    public String getEmail() { return email; }
    public void setEmail(Object email) { this.email = email == null ? null : String.valueOf(email); }
    
    public String getCountry() { return Country; }
    public void setCountry(Object country) { this.Country = country == null ? null : String.valueOf(country); }
    
    public String getPassword() { return password; }
    public void setPassword(Object password) { this.password = password == null ? null : String.valueOf(password); }
    
    public String getContact() { return contact; }
    public void setContact(Object contact) { this.contact = contact == null ? null : String.valueOf(contact); }
    
    public String getDateofbirth() { return dateofbirth; }
    public void setDateofbirth(Object dateofbirth) { this.dateofbirth = dateofbirth == null ? null : String.valueOf(dateofbirth); }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getSignupStage() { return signupStage; }
    public void setSignupStage(Object signupStage) { this.signupStage = signupStage == null ? null : String.valueOf(signupStage); }
    
    // Standard getters/setters for Firebase (supports both isSignedUp and signedUp keys)
    public boolean getIsSignedUp() { return isSignedUp; }
    public void setIsSignedUp(boolean signedUp) { isSignedUp = signedUp; }
    public boolean isSignedUp() { return isSignedUp; }
    public void setSignedUp(boolean signedUp) { isSignedUp = signedUp; }

    public boolean getIsCNICVerified() { return isCNICVerified; }
    public void setIsCNICVerified(boolean CNICVerified) { isCNICVerified = CNICVerified; }
    public boolean isCnicverified() { return isCNICVerified; }
    public void setCnicverified(boolean cnicverified) { isCNICVerified = cnicverified; }

    public boolean getIsDataUpdated() { return isDataUpdated; }
    public void setIsDataUpdated(boolean dataUpdated) { isDataUpdated = dataUpdated; }
    public boolean isDataUpdated() { return isDataUpdated; }
    public void setDataUpdated(boolean dataUpdated) { isDataUpdated = dataUpdated; }

    public boolean getIsSkillsTested() { return isSkillsTested; }
    public void setIsSkillsTested(boolean skillsTested) { isSkillsTested = skillsTested; }
    public boolean isSkillsTested() { return isSkillsTested; }
    public void setSkillsTested(boolean skillsTested) { isSkillsTested = skillsTested; }

    public boolean getIsAccountSet() { return isAccountSet; }
    public void setIsAccountSet(boolean accountSet) { isAccountSet = accountSet; }
    public boolean isAccountSet() { return isAccountSet; }
    public void setAccountSet(boolean accountSet) { isAccountSet = accountSet; }

    public String getGender() { return gender; }
    public void setGender(Object gender) { this.gender = gender == null ? null : String.valueOf(gender); }
    
    public List<String> getTeachingSkills() { return teachingSkills; }
    public void setTeachingSkills(List<String> teachingSkills) { this.teachingSkills = teachingSkills; }
    
    public List<String> getLearningInterests() { return learningInterests; }
    public void setLearningInterests(List<String> learningInterests) { this.learningInterests = learningInterests; }
    
    public String getEducation() { return education; }
    public void setEducation(Object education) { this.education = education == null ? null : String.valueOf(education); }
    
    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(Object currentJob) { this.currentJob = currentJob == null ? null : String.valueOf(currentJob); }
    
    public String getExperience() { return experience; }
    public void setExperience(Object experience) { this.experience = experience == null ? null : String.valueOf(experience); }

    public List<String> getPassedSkills() { return passedSkills; }
    public void setPassedSkills(List<String> passedSkills) { this.passedSkills = passedSkills; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(Object profileImageUrl) { this.profileImageUrl = profileImageUrl == null ? null : String.valueOf(profileImageUrl); }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(Object fcmToken) { this.fcmToken = fcmToken == null ? null : String.valueOf(fcmToken); }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public Map<String, Object> getMatchRequests() { return matchRequests; }
    public void setMatchRequests(Map<String, Object> matchRequests) { this.matchRequests = matchRequests; }

    public Map<String, Object> getNotifications() { return notifications; }
    public void setNotifications(Map<String, Object> notifications) { this.notifications = notifications; }
    public Map<String, Object> getAllConnections() { return allConnections; }
    public void setAllConnections(Map<String, Object> allConnections) { this.allConnections = allConnections; }

    public Map<String, Object> getRecentChats() { return recentChats; }
    public void setRecentChats(Map<String, Object> recentChats) { this.recentChats = recentChats; }

    public HelperClass(String username, String fullName, String email, String password, String contact, String dateofbirth, String country) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.Country = country;
        this.password = password;
        this.contact = contact;
        this.dateofbirth = dateofbirth;
        this.verified = false;
        this.signupStage = "CNIC_PENDING";
        this.isSignedUp = true;
        this.isCNICVerified = false;
        this.isDataUpdated = false;
        this.isSkillsTested = false;
        this.isAccountSet = false;
    }

    public HelperClass() {}
}
