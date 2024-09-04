package com.kcc.springmini.domain.meetup.service;


import com.kcc.springmini.domain.meetup.model.dto.Criteria;
import com.kcc.springmini.domain.meetup.model.dto.MeetUpRequestDto;
import com.kcc.springmini.domain.meetup.model.vo.MeetUpVO;

import java.util.List;
import java.util.Optional;

public interface MeetUpService {
	void insertMeetup(MeetUpRequestDto dto);
    List<MeetUpVO> findAll(Criteria cri);
    List<MeetUpVO> findByTitle(String title, Criteria cri);
    int getTotalCount(String title, Criteria cri);
    int getMemberTotal(Long memberId);
    boolean isPass(Long meetUpId, Long memberId);
    void join(Long meetUpId, Long memberId, String grade);
    void insertQuestion(Long meetUpId,String content);
    Optional<MeetUpVO> findById(Long meetUpId);
}
