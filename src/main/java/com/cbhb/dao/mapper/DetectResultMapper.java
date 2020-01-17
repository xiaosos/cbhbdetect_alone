package com.cbhb.dao.mapper;

import com.cbhb.dao.entity.DetectResult;
import com.cbhb.util.DetectResultDto;

import java.util.List;

public interface DetectResultMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DetectResult record);

    int insertSelective(DetectResult record);

    DetectResult selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DetectResult record);

    int updateByPrimaryKey(DetectResult record);

    int insertForeach(List<DetectResult> list);

    List<DetectResult> selectByDate(String date);
}