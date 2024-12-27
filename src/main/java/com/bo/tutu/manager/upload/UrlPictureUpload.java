package com.bo.tutu.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.bo.tutu.common.ErrorCode;
import com.bo.tutu.exception.BusinessException;
import com.bo.tutu.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected String validSource(Object inputSource) {
        String fileUrl= (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");
        try {
            // 1. 验证 URL 格式
            new URL(fileUrl); // 验证是否是合法的 URL
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // 2. 校验 URL 协议
        ThrowUtils.throwIf(!(fileUrl.startsWith("http://") || fileUrl.startsWith("https://")),
                ErrorCode.PARAMS_ERROR, "仅支持 HTTP 或 HTTPS 协议的文件地址");
        // 3. 发送 HEAD 请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return null;
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long TWO_MB = 2 * 1024 * 1024L; // 限制文件大小为 2MB
                    ThrowUtils.throwIf(contentLength > TWO_MB, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
                return contentType;
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型无法识别");
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        try {
            // 提取最后一个 `/` 后的部分
            String lastPart = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            // 检查最后一部分是否有文件扩展名
            if (lastPart.matches(".*\\.(jpg|png|jpeg|gif|bmp|webp)$")) {
                // 如果有扩展名，使用 FileUtil 提取主文件名
                return FileUtil.mainName(lastPart);
            }
            // 判断是否包含特殊字符
            if (lastPart.matches(".*[\\-,&].*")) {
                // 如果包含特殊字符，提取最后一个 `/` 前的部分
                String beforeLastSlash = fileUrl.substring(0, fileUrl.lastIndexOf("/"));
                String substring = beforeLastSlash.substring(beforeLastSlash.lastIndexOf("/") + 1);
                return substring;
            } else {
                // 如果不包含特殊字符，直接返回最后部分
                return lastPart;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl= (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }
}
