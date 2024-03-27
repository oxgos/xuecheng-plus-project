package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.feignclient.CheckCodeClient;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.dto.RegisterXcUserDto;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.RegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Resource
    XcUserMapper xcUserMapper;

    @Resource
    XcUserRoleMapper xcUserRoleMapper;

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

    @Transactional
    @Override
    public String register(RegisterXcUserDto registerXcUserDto) {
        String checkcode = registerXcUserDto.getCheckcode();
        String checkcodekey = registerXcUserDto.getCheckcodekey();
        // 校验验证码
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if (!verify) {
            XueChengPlusException.cast("验证码不正确");
        }
        String cellphone = registerXcUserDto.getCellphone();
        String email = registerXcUserDto.getEmail();
        XcUser user1 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getCellphone, cellphone));
        if (user1 != null) {
            XueChengPlusException.cast("该手机号已注册");
        }
        if (StringUtils.isNotEmpty(email)) {
            XcUser user2 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getEmail, email));
            if (user2 != null) {
                XueChengPlusException.cast("该邮箱已注册");
            }
        }

        String username = registerXcUserDto.getUsername();
        XcUser user3 = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username));
        if (user3 != null) {
            XueChengPlusException.cast("该账号已注册");
        }
        String password = registerXcUserDto.getPassword();
        String confirmpwd = registerXcUserDto.getConfirmpwd();
        if (!password.equals(confirmpwd)) {
            XueChengPlusException.cast("两次密码不一致");
        }
        String encode = new BCryptPasswordEncoder().encode(password);
        String nickname = registerXcUserDto.getNickname();
        LocalDateTime now = LocalDateTime.now();
        String userId = UUID.randomUUID().toString();
        XcUser xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setUtype("101001");
        xcUser.setCellphone(cellphone);
        xcUser.setEmail(email);
        xcUser.setName(nickname);
        xcUser.setNickname(nickname);
        xcUser.setUsername(username);
        xcUser.setPassword(encode);
        xcUser.setCreateTime(now);
        xcUser.setStatus("1");
        int insert = xcUserMapper.insert(xcUser);
        if (insert <= 0) {
            XueChengPlusException.cast("注册失败");
        }
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setRoleId("17");
        xcUserRole.setUserId(userId);
        xcUserRole.setCreateTime(now);
        int insert1 = xcUserRoleMapper.insert(xcUserRole);
        if (insert1 <= 0) {
            XueChengPlusException.cast("注册失败");
        }
        return "注册成功";
    }
}
