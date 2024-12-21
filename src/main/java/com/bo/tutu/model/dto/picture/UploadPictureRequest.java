package com.bo.tutu.model.dto.picture;

import java.io.Serializable;
import lombok.Data;

/**
 * 文件上传请求
 *
 * @author <a href="https://github.com/JavaBo14">Bo</a>
 * @from <a href="https://github.com/JavaBo14/BoTuTu-Backend">
 */
@Data
public class UploadPictureRequest implements Serializable {

    /**
     * 业务
     */
    private String biz;

    private static final long serialVersionUID = 1L;
}