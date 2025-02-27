package com.clover.habbittracker.domain.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clover.habbittracker.domain.member.dto.MemberRequest;
import com.clover.habbittracker.domain.member.dto.MemberResponse;
import com.clover.habbittracker.domain.member.entity.Member;
import com.clover.habbittracker.domain.member.exception.MemberDuplicateNickName;
import com.clover.habbittracker.domain.member.exception.MemberNotFoundException;
import com.clover.habbittracker.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

	private final MemberRepository memberRepository;

	@Override
	public MemberResponse getProfile(Long memberId) {
		return memberRepository.findById(memberId)
			.map(MemberResponse::from)
			.orElseThrow(() -> new MemberNotFoundException(memberId));
	}

	@Override
	@Transactional
	public MemberResponse updateProfile(Long memberId, MemberRequest request) {
		return memberRepository.findById(memberId)
			.map(member -> update(member, request))
			.map(MemberResponse::from)
			.orElseThrow();

	}

	@Override
	public void deleteProfile(Long memberId) {
		memberRepository.deleteById(memberId);
	}


	private Member update(Member member, MemberRequest request) {
		Optional<Member> byNickName = memberRepository.findByNickName(request.getNickName());
		if (byNickName.isEmpty()) {
			Optional.ofNullable(request.getNickName()).ifPresent(member::setNickName);
			return member;
		} else {
			throw new MemberDuplicateNickName(byNickName.get().getNickName());
		}
	}
}
