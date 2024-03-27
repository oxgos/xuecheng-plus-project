package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.feignclient.CheckCodeClient;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.RegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    CheckCodeClient checkCodeClient;


    @Override
    public String findPassword(FindPasswordParamsDto findPasswordParamsDto) {
        String checkcodekey = findPasswordParamsDto.getCheckcodekey();
        String checkcode = findPasswordParamsDto.getCheckcode();
        // 校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) {
            XueChengPlusException.cast("验证码不正确");
        }
        String cellphone = findPasswordParamsDto.getCellphone();
        String emailphone = findPasswordParamsDto.getEmailphone();
        XcUser user;
        if (StringUtils.isNotEmpty(cellphone)) {
            user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getCellphone, cellphone));
            if (user == null) {
                XueChengPlusException.cast("账号不存在");
            }
        } else {
            user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, emailphone));
            if (user == null) {
                XueChengPlusException.cast("账号不存在");
            }
        }

        String password = findPasswordParamsDto.getPassword();
        String confirmpwd = findPasswordParamsDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengPlusException.cast("两次密码不一致");
        }
        String encode = new BCryptPasswordEncoder().encode(password);
        user.setPassword(encode);
        int i = xcUserMapper.updateById(user);
        if (i == 0) {
            XueChengPlusException.cast("找回密码失败");
        }
        return "找回密码成功";
    }
}
