package com.clover.habbittracker.domain.comment.api;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clover.habbittracker.domain.comment.dto.CommentRequest;
import com.clover.habbittracker.domain.comment.dto.CommentResponse;
import com.clover.habbittracker.domain.comment.service.CommentService;
import com.clover.habbittracker.global.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class CommentController {

	private final CommentService commentService;

	@PostMapping("/{postId}/comments")
	public ResponseEntity<ApiResponse<CommentResponse>> createComment(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long postId,
		@RequestBody CommentRequest request
	) {
		CommentResponse comment = commentService.createComment(memberId, postId, request);
		ApiResponse<CommentResponse> response = ApiResponse.success(comment);
		return ResponseEntity.status(CREATED).body(response);
	}

	@PutMapping("/{postId}/comments/{commentId}")
	public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
		@AuthenticationPrincipal Long memberId,
		@PathVariable Long commentId,
		@PathVariable Long postId,
		@RequestBody CommentRequest request
	) {
		CommentResponse updateComment = commentService.updateComment(memberId, commentId, postId, request);
		ApiResponse<CommentResponse> response = ApiResponse.success(updateComment);
		return ResponseEntity.ok().body(response);
	}

	@GetMapping("/comment/{commentId}/reply")
	public void getReplyList(
		@PathVariable Long commentId
	) {
		commentService.getReplyList(commentId);
	}

	@PostMapping("/comment/{commentId}/reply")
	public void createReply(
		@PathVariable Long commentId
	) {
		commentService.createReply(commentId);
	}
}
