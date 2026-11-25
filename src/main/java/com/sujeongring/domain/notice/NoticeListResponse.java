package com.sujeongring.domain.notice;

import java.time.LocalDate;
import java.util.List;

public record NoticeListResponse(
        List<NoticeItem> notices
) {

    public record NoticeItem(
            Long noticeId,
            NoticeCategory category,
            String title,
            LocalDate createdAt
    ) {}
}