package com.kcc.springmini.domain.meetup.controller;

import com.kcc.springmini.domain.meetup.model.dto.Criteria;
import com.kcc.springmini.domain.meetup.model.dto.MeetUpRequestDto;
import com.kcc.springmini.domain.meetup.model.dto.PageDto;
import com.kcc.springmini.domain.meetup.model.vo.Question;
import com.kcc.springmini.domain.meetup.service.MeetUpService;
import com.kcc.springmini.domain.member.model.dto.MemberApproveRequestDto;
import com.kcc.springmini.domain.member.service.MemberService;
import com.kcc.springmini.domain.post.model.vo.PostVO;
import com.kcc.springmini.domain.post.service.PostService;
import com.kcc.springmini.domain.schedule.service.ScheduleService;
import com.kcc.springmini.global.aop.LoginValid;
import com.kcc.springmini.global.auth.PrincipalDetail;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/meetups")
public class MeetUpController {
    private final PostService postService;
    private final MeetUpService meetUpService;
    private final ScheduleService scheduleService;
    private final MemberService memberService;

    @GetMapping("/{meetUpId}")
    public String meetUp(@AuthenticationPrincipal PrincipalDetail principalDetail,
            @PathVariable("meetUpId") Long meetUpId,
    					 Criteria cri, Model model) {
        List<PostVO> posts = postService.findAll(meetUpId); //전체 글 (총 게시글 갯수에만 사용)
        List<PostVO> totalPaging = postService.findAllWithPaging(cri, meetUpId); //페이징된 글 

        model.addAttribute("posts", totalPaging); //현 페이지에서 보여줄 글 목록
		model.addAttribute("pageMaker", new PageDto(cri, posts.size())); //페이징 수 ex.(|1|2|3|4|5)
        model.addAttribute("totalPosts", posts.size()); //총 게시글
        model.addAttribute("totalMembers", meetUpService.getMemberTotal(meetUpId)); //모임 인원
        model.addAttribute("meetupId", meetUpId);
        model.addAttribute("schedules", scheduleService.findAll(meetUpId, 1));

        model.addAttribute("pageMaker", new PageDto(cri, posts.size())); //페이징 수 ex.(|1|2|3|4|5)
        if (principalDetail != null) {
            boolean pass = meetUpService.isPass(meetUpId, principalDetail.getMember().getMemberId());
            model.addAttribute("isPass", pass ? 1 : 0);
        }
         else {
            model.addAttribute("isPass", 0);
        }

        return "meetup/meetupDetail";
    }
  
    @GetMapping("/register")
    public String register() {
        return "meetup/meetupRegister";
    }
    
    @PostMapping("/register")
    public String postMeetup(@ModelAttribute MeetUpRequestDto dto ,
    		@RequestPart(value = "file", required=false) MultipartFile file) {
    	meetUpService.insertMeetup(dto);
    	return "redirect:/";
    }

    // 모임 참가
    @LoginValid
    @PostMapping("/{meetUpId}/join")
    @ResponseBody
    public String joinMeetup(@AuthenticationPrincipal PrincipalDetail principalDetail,
                             @PathVariable("meetUpId") Long meetUpId, 
                            @RequestParam Map<String, String> answers
                          ) throws IOException {
        if (principalDetail == null) {
        	return "/members/loginForm";
        }
    	
    	Long memberId = principalDetail.getMember().getMemberId();
    	
    	List<AnswerDto> answersDto = new ArrayList<>();

        answers.forEach((key, answer) -> {
            Long questionId = Long.valueOf(key);
            answersDto.add(new AnswerDto(memberId, questionId, answer, meetUpId));
        });
        
        if(answers.isEmpty()) { //즉시가입 가능 모임
        	meetUpService.join(meetUpId, principalDetail.getMember().getMemberId(), "일반회원");
        } else { //승인대기 모임
        	meetUpService.insertAnswers(answersDto);
        }
        
        return "/meetups/" + meetUpId;
    }
    
    @GetMapping("/{meetUpId}/questions")
    @ResponseBody
    public List<Question> getQuestion(@PathVariable("meetUpId") Long meetUpId, Model model) {
        model.addAttribute("message", "모임에 가입을 축하드립니다!!!");
        return meetUpService.findQuestions(meetUpId);
    }
    
    
    
    
    @PostMapping("/{meetUpId}/members/{memberId}/approve")
    @ResponseBody
    public String approveJoin(@PathVariable("meetUpId") Long meetUpId,
    	    @PathVariable("memberId") Long memberId) {
    	System.out.println("1231231231");
		System.out.println(meetUpId);
    	System.out.println(memberId);
    	
    	meetUpService.join(meetUpId, memberId, "일반회원");
    	memberService.deletePendingMember(new MemberApproveRequestDto(memberId, meetUpId));
    	return null;
    }
    
    
    
}
