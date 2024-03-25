package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Resource
    XcMenuMapper xcMenuMapper;

    @Resource
    ApplicationContext applicationContext;

    /**
     * @param s 账号信息
     * @return org.springframework.security.core.userdetails.UserDetails
     * @description 根据账号查询用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try {
            // 将认证参数转为AuthParamsDto类型
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            log.info("认证请求不符合项目要求:{}", s);
            throw new RuntimeException("认证请求数据格式不对");
        }
        String authType = authParamsDto.getAuthType();

        if (authType == null) {
            throw new RuntimeException("认证请求缺少校验方式参数authType");
        }
        // 利用策略模式，根据authType获取不同的ServiceBean
        AuthService authService = applicationContext.getBean(authType + "_authService", AuthService.class);

        XcUserExt user = authService.execute(authParamsDto);

        return getUserPrincipal(user);
    }

    /**
     * @param user 用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @description 查询用户信息
     */
    public UserDetails getUserPrincipal(XcUserExt user) {
        String password = user.getPassword();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList();
        if (xcMenus.size() == 0) {
            //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
            permissions.add("p1");
        } else {
            xcMenus.stream().forEach(xcMenu -> {
                permissions.add(xcMenu.getCode());
            });
        }
        // 将用户权限放在XcUserExt中
        user.setPermissions(permissions);

        // 为了安全在令牌中不放密码
        user.setPassword(null);
        // 将user对象转json
        String userString = JSON.toJSONString(user);
        String[] authorities = permissions.toArray(new String[0]);
        // 创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }
}