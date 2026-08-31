package com.first.train.business.controller;

import com.first.train.business.req.ConfirmOrderDoReq;
import com.first.train.business.service.ConfirmOrderService;
import com.first.train.common.context.LoginMemberContext;
import com.first.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Object> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        req.setMemberId(LoginMemberContext.getId());
        confirmOrderService.doConfirm(req);
        return new CommonResp<>();
    }

}
