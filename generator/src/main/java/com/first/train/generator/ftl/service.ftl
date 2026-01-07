package com.first.train.${module}.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
<#--import com.first.train.common.context.LoginMemberContext;-->
import com.first.train.common.resp.PageResp;
import com.first.train.common.util.SnowUtil;
import com.first.train.${module}.domain.${Domain};
import com.first.train.${module}.domain.${Domain}Example;
import com.first.train.${module}.mapper.${Domain}Mapper;
import com.first.train.${module}.req.${Domain}QueryReq;
import com.first.train.${module}.req.${Domain}SaveReq;
import com.first.train.${module}.resp.${Domain}QueryResp;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${Domain}Service {

    private static final Logger LOG = LoggerFactory.getLogger(${Domain}Service.class);
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(${Domain}Service.class);

    @Resource
    private ${Domain}Mapper ${domain}Mapper;
    public void save(${Domain}SaveReq req)
    {
        DateTime now=DateTime.now();
        ${Domain} ${domain} = BeanUtil.copyProperties(req, ${Domain}.class);
        if(ObjectUtil.isNull(${domain}.getId())){
<#--            ${domain}.setMemberId(LoginMemberContext.getId());-->
            ${domain}.setId(SnowUtil.getSnowflakeNextId());
            ${domain}.setCreateTime(now);
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.insert(${domain});
        }else{
            ${domain}.setUpdateTime(now);
            ${domain}Mapper.updateByPrimaryKey(${domain});
        }

    }
    public PageResp<${Domain}QueryResp> queryList(${Domain}QueryReq req)
    {
        ${Domain}Example ${domain}Example = new ${Domain}Example();
        ${domain}Example.setOrderByClause("id desc");
        ${Domain}Example.Criteria criteria=${domain}Example.createCriteria();
<#--        不是所有表都有memberid
               if(ObjectUtil.isNotNull(req.getMemberId())) {-->
<#--            criteria.andMemberIdEqualTo(req.getMemberId());-->
<#--        }-->
        log.info("查询页码：{}", req.getPage());
        log.info("每页条数：{}", req.getSize());

        PageHelper.startPage(req.getPage(),req.getSize());
        List<${Domain}> ${domain}List = ${domain}Mapper.selectByExample(${domain}Example);

        PageInfo<${Domain}> pageInfo=new PageInfo<>(${domain}List);
       log.info("总行数：{}", pageInfo.getTotal());
       log.info("总页数：{}", pageInfo.getPages());

        List<${Domain}QueryResp> list = BeanUtil.copyToList(${domain}List, ${Domain}QueryResp.class);

        PageResp<${Domain}QueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);

        return pageResp;
    }

    public void delete(Long id) {
      ${domain}Mapper.deleteByPrimaryKey(id);
    }
}
