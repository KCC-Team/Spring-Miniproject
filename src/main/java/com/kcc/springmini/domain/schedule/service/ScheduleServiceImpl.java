package com.kcc.springmini.domain.schedule.service;

import com.kcc.springmini.domain.meetup.service.MeetUpService;
import com.kcc.springmini.domain.member.model.vo.MemberVO;
import com.kcc.springmini.domain.schedule.mapper.ScheduleMapper;
import com.kcc.springmini.domain.schedule.model.ScheduleVO;
import com.kcc.springmini.domain.schedule.model.dto.PageResponseDto;
import com.kcc.springmini.domain.schedule.model.dto.ScheduleListResponseDto;
import com.kcc.springmini.domain.schedule.model.dto.ScheduleResponseDto;
import com.kcc.springmini.global.exception.BadRequestException;
import com.kcc.springmini.global.exception.NotFoundException;
import com.kcc.springmini.global.exception.NotFoundToErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
    private final ScheduleMapper scheduleMapper;
    private final MeetUpService meetUpService;

    private static final int LIMIT = 3;
    private static final int PAGE_LIMIT_SIZE = 5;

    @Transactional
    @Override
    public void save(MemberVO member, Long meetupId, ScheduleVO scheduleVO) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (meetUpService.findById(meetupId).isEmpty()) {
            throw new NotFoundException("존재하지 않는 모임입니다.");
        }

        scheduleVO.setMeetUpId(meetupId);
        scheduleVO.setMemberId(member.getMemberId());

        LocalDate deadline = LocalDate.parse(scheduleVO.getDeadline());
        scheduleVO.setDeadline(deadline.atStartOfDay().format(dateFormatter));
        scheduleVO.setScheduleDateTime(LocalDateTime.parse(scheduleVO.getScheduleDateTime(), inputFormatter).format(outputFormatter));

        scheduleMapper.save(scheduleVO);
        participateSchedule(meetupId, scheduleVO.getScheduleId(), member.getMemberId());
    }

    @Transactional
    @Override
    public void participateSchedule(Long meetupId, Long scheduleId, Long memberId) {
        Map<String, Long> map = new HashMap<>();
        map.put("meetUpId", meetupId);
        map.put("memberId", memberId);
        map.put("scheduleId", scheduleId);

        // 모임 일정이 마감되었는지 확인 (비관 락)
        if (scheduleMapper.lockScheduleMember(scheduleId) == 0) {
            throw new BadRequestException("모임 일정이 마감되었습니다.");
        }

        // 모임 일정 참여자 생성
        if (scheduleMapper.saveMember(map) == 0) {
            throw new BadRequestException("모임 일정에 참여할 수 없습니다.");
        }
    }

    @Override
    public boolean isParticipated(Long meetupId, Long scheduleId, Long memberId) {
        Map<String, Long> map = new HashMap<>();
        map.put("meetUpId", meetupId);
        map.put("memberId", memberId);
        map.put("scheduleId", scheduleId);
        return scheduleMapper.isParticipated(map) > 0;
    }

    @Override
    public void delete(Long id, Long memberId) {
        if (scheduleMapper.delete(id, memberId) == 0) {
            throw new BadRequestException("모임 일정을 삭제할 수 없습니다.");
        }
    }

    @Transactional
    @Override
    public void update(Long id, ScheduleVO scheduleVO) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ScheduleResponseDto schedule = scheduleMapper.findById(id);
        if (schedule == null) {
            throw new NotFoundException("존재하지 않는 일정입니다.");
        }

        LocalDate deadline = LocalDate.parse(scheduleVO.getDeadline());
        scheduleVO.setDeadline(deadline.atStartOfDay().format(dateFormatter));
        scheduleVO.setScheduleDateTime(LocalDateTime.parse(scheduleVO.getScheduleDateTime(), inputFormatter).format(outputFormatter));

        if (scheduleMapper.update(id, scheduleVO) == 0) {
            throw new BadRequestException("모임 일정을 수정할 수 없습니다.");
        }
    }

    @Override
    public ScheduleResponseDto findById(Long id) {
        return scheduleMapper.findById(id);
    }

    @Override
    public PageResponseDto findAll(Long meetUpId, String keyword, String type, int page) {
        Map<String, Object> map = prepareParameters(meetUpId, page, LIMIT, keyword, type);
        List<ScheduleListResponseDto> schedules = scheduleMapper.findAll(map);

        Long totalCount = scheduleMapper.count(meetUpId);
        int realEndPage = (int) Math.ceil((double) totalCount / LIMIT);
        int startPage = Math.max((((page - 1) / PAGE_LIMIT_SIZE) * PAGE_LIMIT_SIZE + 1), 1);
        int endPage = Math.min(startPage + PAGE_LIMIT_SIZE - 1, realEndPage);
        if (page < 1 || page > realEndPage && realEndPage != 0) {
            throw new NotFoundToErrorException("존재하지 않는 페이지입니다.", HttpStatus.BAD_REQUEST);
        }
        return new PageResponseDto(page, startPage, endPage, schedules);
    }

    @Transactional
    @Override
    public void checkDeadline() {
        scheduleMapper.checkDeadline();
    }

    private Map<String, Object> prepareParameters(Long meetUpId, int page, int limit, String keyword, String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("meetUpId", meetUpId);
        map.put("offset", (long) (page - 1) * limit);
        map.put("limit", (long) limit);
        map.put("keyword", keyword);
        map.put("type", type);
        return map;
    }
}
