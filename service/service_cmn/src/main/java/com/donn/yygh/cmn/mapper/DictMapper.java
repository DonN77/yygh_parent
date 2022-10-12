package com.donn.yygh.cmn.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.donn.yygh.model.cmn.Dict;

import java.util.List;

/**
 * <p>
 * 组织架构表 Mapper 接口
 * </p>
 *
 * @author donn
 * @since 2022-09-23
 */
public interface DictMapper extends BaseMapper<Dict> {
    void saveDictList(List<Dict> list);
}
