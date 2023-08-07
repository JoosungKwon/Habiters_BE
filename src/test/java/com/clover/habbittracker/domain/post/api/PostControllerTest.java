package com.clover.habbittracker.domain.post.api;

import static com.clover.habbittracker.global.restdocs.config.RestDocsConfig.*;
import static com.clover.habbittracker.global.restdocs.util.DocumentLinkGenerator.*;
import static com.clover.habbittracker.util.PostProvider.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import com.clover.habbittracker.base.RestDocsSupport;
import com.clover.habbittracker.domain.post.dto.PostRequest;
import com.clover.habbittracker.domain.post.dto.PostSearchCondition;
import com.clover.habbittracker.domain.post.entity.Post;
import com.clover.habbittracker.domain.post.repository.PostRepository;
import com.clover.habbittracker.util.CustomTransaction;

import jakarta.transaction.Transactional;

@Transactional(value = Transactional.TxType.REQUIRED)
class PostControllerTest extends RestDocsSupport {

	@Autowired
	private PostRepository postRepository;

	private Post savedPost;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionStatus transactionStatus;

	private final TransactionDefinition transactionDefinition = new CustomTransaction();

	@BeforeEach
	void setUp() {
		Post testPost = createTestPost(savedMember);
		savedPost = postRepository.save(testPost);
	}

	@Test
	@DisplayName("회원은 게시글을 등록 할 수 있다.")
	void createPostTest() throws Exception {
		//given
		PostRequest postRequest = createPostRequest();
		String request = objectMapper.writeValueAsString(postRequest);

		//when then
		mockMvc.perform(post("/posts")
				.header("Authorization", "Bearer " + accessToken)
				.contentType(APPLICATION_JSON)
				.content(request))
			.andExpect(status().isCreated())
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				),
				requestFields(
					fieldWithPath("title").description("게시글 제목").attributes(field("constraints", "아직 미정")),
					fieldWithPath("content").description("게시글 본문").attributes(field("constraints", "아직 미정")),
					fieldWithPath("category").description(generateLinkCode(DocUrl.CATEGORY))
				),
				responseHeaders(
					headerWithName("Location").description("리다이렉트 URL")
				)
			));

	}

	@Test
	@DisplayName("사용자는 게시글 리스트를 조회 할 수 있다.")
	void getPostListTest() throws Exception {

		//when then
		mockMvc.perform(get("/posts")
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[*].id").exists())
			.andExpect(jsonPath("$.data[*].title").exists())
			.andExpect(jsonPath("$.data[*].content").exists())
			.andExpect(jsonPath("$.data[*].category").exists())
			.andExpect(jsonPath("$.data[*].views").exists())
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				),
				responseFields(
					fieldWithPath("code").type(STRING).description("결과 코드"),
					fieldWithPath("message").type(STRING).description("결과 메세지"),
					fieldWithPath("data[].id").type(NUMBER).description("게시글 id"),
					fieldWithPath("data[].title").type(STRING).description("게시글 제목"),
					fieldWithPath("data[].content").type(STRING).description("게시글 본문"),
					fieldWithPath("data[].category").type(STRING).description(generateLinkCode(DocUrl.CATEGORY)),
					fieldWithPath("data[].views").type(NUMBER).description("조회수"),
					fieldWithPath("data[].numOfComments").type(NUMBER).description("댓글 수"),
					fieldWithPath("data[].numOfEmojis").type(NUMBER).description("이모지 수"),
					fieldWithPath("data[].createDate").type(STRING).description("생성 날짜")
				)
			));
	}

	@Test
	@DisplayName("사용자는 게시글을 단건 조회 할 수 있다.")
	void getPostTest() throws Exception {

		//when then
		mockMvc.perform(get("/posts/{postId}", savedPost.getId())
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.title").exists())
			.andExpect(jsonPath("$.data.content").exists())
			.andExpect(jsonPath("$.data.category").exists())
			.andExpect(jsonPath("$.data.views").exists())
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				),
				responseFields(
					fieldWithPath("code").type(STRING).description("결과 코드"),
					fieldWithPath("message").type(STRING).description("결과 메세지"),
					fieldWithPath("data.title").type(STRING).description("게시글 제목"),
					fieldWithPath("data.content").type(STRING).description("게시글 본문"),
					fieldWithPath("data.category").type(STRING).description(generateLinkCode(DocUrl.CATEGORY)),
					fieldWithPath("data.views").type(NUMBER).description("조회수"),
					fieldWithPath("data.comments").type(ARRAY).description("댓글 수"),
					fieldWithPath("data.emojis").type(ARRAY).description("이모지 수"),
					fieldWithPath("data.createDate").type(STRING).description("생성 날짜"),
					fieldWithPath("data.updateDate").type(STRING).description("수정 날짜")
				)
			));
	}

	@Test
	@DisplayName("사용자는 게시글을 제목, 본문 으로 나눠서 검색 할 수 있다.")
	@Sql(scripts = "/searchTest_after.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	void searchPostTest() throws Exception {
		//given
		transactionStatus = transactionManager.getTransaction(transactionDefinition);
		Post post = createTestPost(this.savedMember, "searchTitle");
		Post searchPost = postRepository.save(post);
		transactionManager.commit(transactionStatus);
		PostSearchCondition postSearchCondition =
			new PostSearchCondition(Post.Category.DAILY, PostSearchCondition.SearchType.TITLE, searchPost.getTitle());
		String request = objectMapper.writeValueAsString(postSearchCondition);

		//when then
		mockMvc.perform(get("/posts/search")
				.header("Authorization", "Bearer " + accessToken)
				.param("page", "0")
				.contentType(APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].title", is(searchPost.getTitle())))
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				),
				queryParameters(
					parameterWithName("page").description("페이지 번호").optional()
				),
				requestFields(
					fieldWithPath("category").description(generateLinkCode(DocUrl.CATEGORY)),
					fieldWithPath("searchType").description(generateLinkCode(DocUrl.SEARCHTYPE)),
					fieldWithPath("keyword").description("검색 내용")
				),
				responseFields(
					beneathPath("data").withSubsectionId("data"),
					fieldWithPath("content[].id").type(NUMBER).description("게시글 id"),
					fieldWithPath("content[].title").type(STRING).description("게시글 제목"),
					fieldWithPath("content[].content").type(STRING).description("게시글 본문"),
					fieldWithPath("content[].category").type(STRING).description(generateLinkCode(DocUrl.CATEGORY)),
					fieldWithPath("content[].views").type(NUMBER).description("조회수"),
					fieldWithPath("content[].numOfComments").type(NUMBER).description("댓글 수"),
					fieldWithPath("content[].numOfEmojis").type(NUMBER).description("이모지 수"),
					fieldWithPath("content[].createDate").type(STRING).description("생성 날짜"),
					//TODO: Pageable 수정 예정
					fieldWithPath("pageable.sort.empty").type(BOOLEAN).description("pageable"),
					fieldWithPath("pageable.sort.sorted").type(BOOLEAN).description("pageable"),
					fieldWithPath("pageable.sort.unsorted").type(BOOLEAN).description("pageable"),
					fieldWithPath("pageable.offset").type(NUMBER).description("pageable"),
					fieldWithPath("pageable.pageNumber").type(NUMBER).description("pageable"),
					fieldWithPath("pageable.pageSize").type(NUMBER).description("pageable"),
					fieldWithPath("pageable.paged").type(BOOLEAN).description("pageable"),
					fieldWithPath("pageable.unpaged").type(BOOLEAN).description("pageable"),
					fieldWithPath("last").type(BOOLEAN).description("pageable"),
					fieldWithPath("totalPages").type(NUMBER).description("pageable"),
					fieldWithPath("totalElements").type(NUMBER).description("pageable"),
					fieldWithPath("number").type(NUMBER).description("pageable"),
					fieldWithPath("sort.empty").type(BOOLEAN).description("pageable"),
					fieldWithPath("sort.sorted").type(BOOLEAN).description("pageable"),
					fieldWithPath("sort.unsorted").type(BOOLEAN).description("pageable"),
					fieldWithPath("first").type(BOOLEAN).description("pageable"),
					fieldWithPath("size").type(NUMBER).description("pageable"),
					fieldWithPath("numberOfElements").type(NUMBER).description("pageable"),
					fieldWithPath("empty").type(BOOLEAN).description("pageable")
				)
			));

	}

	@Test
	@DisplayName("게시글 작성자는 게시글을 수정 할 수 있다.")
	void updatePostTest() throws Exception {
		//given
		PostRequest postRequest = new PostRequest("updateTitle", "updateContent", Post.Category.STUDY);
		String request = objectMapper.writeValueAsString(postRequest);

		//when then
		mockMvc.perform(put("/posts/{postId}", savedPost.getId())
				.header("Authorization", "Bearer " + accessToken)
				.contentType(APPLICATION_JSON)
				.content(request))
			.andExpect(status().isOk())
			.andExpect(header().stringValues("Location", "/posts/" + savedPost.getId()))
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				),
				requestFields(
					fieldWithPath("title").description("수정 할 게시글 제목").attributes(field("constraints", "아직 미정")),
					fieldWithPath("content").description("수정 할 게시글 본문").attributes(field("constraints", "아직 미정")),
					fieldWithPath("category").description(generateLinkCode(DocUrl.CATEGORY))
				),
				responseHeaders(
					headerWithName("Location").description("리다이렉트 URL")
				)
			));
	}

	@Test
	@DisplayName("게시글 작성자는 게시글을 삭제 할 수 있다.")
	void deletePostTest() throws Exception {
		//when then
		mockMvc.perform(delete("/posts/{postId}", savedPost.getId())
				.header("Authorization", "Bearer " + accessToken))
			.andExpect(status().isNoContent())
			.andDo(restDocs.document(
				requestHeaders(
					headerWithName("Authorization").description("JWT Access 토큰")
				)
			));
	}
}