package com.ces.exam.service;

import com.ces.exam.model.entity.AppSetting;
import com.ces.exam.payload.request.SettingsUpdateRequest;
import com.ces.exam.payload.response.PublicSettingsResponse;
import com.ces.exam.payload.response.SettingsResponse;
import com.ces.exam.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SettingsService {

    public static final String ORG_NAME = "org_name";
    public static final String SUPPORT_EMAIL = "support_email";
    public static final String DEFAULT_PASS_MARK = "default_pass_mark";
    public static final String DEFAULT_DURATION = "default_duration_minutes";
    public static final String DEFAULT_LINK_VALIDITY = "default_link_validity_days";
    public static final String PROCTORING_ENABLED = "proctoring_enabled";
    public static final String SHUFFLE_QUESTIONS = "shuffle_questions";
    public static final String SHUFFLE_OPTIONS = "shuffle_options";
    public static final String SHOW_RESULT_TO_CANDIDATE = "show_result_to_candidate";
    public static final String TAB_SWITCH_LIMIT = "tab_switch_limit";

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put(ORG_NAME, "CES Assessment");
        DEFAULTS.put(SUPPORT_EMAIL, "");
        DEFAULTS.put(DEFAULT_PASS_MARK, "60");
        DEFAULTS.put(DEFAULT_DURATION, "30");
        DEFAULTS.put(DEFAULT_LINK_VALIDITY, "7");
        DEFAULTS.put(PROCTORING_ENABLED, "true");
        DEFAULTS.put(SHUFFLE_QUESTIONS, "false");
        DEFAULTS.put(SHUFFLE_OPTIONS, "false");
        DEFAULTS.put(SHOW_RESULT_TO_CANDIDATE, "true");
        DEFAULTS.put(TAB_SWITCH_LIMIT, "3");
    }

    private final AppSettingRepository repository;

    public SettingsService(AppSettingRepository repository) {
        this.repository = repository;
    }

    private Map<String, String> loadAll() {
        Map<String, String> values = new HashMap<>(DEFAULTS);
        for (AppSetting s : repository.findAll()) {
            if (s.getValue() != null) values.put(s.getKey(), s.getValue());
        }
        return values;
    }

    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        Map<String, String> v = loadAll();
        return new SettingsResponse(
                v.get(ORG_NAME),
                v.get(SUPPORT_EMAIL),
                parseInt(v.get(DEFAULT_PASS_MARK), 60),
                parseInt(v.get(DEFAULT_DURATION), 30),
                parseInt(v.get(DEFAULT_LINK_VALIDITY), 7),
                parseBool(v.get(PROCTORING_ENABLED), true),
                parseBool(v.get(SHUFFLE_QUESTIONS), false),
                parseBool(v.get(SHUFFLE_OPTIONS), false),
                parseBool(v.get(SHOW_RESULT_TO_CANDIDATE), true),
                parseInt(v.get(TAB_SWITCH_LIMIT), 3));
    }

    @Transactional(readOnly = true)
    public PublicSettingsResponse getPublicSettings() {
        Map<String, String> v = loadAll();
        return new PublicSettingsResponse(
                v.get(ORG_NAME),
                v.get(SUPPORT_EMAIL),
                parseBool(v.get(PROCTORING_ENABLED), true),
                parseInt(v.get(TAB_SWITCH_LIMIT), 3));
    }

    @Transactional
    public SettingsResponse updateSettings(SettingsUpdateRequest request) {
        Map<String, String> incoming = new LinkedHashMap<>();
        incoming.put(ORG_NAME, request.getOrgName().trim());
        incoming.put(SUPPORT_EMAIL, request.getSupportEmail() != null ? request.getSupportEmail().trim() : "");
        incoming.put(DEFAULT_PASS_MARK, String.valueOf(request.getDefaultPassMark()));
        incoming.put(DEFAULT_DURATION, String.valueOf(request.getDefaultDurationMinutes()));
        incoming.put(DEFAULT_LINK_VALIDITY, String.valueOf(request.getDefaultLinkValidityDays()));
        incoming.put(PROCTORING_ENABLED, String.valueOf(request.isProctoringEnabled()));
        incoming.put(SHUFFLE_QUESTIONS, String.valueOf(request.isShuffleQuestions()));
        incoming.put(SHUFFLE_OPTIONS, String.valueOf(request.isShuffleOptions()));
        incoming.put(SHOW_RESULT_TO_CANDIDATE, String.valueOf(request.isShowResultToCandidate()));
        incoming.put(TAB_SWITCH_LIMIT, String.valueOf(request.getTabSwitchLimit()));

        for (Map.Entry<String, String> e : incoming.entrySet()) {
            AppSetting setting = repository.findById(e.getKey()).orElseGet(() -> new AppSetting(e.getKey(), null));
            setting.setValue(e.getValue());
            setting.setUpdatedAt(LocalDateTime.now());
            repository.save(setting);
        }
        return getSettings();
    }

    // ---- typed accessors for other services ----

    @Transactional(readOnly = true)
    public String getOrgName() {
        return loadAll().get(ORG_NAME);
    }

    @Transactional(readOnly = true)
    public String getSupportEmail() {
        return loadAll().get(SUPPORT_EMAIL);
    }

    @Transactional(readOnly = true)
    public boolean isShuffleQuestions() {
        return parseBool(loadAll().get(SHUFFLE_QUESTIONS), false);
    }

    @Transactional(readOnly = true)
    public boolean isShuffleOptions() {
        return parseBool(loadAll().get(SHUFFLE_OPTIONS), false);
    }

    @Transactional(readOnly = true)
    public boolean isShowResultToCandidate() {
        return parseBool(loadAll().get(SHOW_RESULT_TO_CANDIDATE), true);
    }

    @Transactional(readOnly = true)
    public int getTabSwitchLimit() {
        return parseInt(loadAll().get(TAB_SWITCH_LIMIT), 3);
    }

    private int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private boolean parseBool(String s, boolean fallback) {
        if (s == null) return fallback;
        return "true".equalsIgnoreCase(s.trim()) || "1".equals(s.trim());
    }
}
