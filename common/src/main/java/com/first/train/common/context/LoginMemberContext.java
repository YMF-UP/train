package com.first.train.common.context;
//线程变量
import com.first.train.common.resp.MemberLoginResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginMemberContext {
    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberContext.class);

    private static ThreadLocal<MemberLoginResp> member = new ThreadLocal<>();

    public static MemberLoginResp getMember() {
        return member.get();
    }//获取当前线程member

    public static void setMember(MemberLoginResp member) {
        LoginMemberContext.member.set(member);
    }

    //怎么实现,首先我要搞懂生命周期--他们最后得调用这个方法吧,在哪里调用合适?又糊涂了,流程还是没明白?controller还是service层吗?不是吧
    //这一句话就行了吗?啊,我不懂啊,应该是啊,这个就是那个线程啊
    public static void remove() {
        member.remove();
    }


    public static Long getId() {
        try {
            return member.get().getId();
        } catch (Exception e) {
            LOG.error("获取登录会员信息异常", e);
            throw e;
        }
    }

}
