package com.ces.exam.payload.response;

public class PublicSettingsResponse {
    private String orgName;
    private String supportEmail;
    private boolean proctoringEnabled;
    private int tabSwitchLimit;

    public PublicSettingsResponse(String orgName, String supportEmail, boolean proctoringEnabled, int tabSwitchLimit) {
        this.orgName = orgName;
        this.supportEmail = supportEmail;
        this.proctoringEnabled = proctoringEnabled;
        this.tabSwitchLimit = tabSwitchLimit;
    }

    public String getOrgName() { return orgName; }
    public String getSupportEmail() { return supportEmail; }
    public boolean isProctoringEnabled() { return proctoringEnabled; }
    public int getTabSwitchLimit() { return tabSwitchLimit; }
}
