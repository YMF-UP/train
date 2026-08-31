package com.first.train.business.mapper;

import com.first.train.business.domain.ConfirmOrder;
import com.first.train.business.domain.ConfirmOrderExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ConfirmOrderMapper {
    long countByExample(ConfirmOrderExample example);

    int deleteByExample(ConfirmOrderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ConfirmOrder row);

    int insertSelective(ConfirmOrder row);

    List<ConfirmOrder> selectByExample(ConfirmOrderExample example);

    ConfirmOrder selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") ConfirmOrder row, @Param("example") ConfirmOrderExample example);

    int updateByExample(@Param("row") ConfirmOrder row, @Param("example") ConfirmOrderExample example);

    int updateByPrimaryKeySelective(ConfirmOrder row);

    int updateByPrimaryKey(ConfirmOrder row);
}
