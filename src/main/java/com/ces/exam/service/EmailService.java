package com.ces.exam.service;

import com.ces.exam.exception.ValidationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final JavaMailSender mailSender; // null when no SMTP host is configured
    private final SettingsService settingsService;
    private final boolean enabled;
    private final String from;
    private final String fromName;
    private final String baseUrl;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        SettingsService settingsService,
                        @Value("${spring.mail.host:}") String host,
                        @Value("${app.mail.from:}") String from,
                        @Value("${app.mail.from-name:CES Assessment}") String fromName,
                        @Value("${app.public-base-url:http://localhost:3000}") String baseUrl) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.settingsService = settingsService;
        this.from = (from != null && !from.isBlank()) ? from.trim() : null;
        this.fromName = (fromName != null && !fromName.isBlank()) ? fromName : "CES Assessment";
        this.baseUrl = baseUrl != null && baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : (baseUrl != null ? baseUrl : "http://localhost:3000");
        this.enabled = mailSender != null && host != null && !host.isBlank() && this.from != null;
        log.info("EmailService initialised — enabled={}, from={}", enabled, this.from);
    }

    /** Organisation name from settings, falling back to the configured from-name. */
    private String orgName() {
        String n = settingsService.getOrgName();
        return (n != null && !n.isBlank()) ? n : fromName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String buildExamLink(String token) {
        return baseUrl + "/exam/token/" + token;
    }

    public String buildResetLink(String token) {
        return baseUrl + "/reset-password?token=" + token;
    }

    /** Best-effort password-reset e-mail — never throws (the caller must not reveal delivery state). */
    public boolean trySendPasswordReset(String toEmail, String name, String token) {
        if (!enabled || toEmail == null || toEmail.isBlank()) return false;
        String link = buildResetLink(token);
        String org = orgName();
        String supportEmail = settingsService.getSupportEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(toEmail.trim());
            helper.setFrom(from, org);
            helper.setSubject("Parolun bərpası — " + org);
            helper.setText(buildResetHtml(name, link, org, supportEmail), true);
            mailSender.send(message);
            log.info("Password-reset e-mail sent to {}", toEmail);
            return true;
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.warn("Password-reset e-mail failed for {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildResetHtml(String name, String link, String org, String supportEmail) {
        String greeting = (name != null && !name.isBlank()) ? "Salam, " + escape(name) + "!" : "Salam!";
        String footerNote = (supportEmail != null && !supportEmail.isBlank())
                ? "Bu sorğunu siz etməmisinizsə, bu mesajı nəzərə almayın və ya <a href=\"mailto:"
                  + escape(supportEmail) + "\" style=\"color:#64748b;\">" + escape(supportEmail) + "</a> ilə əlaqə saxlayın."
                : "Bu sorğunu siz etməmisinizsə, bu mesajı nəzərə almayın.";

        return "<!DOCTYPE html><html lang=\"az\"><body style=\"margin:0;background:#f1f5f9;padding:24px;"
                + "font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:560px;background:#ffffff;border-radius:16px;overflow:hidden;"
                + "box-shadow:0 4px 16px rgba(15,23,42,0.08);\">"
                + "<tr><td style=\"background:linear-gradient(135deg,#21201B,#463F2E);padding:28px 32px;\">"
                + "<span style=\"color:#ffffff;font-size:18px;font-weight:700;letter-spacing:-0.3px;\">"
                + escape(org) + "</span></td></tr>"
                + "<tr><td style=\"padding:32px;\">"
                + "<p style=\"margin:0 0 14px;font-size:15px;color:#0f172a;font-weight:600;\">" + greeting + "</p>"
                + "<p style=\"margin:0 0 18px;font-size:14px;line-height:1.6;color:#334155;\">"
                + "Hesabınızın parolunu bərpa etmək üçün sorğu aldıq. Yeni parol təyin etmək üçün aşağıdakı düyməyə klikləyin. "
                + "Bu link <b>30 dəqiqə</b> ərzində etibarlıdır.</p>"
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:8px 0 22px;\"><tr><td "
                + "style=\"border-radius:10px;background:#8E6F17;\">"
                + "<a href=\"" + link + "\" style=\"display:inline-block;padding:13px 26px;font-size:15px;"
                + "font-weight:600;color:#ffffff;text-decoration:none;\">Parolu bərpa et</a></td></tr></table>"
                + "<p style=\"margin:0 0 6px;font-size:12px;color:#64748b;\">Düymə işləmirsə, bu linki brauzerə kopyalayın:</p>"
                + "<p style=\"margin:0 0 4px;font-size:12px;word-break:break-all;\"><a href=\"" + link
                + "\" style=\"color:#8E6F17;\">" + link + "</a></p>"
                + "</td></tr>"
                + "<tr><td style=\"padding:18px 32px;background:#f8fafc;border-top:1px solid #e2e8f0;\">"
                + "<p style=\"margin:0;font-size:12px;color:#94a3b8;\">" + footerNote + "</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    /** Sends the invite, throwing a ValidationException (with a user-facing message) on any failure. */
    public void sendExamInvite(String toEmail, String candidateName, String examTitle,
                               String token, LocalDateTime endDate) {
        if (!enabled) {
            throw new ValidationException("E-poçt xidməti konfiqurasiya olunmayıb (SMTP təyin edilməyib).");
        }
        if (toEmail == null || toEmail.isBlank()) {
            throw new ValidationException("Alıcı e-poçt ünvanı boşdur.");
        }
        String link = buildExamLink(token);
        String org = orgName();
        String supportEmail = settingsService.getSupportEmail();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // multipart=true so the logo/hero can be embedded inline (CID) — external/localhost
            // URLs are unreachable or blocked in most mail clients.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail.trim());
            helper.setFrom(from, org);
            helper.setSubject("\"" + examTitle + "\" imtahanına dəvət");
            helper.setText(buildHtml(candidateName, examTitle, link, endDate, org, supportEmail), true);
            addInlineImage(helper, "cesLogo", "email/logo-dark.png");
            addInlineImage(helper, "examHero", "email/exam-invite.png");
            mailSender.send(message);
            log.info("Exam invite sent to {} for \"{}\"", toEmail, examTitle);
        } catch (MailException | MessagingException | UnsupportedEncodingException e) {
            log.warn("Exam invite e-mail failed for {}: {}", toEmail, e.getMessage());
            throw new ValidationException("E-poçt göndərilə bilmədi: " + e.getMessage());
        }
    }

    /** Best-effort variant for auto-send during assignment — never throws. */
    public boolean trySendExamInvite(String toEmail, String candidateName, String examTitle,
                                     String token, LocalDateTime endDate) {
        try {
            sendExamInvite(toEmail, candidateName, examTitle, token, endDate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Attaches a bundled image as an inline (CID) part; skips silently if the resource is missing. */
    private void addInlineImage(MimeMessageHelper helper, String cid, String classpath) {
        try {
            ClassPathResource res = new ClassPathResource(classpath);
            if (res.exists()) {
                helper.addInline(cid, res, "image/png");
            } else {
                log.warn("Inline e-mail image missing on classpath: {}", classpath);
            }
        } catch (MessagingException e) {
            log.warn("Could not embed inline image {}: {}", classpath, e.getMessage());
        }
    }

    private String buildHtml(String candidateName, String examTitle, String link, LocalDateTime endDate,
                             String org, String supportEmail) {
        String logoUrl = "cid:cesLogo";  // white wordmark + gold mark, embedded inline (email/logo-dark.png)
        String heroUrl = "cid:examHero"; // GPT-generated hero banner, embedded inline (email/exam-invite.png)

        String greeting = (candidateName != null && !candidateName.isBlank())
                ? "Salam, " + escape(candidateName) + "!"
                : "Salam!";
        String deadlineCard = endDate != null
                ? "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:0 0 22px;\"><tr>"
                  + "<td style=\"background:#faf5e9;border:1px solid #e6d09c;border-radius:11px;padding:13px 18px;\">"
                  + "<span style=\"font-size:11px;font-weight:700;letter-spacing:0.5px;text-transform:uppercase;color:#8e6f17;\">Son tarix</span>"
                  + "<span style=\"display:block;margin-top:3px;font-size:15px;font-weight:700;color:#232019;\">"
                  + DATE_FMT.format(endDate) + "</span></td></tr></table>"
                : "";
        String footerNote = (supportEmail != null && !supportEmail.isBlank())
                ? "Suallarınız üçün: <a href=\"mailto:" + escape(supportEmail) + "\" style=\"color:#8e6f17;\">"
                  + escape(supportEmail) + "</a>"
                : "Bu avtomatik mesajdır. Suallarınız üçün təşkilatçı ilə əlaqə saxlayın.";

        return "<!DOCTYPE html><html lang=\"az\"><body style=\"margin:0;padding:24px 12px;background:#f1ece1;"
                + "font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"560\" cellpadding=\"0\" cellspacing=\"0\" "
                + "style=\"max-width:560px;width:100%;background:#ffffff;border-radius:18px;overflow:hidden;"
                + "box-shadow:0 8px 28px rgba(35,32,25,0.14);\">"
                // header — real CES logo on charcoal
                + "<tr><td style=\"background:#232019;padding:22px 32px 18px;\">"
                + "<img src=\"" + logoUrl + "\" alt=\"" + escape(org) + "\" height=\"44\" "
                + "style=\"display:block;height:44px;width:auto;border:0;\" /></td></tr>"
                // hero banner — charcoal backdrop so it degrades gracefully if images are blocked
                + "<tr><td style=\"background:#1b1813;font-size:0;line-height:0;\">"
                + "<img src=\"" + heroUrl + "\" alt=\"\" width=\"560\" "
                + "style=\"display:block;width:100%;max-width:560px;height:auto;border:0;\" /></td></tr>"
                // body
                + "<tr><td style=\"padding:34px 32px 28px;\">"
                + "<p style=\"margin:0 0 10px;font-size:12px;font-weight:700;letter-spacing:1.5px;"
                + "text-transform:uppercase;color:#8e6f17;\">İmtahan dəvəti</p>"
                + "<p style=\"margin:0 0 14px;font-size:18px;font-weight:700;color:#232019;\">" + greeting + "</p>"
                + "<p style=\"margin:0 0 20px;font-size:14px;line-height:1.65;color:#3f3a30;\">"
                + "Sizə <b style=\"color:#232019;\">&ldquo;" + escape(examTitle) + "&rdquo;</b> imtahanı təyin olunub. "
                + "Aşağıdakı <b>tək-istifadəlik</b> link ilə imtahana birbaşa başlaya bilərsiniz — giriş tələb olunmur.</p>"
                + deadlineCard
                + "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:6px 0 24px;\"><tr><td "
                + "style=\"border-radius:11px;background:#8e6f17;background:linear-gradient(135deg,#b4902f,#8a6a12);"
                + "box-shadow:0 6px 16px rgba(142,111,23,0.35);\">"
                + "<a href=\"" + link + "\" style=\"display:inline-block;padding:15px 32px;font-size:15px;"
                + "font-weight:700;color:#ffffff;text-decoration:none;\">İmtahana başla &rarr;</a></td></tr></table>"
                + "<p style=\"margin:0 0 6px;font-size:12px;color:#8a8170;\">Düymə işləmirsə, bu linki brauzerə kopyalayın:</p>"
                + "<p style=\"margin:0;font-size:12px;word-break:break-all;\"><a href=\"" + link
                + "\" style=\"color:#8e6f17;\">" + link + "</a></p>"
                + "</td></tr>"
                // footer
                + "<tr><td style=\"padding:18px 32px;background:#faf6ec;border-top:1px solid #ece1c8;\">"
                + "<p style=\"margin:0 0 4px;font-size:12px;color:#9a8f78;\">" + footerNote + "</p>"
                + "<p style=\"margin:0;font-size:11px;color:#b9af98;\">&copy; " + escape(org) + "</p>"
                + "</td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
