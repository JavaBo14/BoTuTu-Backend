package com.bo.tutu.utils;

public class ContentTypeUtil {

    /**
     * 根据文件后缀名返回 Content-Type
     * 
     * @param suffix 文件后缀
     * @return Content-Type 字符串
     */
    public static String getContentTypeBySuffix(String suffix) {
        if (suffix == null) {
            return "application/octet-stream"; // 默认值
        }

        switch (suffix.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "tiff":
                return "image/tiff";
            case "svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream"; // 默认类型
        }
    }
}
