package com.first.train.member.controller;

import com.first.train.common.rsp.CommonResp;
import com.first.train.member.req.MemberRegisterReq;
import com.first.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
public class MemberController {
    @Resource
    private MemberService memberService;
    @GetMapping("/count")
    public CommonResp<Integer> count()
    {   int count= memberService.count();
        CommonResp<Integer> commonResp = new CommonResp<>();
        commonResp.setContent(count);
        return commonResp ;
    }
    @PostMapping ("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req)
    {
        long register=memberService.register(req);

        CommonResp<Long> commonResp = new CommonResp<>();
        commonResp.setContent(register);
        return commonResp;
    }


}
