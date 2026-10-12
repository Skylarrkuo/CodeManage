package com.example.entity;

/**
 * 班级信息（年级 / 专业 / 班级）
 */
public class Clazz {

    /** ID */
    private Integer id;
    /** 年级 */
    private String grade;
    /** 专业 */
    private String major;
    /** 班级 */
    private String clazz;
    /** 备注 */
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
