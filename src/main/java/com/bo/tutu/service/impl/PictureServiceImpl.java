package com.bo.tutu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bo.tutu.common.ErrorCode;
import com.bo.tutu.constant.CommonConstant;
import com.bo.tutu.exception.ThrowUtils;
import com.bo.tutu.manager.FileManager;
import com.bo.tutu.mapper.PictureMapper;
import com.bo.tutu.model.dto.picture.PictureQueryRequest;
import com.bo.tutu.model.dto.picture.PictureUploadRequest;
import com.bo.tutu.model.entity.Picture;
import com.bo.tutu.model.entity.User;
import com.bo.tutu.model.vo.PictureVO;
import com.bo.tutu.model.vo.UploadPictureResult;
import com.bo.tutu.model.vo.UserVO;
import com.bo.tutu.service.PictureService;
import com.bo.tutu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Bo
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2024-12-19 10:18:38
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{
    @Resource
    private FileManager fileManager;
    @Resource
    private UserService userService;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {

        //参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //判断是否为更新图片
        Long pictureId = null;
        // TODO: 2024/12/21  
        if (pictureUploadRequest != null){
            pictureId=pictureUploadRequest.getId();
        }
        Picture picture=new Picture();
        if (pictureId != null){
            boolean exists = this.lambdaQuery()
                    .eq(Picture::getId, pictureId)
                    .exists();
            ThrowUtils.throwIf(!exists,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        String uploadPathPrefix=String.format("public/%s",loginUser.getId());
        UploadPictureResult uploadPictureResult = fileManager.uploadPictureResult(multipartFile, uploadPathPrefix);
        //拷贝
        BeanUtil.copyProperties(uploadPictureResult,picture);
        picture.setName(uploadPictureResult.getPicName());
        picture.setUserId(loginUser.getId());
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败");
        return PictureVO.objToVo(picture);
    }

    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null,ErrorCode.PARAMS_ERROR,"图片为空");
        Long id = picture.getId();
        String url = picture.getUrl();
        String name = picture.getName();
        String introduction = picture.getIntroduction();
        String picFormat = picture.getPicFormat();
        ThrowUtils.throwIf(ObjUtil.isNull(id),ErrorCode.NOT_FOUND_ERROR,"id不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(name)) {
            ThrowUtils.throwIf(name.length() > 20, ErrorCode.PARAMS_ERROR, "图片名称过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
        String[] validFormats = {"JPEG", "PNG", "GIF", "BMP", "TIFF"};
        picFormat = picFormat.toUpperCase();
        // 使用Arrays类的binarySearch方法来检查picFormat是否在有效格式列表中
        if (Arrays.binarySearch(validFormats, picFormat) >= 0) {
            System.out.println("图片格式有效");
        } else {
            System.out.println("图片格式无效");
        }
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        if (StringUtils.isNotBlank(searchText)){
            queryWrapper.and(qw ->qw.like(StringUtils.isNotBlank(name),"name",name)
                        .or().like(StringUtils.isNotBlank(introduction), "introduction", introduction)
            );
        }
        // TODO: 2024/12/21
        if (CollectionUtil.isNotEmpty(tags)){
            for (String tag:tags){
                queryWrapper.like("tags","\""+tag+"\"");
            }
        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.eq(StringUtils.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjectUtil.isNotEmpty(picFormat), "picFormat", picFormat);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return null;
    }

    @Override
    public PictureVO getPictureVo(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();
        if (userId != null && userId >0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 分页获取图片封装
     */

    // TODO: 2024/12/21  
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

}




