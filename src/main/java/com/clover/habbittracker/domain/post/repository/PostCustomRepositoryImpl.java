package com.clover.habbittracker.domain.post.repository;

import static com.clover.habbittracker.domain.emoji.entity.QEmoji.*;
import static com.clover.habbittracker.domain.member.entity.QMember.*;
import static com.clover.habbittracker.domain.post.dto.PostSearchCondition.SearchType.*;
import static com.clover.habbittracker.domain.post.entity.QPost.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.clover.habbittracker.domain.post.dto.PostResponse;
import com.clover.habbittracker.domain.post.dto.PostSearchCondition;
import com.clover.habbittracker.domain.post.dto.QPostResponse;
import com.clover.habbittracker.domain.post.entity.Post;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

	private static final String MATCH_FUNCTION = "function('match',{0},{1})";

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<PostResponse> findAllPostsSummary(Pageable pageable, Post.Category category) {

		return jpaQueryFactory
			.select(QPostResponse())
			.from(post)
			.where(eqFilter(category))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createDate.desc())
			.fetch();
	}

	@Override
	public Optional<Post> joinMemberAndEmojisFindById(Long postId) {
		Post result = jpaQueryFactory.selectFrom(post)
			.leftJoin(post.member, member)
			.fetchJoin()
			.leftJoin(post.emojis, emoji)
			.fetchJoin()
			.where(eqId(postId))
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public Long updateViews(Long postId) {
		return jpaQueryFactory.update(post)
			.set(post.views, post.views.add(1))
			.where(eqId(postId))
			.execute();
	}

	@Override
	public Page<PostResponse> searchPostBy(PostSearchCondition postSearchCondition, Pageable pageable) {

		List<PostResponse> content = jpaQueryFactory
			.select(QPostResponse())
			.from(post)
			.where(
				eqFilter(postSearchCondition.getCategory()),
				matchKeyword(postSearchCondition.getSearchType(), postSearchCondition.getKeyword())
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(post.createDate.desc())
			.distinct()
			.fetch();

		Integer count = getCount();

		return new PageImpl<>(content, pageable, count);
	}

	private BooleanExpression eqId(Long postId) {
		if (postId != null) {
			return post.id.eq(postId);
		}
		return null;
	}

	private BooleanExpression eqFilter(Post.Category category) {
		if (category == null || Post.Category.ALL.equals(category)) {
			return null;
		}
		return post.category.eq(category);
	}

	private Integer getCount() {
		return jpaQueryFactory.select(post.count())
			.from(post)
			.fetch().size();
	}

	private QPostResponse QPostResponse() {
		return new QPostResponse(
			post.id,
			post.title,
			post.content,
			post.thumbnailUrl,
			post.category,
			post.views,
			post.comments.size(),
			post.emojis.size(),
			post.createDate);
	}

	private BooleanExpression matchKeyword(PostSearchCondition.SearchType searchType, String keyword) {
		if (isNotValid(keyword)) {
			return null;
		}
		if (searchType == TITLE) {
			return searchTitle(keyword);
		}
		if (searchType == CONTENT) {
			return searchContent(keyword);
		}

		BooleanExpression searchedTitleResult = searchTitle(keyword);
		BooleanExpression searchedContentResult = searchContent(keyword);

		return searchedTitleResult.or(searchedContentResult);
	}

	private BooleanExpression searchTitle(String keyword) {
		NumberExpression<Double> booleanTemplate = Expressions.numberTemplate(Double.class,
			MATCH_FUNCTION, post.title, keyword);

		return booleanTemplate.gt(0);
	}

	private BooleanExpression searchContent(String keyword) {
		NumberTemplate<Double> booleanTemplate = Expressions.numberTemplate(Double.class,
			MATCH_FUNCTION, post.content, keyword);

		return booleanTemplate.gt(0);
	}

	private boolean isNotValid(String keyword) {
		return keyword == null || keyword.isBlank() || keyword.length() < 2;
	}
}
