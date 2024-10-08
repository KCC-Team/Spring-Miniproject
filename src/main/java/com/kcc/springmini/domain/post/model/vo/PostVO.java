package com.kcc.springmini.domain.post.model.vo;

import com.kcc.springmini.domain.member.model.vo.MemberVO;
import lombok.Data;

import java.io.Serializable;


@Data
public class PostVO implements Serializable {
    private Long postId;
    private Long meetupId;
    private String title;
    private String content;
    private String createdAt;
    private String updatedAt;
    private String nickname;
    private MemberVO member;
    private String fileUrl;
}
