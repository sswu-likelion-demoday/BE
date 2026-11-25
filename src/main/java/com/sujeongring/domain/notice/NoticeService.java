package com.sujeongring.domain.notice;

import com.sujeongring.domain.notice.NoticeListResponse;
import com.sujeongring.domain.notice.NoticeCategory;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class NoticeService {

    // 공지사항 하드코딩 데이터
    private static final List<NoticeListResponse.NoticeItem> NOTICES =
            List.of(
                    new NoticeListResponse.NoticeItem(
                            31L,
                            NoticeCategory.EVENT,
                            "IT융합대학의 날 · 수정 2배 이벤트",
                            LocalDate.of(2026, 9, 24)
                    ),
                    new NoticeListResponse.NoticeItem(
                            30L,
                            NoticeCategory.SERVICE,
                            "퀘스트 리워드 정책 변경 안내",
                            LocalDate.of(2026, 9, 20)
                    ),
                    new NoticeListResponse.NoticeItem(
                            29L,
                            NoticeCategory.SERVICE,
                            "수정 사용 안내",
                            LocalDate.of(2026, 9, 15)
                    ),
                    new NoticeListResponse.NoticeItem(
                            28L,
                            NoticeCategory.SERVICE,
                            "관계 단계 기능이 추가됐어요",
                            LocalDate.of(2026, 9, 10)
                    )
            );

    // 공지사항 목록 조회
    public NoticeListResponse getNotices(
            NoticeCategory category
    ) {

        List<NoticeListResponse.NoticeItem> notices =
                NOTICES.stream()

                        // 카테고리별 필터링
                        .filter(notice ->
                                category == NoticeCategory.ALL
                                        || notice.category() == category
                        )

                        // 최신순 정렬
                        .sorted(
                                Comparator.comparing(
                                        NoticeListResponse.NoticeItem::createdAt
                                ).reversed()
                        )

                        .toList();

        return new NoticeListResponse(notices);
    }
}
