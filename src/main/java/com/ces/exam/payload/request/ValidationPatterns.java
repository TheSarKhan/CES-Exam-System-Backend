package com.ces.exam.payload.request;

/** Shared validation regexes/messages so the policy is identical everywhere. */
public final class ValidationPatterns {
    private ValidationPatterns() {}

    // Password: 8–72 chars (72 = bcrypt limit), at least one letter and one digit.
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    // Same policy but also accepts an empty string (for "leave unchanged" on update).
    public static final String PASSWORD_OPTIONAL = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    public static final String PASSWORD_MSG =
            "Parol ən azı 8 simvol olmalı və ən azı bir hərf və bir rəqəm içerməlidir";

    // Name: at least one Unicode letter; allows letters, space, dot, apostrophe, hyphen; 2–60 chars.
    public static final String NAME = "^(?=.*\\p{L})[\\p{L} .'\\-]{2,60}$";
    public static final String NAME_MSG = "Düzgün ad daxil edin (yalnız hərflər, ən azı bir hərf)";
}
