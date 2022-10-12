package com.donn.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.cmn.mapper.DictMapper;
import com.donn.yygh.model.cmn.Dict;
import com.donn.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/24 17:05
 **/
//这个类不能放到spring容器中，因为每次使用easyexcel的read方法都要new 一个
//easyexcel 读操作需要的类，泛型是 excel文件中对应的类
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;
//    批量插入，增加性能
    private static final int BATCH_COUNT = 5;
    private List<Dict> dictList = new ArrayList<>();

    //因为不在spring容器中，不能使用@Autowired注入mapper，只能使用有参构造器将mapper传入
    public DictListener(DictMapper dictMapper){
        this.dictMapper=dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",dict.getId());
        Integer count = dictMapper.selectCount(queryWrapper);
        if(count>0){
            dictMapper.updateById(dict);
        }else{
            dictList.add(dict);
            if(dictList.size() >= BATCH_COUNT){
                dictMapper.saveDictList(dictList);
                dictList.clear();
            }
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(dictList.size()!=0){
            dictMapper.saveDictList(dictList);
            dictList.clear();
        }
    }
}
