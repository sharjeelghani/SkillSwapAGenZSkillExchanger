package com.sharjeelsoft.skillswapagenzskillexchanger.models;

public class MatchUiModel {

    private final String name;
    private final String primarySkill;
    private final String secondarySkill;
    private final int photoRes; // drawable resource id

    public MatchUiModel(String name,
                        String primarySkill,
                        String secondarySkill,
                        int photoRes) {
        this.name = name;
        this.primarySkill = primarySkill;
        this.secondarySkill = secondarySkill;
        this.photoRes = photoRes;
    }

    public String getName() {
        return name;
    }

    public String getPrimarySkill() {
        return primarySkill;
    }

    public String getSecondarySkill() {
        return secondarySkill;
    }

    public int getPhotoRes() {
        return photoRes;
    }
}
