package com.bo.tutu.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import com.bo.tutu.common.ErrorCode;
import com.bo.tutu.constant.FileConstant;
import com.bo.tutu.exception.BusinessException;
import com.bo.tutu.exception.ThrowUtils;
import com.bo.tutu.model.vo.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;


@Slf4j
@Component
public class FileManager {
    @Resource
    private CosManager cosManager;

    /**
     *
     * @param multipartFile
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureResult(MultipartFile multipartFile,String uploadPathPrefix){
        //校验文件
        validFile(multipartFile);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String originalFilename = multipartFile.getOriginalFilename();
        String updatefilename = String.format("%s/%s/%s", DateUtil.formatDate(new Date()),uuid,FileUtil.getSuffix(originalFilename));
        String uploadPath=String.format("/%s/%s",uploadPathPrefix,updatefilename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            double scale = NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue();
            UploadPictureResult uploadPictureResult=new UploadPictureResult();
            uploadPictureResult.setUrl(FileConstant.COS_HOST+uploadPath);
            uploadPictureResult.setPicName(updatefilename);
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(imageInfo.getWidth());
            uploadPictureResult.setPicHeight(imageInfo.getHeight());
            uploadPictureResult.setPicScale(scale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", uploadPath);
                }
            }
        }
    }
    /**
     * 校验文件
     *
     * @param multipartFile
     * @param
     */
    private void validFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null,ErrorCode.PARAMS_ERROR,"文件不能为空");
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L * 10;
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 10M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
