package com.clover.habbittracker.domain.post.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.clover.habbittracker.domain.post.dto.PostRequest;
import com.clover.habbittracker.domain.post.dto.PostDetailResponse;
import com.clover.habbittracker.domain.post.dto.PostResponse;

public interface PostService {
	Long register(Long memberId, PostRequest request);

	PostDetailResponse getPost(Long postId);

	List<PostResponse> getPostList(Pageable pageable);

	void updatePost(Long postId, PostRequest request);
}
