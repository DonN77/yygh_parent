package com.donn.yygh.cmn.service.impl;


import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.donn.yygh.cmn.listener.DictListener;
import com.donn.yygh.cmn.mapper.DictMapper;
import com.donn.yygh.cmn.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.donn.yygh.model.cmn.Dict;
import com.donn.yygh.model.hosp.Hospital;
import com.donn.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author donn
 * @since 2022-09-23
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Override
    //先查缓存有则使用缓存；无则查数据库，再将查到的数据放到缓存中
    @Cacheable(value = "children")
    public List<Dict> getChildListByPid(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",pid);
        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict:dicts){
            dict.setHasChildren(isHasChildren(dict.getId()));
        }
        return dicts;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException {
        List<Dict> list = baseMapper.selectList(null);
        List<DictEeVo> dictEeVoList = new ArrayList<>(list.size()); //优化的点：直接给dictEeVoList的size赋初值
        for (Dict dict:list){
            //因为前端展示的数据并不需要 Dict的所有属性，所以将dict的属性封装到DictEeVo对象里
            DictEeVo dictEeVo = new DictEeVo();
//            使用spring提供的工具类
            BeanUtils.copyProperties(dict,dictEeVo); //要求源对象dict和目标对象dictEeVo对应的属性名必须相同
            dictEeVoList.add(dictEeVo);
        }
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("数据字典", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        //需要使用流的方式，假如使用本地地址的话（"C:\\..."）,如果前端服务器和后端服务器不是同一个，会导致文件直接下载在后端服务器上
        EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet(0).doWrite(dictEeVoList);
    }

    @Override
    //调用该方法时，会更新数据库内容，需要清缓存，不然前端拿到的还是缓存的旧数据
    @CacheEvict(value = "children",allEntries = true)
    public void upload(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper)).sheet(0).doRead();
    }

    @Override
    //通过value值查询 市、区的名字
    public String getNameByValue(Long value) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("value",value).select("name");
        Dict dict = baseMapper.selectOne(queryWrapper);
        if(dict != null){
            return dict.getName();
        }
        return null;
    }

    @Override
    //通过 dict_code查询hosptype对应的id值，然后根据这个id和value返回医院的等级
    public String getNameByDictCodeAndValue(String dictCode, Long value) {
        QueryWrapper<Dict> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper1);
        Long id = dict.getId();
        QueryWrapper<Dict> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("parent_id",id).eq("value",value);
        Dict dict1 = baseMapper.selectOne(queryWrapper2);
        return dict1.getName();
    }

    private boolean isHasChildren(Long pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id",pid);
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }
}
