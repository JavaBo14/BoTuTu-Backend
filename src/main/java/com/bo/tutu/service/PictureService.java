package com.bo.tutu.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bo.tutu.model.dto.picture.PictureQueryRequest;
import com.bo.tutu.model.dto.picture.PictureUploadRequest;
import com.bo.tutu.model.dto.user.UserQueryRequest;
import com.bo.tutu.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bo.tutu.model.entity.User;
import com.bo.tutu.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author Bo
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2024-12-19 10:18:38
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 图片校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 添加用户信息
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVo(Picture picture,HttpServletRequest request);

    /**
     * 分页获取图片封装
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

}
