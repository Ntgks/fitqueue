package dev.fitqueue.service;

import dev.fitqueue.model.Member;

import org.springframework.stereotype.Service;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.repository.MemberRepository;

@Service 
public class MemberService {
    
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }

    public Member createMember(CreateMemberRequest request){
        Member member = new Member(
            null,
            request.name(),
            request.email()
        );

        return memberRepository.save(member);
    }
}
