package com.eightsnpc.todo.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eightsnpc.todo.persistence.entity.TaskEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskMapper extends BaseMapper<TaskEntity> {
}
