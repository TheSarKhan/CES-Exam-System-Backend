package com.ces.exam.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/** Builds a safe Pageable from raw request params (clamps page/size to sane bounds). */
public final class PageRequests {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    private PageRequests() {}

    public static Pageable of(Integer page, Integer size) {
        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? DEFAULT_SIZE : Math.min(Math.max(1, size), MAX_SIZE);
        return PageRequest.of(p, s);
    }
}
