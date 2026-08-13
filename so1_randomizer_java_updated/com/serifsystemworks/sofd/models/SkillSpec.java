package com.serifsystemworks.sofd.models;

public class SkillSpec {
    private int id;
    private String categoryName;
    private String name;
    private int reqSp;
    private int maxLevel;
    private int icSuccessRate;

    public SkillSpec(int id, String categoryName, String name, int reqSp, int maxLevel, int icSuccessRate) {
        this.id = id;
        this.categoryName = categoryName;
        this.name = name;
        this.reqSp = reqSp;
        this.maxLevel = maxLevel;
        this.icSuccessRate = icSuccessRate;
    }

    public int getId() { return id; }
    public String getCategoryName() { return categoryName; }
    public String getName() { return name; }
    public int getReqSp() { return reqSp; }
    public void setReqSp(int reqSp) { this.reqSp = reqSp; }
    public int getMaxLevel() { return maxLevel; }
    public int getIcSuccessRate() { return icSuccessRate; }
    public void setIcSuccessRate(int icSuccessRate) { this.icSuccessRate = icSuccessRate; }
}