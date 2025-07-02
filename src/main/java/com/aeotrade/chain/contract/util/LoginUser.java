package com.aeotrade.chain.contract.util;

import java.util.Objects;


public class LoginUser {

    private Long userId;

    private String username;

    private String avatar;

    private Long deptId;

    private String description;

    private String email;

    private String mobile;

    private Integer ssex;

    private Long staffId;

    private Integer status;

    private String theme;

    public LoginUser() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getSsex() {
        return ssex;
    }

    public void setSsex(Integer ssex) {
        this.ssex = ssex;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginUser loginUser = (LoginUser) o;
        return Objects.equals(userId, loginUser.userId) &&
                Objects.equals(username, loginUser.username) &&
                Objects.equals(avatar, loginUser.avatar) &&
                Objects.equals(deptId, loginUser.deptId) &&
                Objects.equals(description, loginUser.description) &&
                Objects.equals(email, loginUser.email) &&
                Objects.equals(mobile, loginUser.mobile) &&
                Objects.equals(ssex, loginUser.ssex) &&
                Objects.equals(staffId, loginUser.staffId) &&
                Objects.equals(status, loginUser.status) &&
                Objects.equals(theme, loginUser.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, avatar, deptId, description, email, mobile, ssex, staffId, status, theme);
    }

    @Override
    public String toString() {
        return "LoginUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", deptId=" + deptId +
                ", description='" + description + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", ssex=" + ssex +
                ", staffId=" + staffId +
                ", status=" + status +
                ", theme='" + theme + '\'' +
                '}';
    }
}
