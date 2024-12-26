package com.bo.tutu.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.bo.tutu.common.ErrorCode;
import com.bo.tutu.constant.FileConstant;
import com.bo.tutu.exception.BusinessException;
import com.bo.tutu.manager.CosManager;
import com.bo.tutu.model.vo.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;


@Slf4j
public abstract class PictureUploadTemplate {
    @Resource
    private CosManager cosManager;

    /**
     * 上传文件
     *
     * @param inputSource
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPictureResult(Object inputSource, String uploadPathPrefix) {
        // TODO: 2024/12/25  校验（图片或者url）
        validSource(inputSource);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomUtil.randomString(16);
        // TODO: 2024/12/25  获取文件名
        String originalFilename = getOriginFilename(inputSource);
        String updatefilename = String.format("%s/%s/%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, updatefilename);
        File file = null;
        try {
            // TODO: 2024/12/25 处理输入源并生成本地临时文件
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource,file);
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            //获取图片信息
            return buildPictureResult(updatefilename, uploadPath, file, putObjectResult);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 封装返回结果
     * @param updatefilename
     * @param uploadPath
     * @param file
     * @param putObjectResult
     * @return
     */
    private static UploadPictureResult buildPictureResult(String updatefilename, String uploadPath, File file, PutObjectResult putObjectResult) {
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        double scale = NumberUtil.round(imageInfo.getWidth() * 1.0 / imageInfo.getHeight(), 2).doubleValue();
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(FileConstant.COS_HOST + uploadPath);
        uploadPictureResult.setPicName(updatefilename);
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(imageInfo.getWidth());
        uploadPictureResult.setPicHeight(imageInfo.getHeight());
        uploadPictureResult.setPicScale(scale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        return uploadPictureResult;
    }

    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validSource(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;




    /**
     * 清理临时文件
     *
     * @param file
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
    }
