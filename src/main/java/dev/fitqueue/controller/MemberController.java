package dev.fitqueue.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.model.Member;
import dev.fitqueue.service.MemberService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController 
public class MemberController {
    
    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/api/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Member createMember(@Valid @RequestBody CreateMemberRequest request){
        return memberService.createMember(request);
    }
    
}
