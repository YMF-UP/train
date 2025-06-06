package com.first.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.first.train.common.exception.BusinessException;
import com.first.train.common.exception.BusinessExceptionEnum;
import com.first.train.common.util.SnowUtil;
import com.first.train.member.domain.Member;
import com.first.train.member.domain.MemberExample;
import com.first.train.member.mapper.MemberMapper;
import com.first.train.member.req.MemberLoginReq;
import com.first.train.member.req.MemberRegisterReq;
import com.first.train.member.req.MemberSendCodeReq;
import com.first.train.member.resp.MemberLoginResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberService {

    //private static final Logger Log= (Logger) LoggerFactory.getLogger(MemberService.class);
    private static final Logger Log = LoggerFactory.getLogger(MemberService.class);

    @Resource
    private MemberMapper memberMapper;
    public int count()
    {
      return Math.toIntExact(memberMapper.countByExample(null));
    }

    public long register(MemberRegisterReq req)
    {
        String mobile=req.getMobile();
        Member memberDB = selectByMobile(mobile);


        if(ObjectUtil.isNotNull(memberDB))
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

    public void sendCode(MemberSendCodeReq req)
    {
        String mobile=req.getMobile();
        Member memberDB = selectByMobile(mobile);

        //不存在则插入
        if(ObjectUtil.isNull(memberDB))
        {
            Log.info("手机号不存在，输入");
            Member member = new Member();
            //雪花算法 64比特 41时间戳 10机器id 12序列 1符号位默认正
            //
            member.setId(SnowUtil.getSnowflakeNextId());
            member.setMobile(mobile);
            memberMapper.insert(member);
        }
        else
        {
            Log.info("手机号存在");
        }

         //生成验证码
        String code=RandomUtil.randomString(4);
        Log.info("生成短信验证码：{}",code);
        //保存短信记录表，手机号，短信验证码，有效期，是否使用，业务类型，发送时间，使用时间

        //对接短信通道，发送短信
        Log.info("对接短信");
    }

    public MemberLoginResp login(MemberLoginReq req)
    {
        String mobile=req.getMobile();
        String code=req.getCode();
        Member memberDB = selectByMobile(mobile);

        //不存在则异常
        if(ObjectUtil.isNull(memberDB))
        {
            // return list.get(0).getId();
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_Not_EXIST);
        }
        //校验验证码
        if (!"888".equals(code))
        {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_MOBILE_Code_ERROR);
        }
         MemberLoginResp memLoginResp= BeanUtil.copyProperties(memberDB,MemberLoginResp.class);
          return memLoginResp;

    }
    private Member selectByMobile(String mobile) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        List<Member> list = memberMapper.selectByExample(memberExample);
        if(CollUtil.isEmpty(list))
        {
           return null;
        }
        else
        {
           return list.get(0);
        }

    }


}
