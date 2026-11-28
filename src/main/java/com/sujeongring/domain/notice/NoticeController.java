package com.sujeongring.domain.notice;

import com.sujeongring.domain.notice.NoticeListResponse;
import com.sujeongring.domain.notice.NoticeCategory;
import com.sujeongring.domain.notice.NoticeService;
import com.sujeongring.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "공지사항 목록 조회",
            description = "전체, 서비스 소식, 이벤트별 공지사항 조회"
    )
    @GetMapping
    public ApiResponse<NoticeListResponse> getNotices(
            @RequestParam(
                    value = "category",
                    defaultValue = "ALL"
            ) NoticeCategory category
    ) {

        return ApiResponse.success(
                "공지사항을 조회하였습니다.",
                noticeService.getNotices(category)
        );
    }
}