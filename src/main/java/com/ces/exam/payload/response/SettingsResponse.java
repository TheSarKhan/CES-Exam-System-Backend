package com.ces.exam.payload.response;

public class SettingsResponse {
    private String orgName;
    private String supportEmail;
    private int defaultPassMark;
    private int defaultDurationMinutes;
    private int defaultLinkValidityDays;
    private boolean proctoringEnabled;
    private boolean shuffleQuestions;
    private boolean shuffleOptions;
    private boolean showResultToCandidate;
    private int tabSwitchLimit;

    public SettingsResponse(String orgName, String supportEmail, int defaultPassMark,
                            int defaultDurationMinutes, int defaultLinkValidityDays, boolean proctoringEnabled,
                            boolean shuffleQuestions, boolean shuffleOptions, boolean showResultToCandidate,
                            int tabSwitchLimit) {
        this.orgName = orgName;
        this.supportEmail = supportEmail;
        this.defaultPassMark = defaultPassMark;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.defaultLinkValidityDays = defaultLinkValidityDays;
        this.proctoringEnabled = proctoringEnabled;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleOptions = shuffleOptions;
        this.showResultToCandidate = showResultToCandidate;
        this.tabSwitchLimit = tabSwitchLimit;
    }

    public String getOrgName() { return orgName; }
    public String getSupportEmail() { return supportEmail; }
    public int getDefaultPassMark() { return defaultPassMark; }
    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }
    public int getDefaultLinkValidityDays() { return defaultLinkValidityDays; }
    public boolean isProctoringEnabled() { return proctoringEnabled; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public boolean isShuffleOptions() { return shuffleOptions; }
    public boolean isShowResultToCandidate() { return showResultToCandidate; }
    public int getTabSwitchLimit() { return tabSwitchLimit; }
}
