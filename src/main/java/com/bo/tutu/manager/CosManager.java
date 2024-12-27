package com.bo.tutu.manager;

import cn.hutool.core.util.StrUtil;
import com.bo.tutu.utils.ContentTypeUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.bo.tutu.config.CosClientConfig;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;

import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;


/**
 * Cos 对象存储操作
 *
 * @author <a href="https://github.com/JavaBo14">Bo</a>
 * @from <a href="https://github.com/JavaBo14/BoTuTu-Backend">
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key 唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key 唯一键（代表文件在对象存储服务中的路径）
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }
    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 自定义headers
     * @param key
     * @param file
     * @param filesuffix
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file,String filesuffix,String contentDeliverType) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        //contentType 图片->filesuffix  url->filesuffix
        String contentType=contentDeliverType;
        if (StrUtil.isBlank(contentDeliverType)){
            contentType = ContentTypeUtil.getContentTypeBySuffix(filesuffix);
        }
        // 设置自定义元数据（包括 Content-Type）
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentDisposition("inline"); // 设置为 inline
        putObjectRequest.setMetadata(metadata);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 构造处理参数
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }


}
