package com.donn.yygh.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.donn.yygh.oss.prop.OssProperties;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/10/7 13:10
 **/
@Service
public class OssService {

    @Autowired
    private OssProperties ossProperties;

    public String upload(MultipartFile file) {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = ossProperties.getEndpoint();
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ossProperties.getKeyid();
        String accessKeySecret = ossProperties.getKeysecret();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ossProperties.getBucketname();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        //为了防止出现重复名称的文件，出现覆盖情况
        //加上年月日是为了在 bucket文件夹中再细分 根据年月日建立的子文件夹，将当天上传的图片放到同一子文件夹中
        String fileName = new DateTime().toString("yyyy/MM/dd") +
                UUID.randomUUID().toString().replaceAll("-","")+file.getOriginalFilename();

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, fileName, file.getInputStream());
            return " https://"+ossProperties.getBucketname()+"."+ossProperties.getEndpoint()+"/"+fileName;
        } catch(Exception ce) {
            System.out.println(ce.getMessage());
            return  null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
