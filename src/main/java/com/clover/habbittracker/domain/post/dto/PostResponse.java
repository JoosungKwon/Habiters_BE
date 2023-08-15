package com.clover.habbittracker.domain.post.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.*;

import java.time.LocalDateTime;

import com.clover.habbittracker.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;

public record PostResponse(
	Long id,
	String title,
	String content,
	String thumbnailUrl,
	Post.Category category,
	Long views,
	Integer numOfComments,
	Integer numOfEmojis,
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createDate
) {
}
