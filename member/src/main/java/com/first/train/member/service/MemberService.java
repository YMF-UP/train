package com.first.train.member.service;

import cn.hutool.core.collection.CollUtil;
import com.first.train.common.exception.BusinessException;
import com.first.train.common.exception.BusinessExceptionEnum;
import com.first.train.common.util.SnowUtil;
import com.first.train.member.domain.Member;
import com.first.train.member.domain.MemberExample;
import com.first.train.member.mapper.MemberMapper;
import com.first.train.member.req.MemberRegisterReq;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    @Resource
    private MemberMapper memberMapper;
    public int count()
    {
      return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req)
    {
        String mobile=req.getMobile();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);

        if(CollUtil.isNotEmpty(list))
        {
           // return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_EXIST);
        }

        Member member = new Member();
        //雪花算法 64比特 41时间戳 10机器id 12序列 1符号位默认正
        //
        member.setId(SnowUtil.getSnowflakeNextId());
        member.setMobile(mobile);

        memberMapper.insert(member);
        return  member.getId();
    }


}
