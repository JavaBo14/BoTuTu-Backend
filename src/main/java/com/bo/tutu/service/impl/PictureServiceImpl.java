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
import com.bo.tutu.exception.BusinessException;
import com.bo.tutu.exception.ThrowUtils;
import com.bo.tutu.manager.FileManager;
import com.bo.tutu.manager.upload.FilePictureUpload;
import com.bo.tutu.manager.upload.PictureUploadTemplate;
import com.bo.tutu.manager.upload.UrlPictureUpload;
import com.bo.tutu.mapper.PictureMapper;
import com.bo.tutu.model.dto.picture.PictureQueryRequest;
import com.bo.tutu.model.dto.picture.PictureReviewRequest;
import com.bo.tutu.model.dto.picture.PictureUploadRequest;
import com.bo.tutu.model.entity.Picture;
import com.bo.tutu.model.entity.User;
import com.bo.tutu.model.enums.PictureReviewEnum;
import com.bo.tutu.model.enums.UserRoleEnum;
import com.bo.tutu.model.vo.PictureVO;
import com.bo.tutu.model.vo.UploadPictureResult;
import com.bo.tutu.model.vo.UserVO;
import com.bo.tutu.service.PictureService;
import com.bo.tutu.service.UserService;
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

    @Resource
    private UrlPictureUpload urlPictureUpload;
    @Resource
    private FilePictureUpload filePictureUpload;

    /**
     * 上传图片
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {

        //参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        //判断是否为更新图片
        Long pictureId = null;
        // TODO: 2024/12/21  在 Spring 的控制器方法中，如果方法参数是一个类（如 PictureUploadRequest），即使前端没有传递参数，Spring 会默认实例化这个类并注入。
        // TODO: 2024/12/23 这种行为使得 pictureUploadRequest 不会为 null，而是一个默认的空对象（所有字段为 null）防御性编程和代码的健壮性，避免潜在的 NullPointerException
        if (pictureUploadRequest != null){
            pictureId=pictureUploadRequest.getId();
        }
        Picture picture=new Picture();
        //图片自动审核
        fillReviewParams(picture, loginUser);
        if (pictureId != null){
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null,ErrorCode.NOT_FOUND_ERROR,"图片不存在");
//            boolean exists = this.lambdaQuery()
//                    .eq(Picture::getId, pictureId)
//                    .exists();
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }
        //上传Cos对象存储
        String uploadPathPrefix=String.format("public/%s",loginUser.getId());
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        //用于检查 inputSource 对象是否是 String 类型
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPictureResult(inputSource, uploadPathPrefix);
        //拷贝
        BeanUtil.copyProperties(uploadPictureResult,picture);
        picture.setName(uploadPictureResult.getPicName());
        picture.setUserId(loginUser.getId());
        boolean result = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR,"图片上传失败");
        return PictureVO.objToVo(picture);
    }

    /**
     * 图片校验
     * @param picture
     */
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

    /**
     * 获取查询条件
     * @param pictureQueryRequest
     * @return
     */
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
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        if (StrUtil.isNotBlank(searchText)){
            queryWrapper.and(qw ->qw.like(StrUtil.isNotBlank(name),"name",name)
                        .or().like(StrUtil.isNotBlank(introduction), "introduction", introduction)
            );
        }
        // TODO: 2024/12/21
        if (CollectionUtil.isNotEmpty(tags)){
            for (String tag:tags){
                queryWrapper.like("tags","\""+tag+"\"");
            }
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(picFormat), "picFormat", picFormat);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 添加用户信息
     * @param picture
     * @param request
     * @return
     */
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

    // TODO: 2024/12/21  !!!
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
                //get(0)如果一个用户 ID 对应多个用户记录（理论上不应该发生，但代码需要防御性编程）
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     * @return
     */
    @Override
    public Boolean doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        Long userId = loginUser.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewEnum reviewStatusEnum = PictureReviewEnum.getEnumByValue(reviewStatus);
        if (userId == null || reviewStatus == null || PictureReviewEnum.REVIEWING.equals(reviewStatusEnum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //判断是否存在
        Picture oldPicture = this.getById(pictureReviewRequest.getId());
        ThrowUtils.throwIf(oldPicture == null,ErrorCode.OPERATION_ERROR,"图片不存在");
        //不能重复审核
        if (oldPicture.getReviewStatus().equals(reviewStatus)){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"不能重复审核");
        }
        //操作数据库
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest,picture);
        picture.setUserId(loginUser.getId());
        picture.setReviewTime(new Date());
        boolean result = this.save(picture);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return result;
    }

    @Override
    public Boolean fillReviewParams(Picture picture, User loginUser) {
        String userRole = loginUser.getUserRole();
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
        if (UserRoleEnum.ADMIN.equals(userRoleEnum)){
            picture.setReviewTime(new Date());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewStatus(PictureReviewEnum.PASS.getValue());
        }else{
            picture.setReviewStatus(PictureReviewEnum.REVIEWING.getValue());
        }
        return true;
    }
}




