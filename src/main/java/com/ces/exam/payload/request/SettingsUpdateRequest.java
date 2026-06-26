package com.ces.exam.payload.request;

import jakarta.validation.constraints.*;

public class SettingsUpdateRequest {

    @NotBlank(message = "Təşkilat adı boş ola bilməz")
    @Size(max = 120, message = "Təşkilat adı çox uzundur")
    private String orgName;

    @Size(max = 255)
    @Email(message = "Dəstək e-poçtu düzgün deyil")
    private String supportEmail;

    @Min(value = 0, message = "Keçid balı 0-100 aralığında olmalıdır")
    @Max(value = 100, message = "Keçid balı 0-100 aralığında olmalıdır")
    private int defaultPassMark;

    @Min(value = 1, message = "Müddət ən azı 1 dəqiqə olmalıdır")
    @Max(value = 1440, message = "Müddət 1440 dəqiqədən çox ola bilməz")
    private int defaultDurationMinutes;

    @Min(value = 1, message = "Etibarlılıq müddəti ən azı 1 gün olmalıdır")
    @Max(value = 365, message = "Etibarlılıq müddəti 365 gündən çox ola bilməz")
    private int defaultLinkValidityDays;

    private boolean proctoringEnabled;
    private boolean shuffleQuestions;
    private boolean shuffleOptions;
    private boolean showResultToCandidate;

    @Min(value = 0, message = "Tab limiti 0-20 aralığında olmalıdır")
    @Max(value = 20, message = "Tab limiti 0-20 aralığında olmalıdır")
    private int tabSwitchLimit;

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }
    public int getDefaultPassMark() { return defaultPassMark; }
    public void setDefaultPassMark(int defaultPassMark) { this.defaultPassMark = defaultPassMark; }
    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }
    public void setDefaultDurationMinutes(int defaultDurationMinutes) { this.defaultDurationMinutes = defaultDurationMinutes; }
    public int getDefaultLinkValidityDays() { return defaultLinkValidityDays; }
    public void setDefaultLinkValidityDays(int defaultLinkValidityDays) { this.defaultLinkValidityDays = defaultLinkValidityDays; }
    public boolean isProctoringEnabled() { return proctoringEnabled; }
    public void setProctoringEnabled(boolean proctoringEnabled) { this.proctoringEnabled = proctoringEnabled; }
    public boolean isShuffleQuestions() { return shuffleQuestions; }
    public void setShuffleQuestions(boolean shuffleQuestions) { this.shuffleQuestions = shuffleQuestions; }
    public boolean isShuffleOptions() { return shuffleOptions; }
    public void setShuffleOptions(boolean shuffleOptions) { this.shuffleOptions = shuffleOptions; }
    public boolean isShowResultToCandidate() { return showResultToCandidate; }
    public void setShowResultToCandidate(boolean showResultToCandidate) { this.showResultToCandidate = showResultToCandidate; }
    public int getTabSwitchLimit() { return tabSwitchLimit; }
    public void setTabSwitchLimit(int tabSwitchLimit) { this.tabSwitchLimit = tabSwitchLimit; }
}
