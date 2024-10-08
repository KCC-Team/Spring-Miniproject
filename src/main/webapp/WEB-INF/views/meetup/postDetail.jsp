
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>PET KCC</title>

    <script src="https://code.jquery.com/jquery-latest.min.js"></script>
    <script src="../../../resources/common/common.js" type="text/javascript"></script>
    <link rel="icon" href="../../../resources/images/spring-logo.ico" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
      integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
      crossorigin="anonymous"
    />
    <script
      src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
      integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
      crossorigin="anonymous"
    ></script>

    <link rel="stylesheet" type="text/css" href="../../../resources/common/common.css" />
    <link
      rel="stylesheet"
      type="text/css"
      href="../../../resources/css/postDetail.css"
    />
    <style>
      .container {
        margin-bottom: 30px;
      }

      .comment-box {
        margin-bottom: 15px;
        padding-left: 0;
      }
      .reply-box {
        margin-left: 40px;
        margin-top: 10px;
      }
      
      .comment-indent {
        padding-left: calc(30px * var(--dept-level));
      }

      .reply-container {
        max-width: 800px;
      }

      .reply-title {
        font-size: 25px;
        font-weight: bold;
      }
    .j-reply-name-date-box{
      display: flex;
      flex-direction: row;
      justify-content: space-between;
    }
    .j-reply-content-box{
      display: flex;
      justify-content: space-between;
      white-space: normal;
    }
    .j-reply-content{
      width: 70%;
      white-space: normal;
      word-break: normal;
    }
    .j-reply-button{
    }
      .hidden {
        display: none;
      }
    </style>
</head>
<body>

	<header>
		<%@ include file="../../../resources/common/header.jsp"%>
	</header>

	<div class="container">
		<section class="post-container">
			<div class="post-header">
				<img src="${post.fileUrl}" alt="프로필 사진" class="profile-img" />
				<div class="text-container">
					<h2 class="fw-bold">${post.nickname}</h2>
					<p class="post-date">${post.createdAt}</p>
				</div>
			</div>
			<div class="post-body">
				<div class="post">
					<h3>${post.title}</h3>
					<p>${post.content}</p>
				</div>
			</div>
		</section>
	</div>

	<div class="container reply-container">
		<span class="reply-title">댓글</span>
		<div class="comment-container">
			<div class="comment-box">
				<c:forEach var="reply" items="${replies}">
					<div class="comment-box comment-indent"
						style="--dept-level: ${reply.dept};">
						<div class="p-2 bg-light">
							<div class="post-header">
								<span class="hidden">${reply.replyId}</span>
								<img src="${reply.fileUrl}" alt="프로필 사진" class="profile-img" />
								<div class="text-container">
									<strong>${reply.writer.nickname}</strong>
									<small>${reply.createdAt}</small>
								</div>
							</div>
						
					
							<div class="j-reply-content-box">
								<div class="j-reply-content" style="margin-top:10px;">${reply.content}</div>
								<div class="j-reply-button">
									<input type="button" class="jw-reply-button" value="댓글달기">
									<c:if test="${reply.writer.memberId eq loginMemberId }">
										<input type="button" class="jw-edit-button" value="수정하기">
										<input type="button" value="삭제">
									</c:if>
								</div>
							</div>
						</div>
					</div>
				</c:forEach>
				<textarea id="new-comment" class="form-control jw-add-comment"
					rows="3" placeholder="댓글을 입력하세요..."></textarea>
				<button id="add-comment" class="btn btn-primary mt-2 jw-add-button">댓글
					추가</button>
			</div>
		</div>
	</div>

	<footer>
		<%@ include file="../../../resources/common/footer.jsp"%>
	</footer>

	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
		integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
		crossorigin="anonymous">

    </script>
	<script>


      $(document).ready(function () {
        $('#add-comment').on('click', function () {
          let commentText = $(this).prev('#new-comment').val();
          console.log(commentText);
          if (commentText) {
            addComment(commentText);
            $('#new-comment').val('');
          }
        });
        
        $('.comment-box').on('click', '.jw-edit-button', function(e) {
            e.stopPropagation();
            let parentElement = $(this).closest('.comment-box .comment-indent');
            
            if (parentElement.find('.jw-edit-comment').length > 0) {
                parentElement.find('.jw-edit-comment').remove();
                parentElement.find('.jw-save-button').remove();
            } else {
                let existingContent = parentElement.find('.j-reply-content').text().trim();
                console.log(existingContent);
               
                let html = "<textarea class='form-control jw-edit-comment mt-2' rows='3'>";
                html += existingContent += "</textarea>";
                html += "<button class='btn btn-primary mt-2 jw-save-button'>수정</button>"
         
                parentElement.append(html);
            }
        });
        
        
     	// 수정 버튼 클릭 이벤트 (텍스트 입력 후 수정)
        $('.comment-box').on('click', '.jw-save-button', function(e) {
            e.stopPropagation();
            let parentElement = $(this).closest('.comment-box.comment-indent');
            let newContent = parentElement.find('.jw-edit-comment').val();
            let replyId = parentElement.find('.hidden').text();
			console.log(newContent);
			console.log(replyId);
            if (newContent) {
                updateComment(replyId, newContent);
            }
        });
        
        
      });
      
      
      function updateComment(replyId, newContent) {
	  	    $.ajax({
	  	        url: '/reply',
	  	        method: 'PUT',
	  	        contentType: 'application/json; charset=utf-8',
	  	        data: JSON.stringify({
	  	            content: newContent,
	  	            replyId: replyId
	  	        }),
	  	    }).always(function () {
	  	        location.reload();
	  	    });
  	  }
      
      

      function addComment(commentText, parentId = null) {
        const url = new URL(window.location.href);
        console.log(url);

        const path = url.pathname; // "/posts/6"
        const segments = path.split('/'); // ["", "posts", "6"]
        const postId = segments.pop(); // "6"

        $.ajax({
          url: '/reply',
          method: 'POST',
          data: JSON.stringify({
            postId : postId,
            content: commentText,
            memberId: 9,
            parentId: parentId
          }),
          contentType: 'application/json',

        }).always(function () {
          location.reload();
        });
      }

      $('.comment-box').on('click','.jw-reply-button',function(e){
        e.stopPropagation();

        let parentElement = $(this).closest('.comment-box.comment-indent');

        if (parentElement.find('.jw-add-comment').length > 0) {
          parentElement.find('.jw-add-comment').remove();
          parentElement.find('.jw-add-button').remove();
        } else {
          let html = `<textarea id="new-new-comment" class="form-control jw-add-comment" rows="3" placeholder="댓글을 입력하세요..."></textarea>
                    <button id="add-new-comment" class="btn btn-primary mt-2 jw-add-button">댓글 추가</button>`;
          parentElement.append(html);
        }
      });

      $('.comment-box').on('click', '.jw-add-button', function(e) {
        e.stopPropagation();
        let commentText = $(this).prev('#new-new-comment').val();
        console.log(commentText);
        console.log("parent");
        let parentId = $(this).closest('.comment-box').find('.hidden').text();
        if (commentText) {
          addComment(commentText, parentId);
          $(this).prev('#new-new-comment').remove();
          $(this).prev('#add-new-comment').remove();
        }
      });
    </script>
</body>
</html>
