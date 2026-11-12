package dev.fitqueue.service;

import dev.fitqueue.model.Member;

import org.springframework.stereotype.Service;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.repository.MemberRepository;
import dev.fitqueue.dto.MemberResponse;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service 
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public MemberResponse createMember(CreateMemberRequest request){
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Password must not exceed 72 UTF-8 bytes"
            );
        }

        String passwordHash = passwordEncoder.encode(request.password());

        Member member = new Member(
            null,
            request.name(),
            request.email(),
            passwordHash
        );

        Member savedMember = memberRepository.save(member);

        return new MemberResponse(
            savedMember.getId(),
            savedMember.getName(),
            savedMember.getEmail()
        );
    }

    public MemberResponse getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Member not found"
            ));

        return new MemberResponse(
            member.getId(),
            member.getName(),
            member.getEmail()
        );
        
    }
}
