package dev.fitqueue.controller;

import org.springframework.web.bind.annotation.RestController;

import dev.fitqueue.dto.CreateMemberRequest;
import dev.fitqueue.dto.MemberResponse;
import dev.fitqueue.service.MemberService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
public class MemberController {
    
    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/api/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@Valid @RequestBody CreateMemberRequest request){
        return memberService.createMember(request);
    }

    @GetMapping("/api/members/me")
    public MemberResponse getCurrentMember(Principal principal) {
        return memberService.getMemberByEmail(principal.getName());
    }
    
}
