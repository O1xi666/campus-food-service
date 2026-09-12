package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.TaskInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskInfoMapper extends BaseMapper<TaskInfo> {
    // 这个接口现在是空的，但因为它继承了 BaseMapper<TaskInfo>
    // 所以它已经自动拥有了对 TaskInfo 表进行增删改查的所有能力！
}