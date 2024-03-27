package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.dto.RegisterXcUserDto;

public interface RegisterService {

    /**
     * 找回密码
     * @param findPasswordParamsDto
     * @return
     */
    public String findPassword(FindPasswordParamsDto findPasswordParamsDto);

    /**
     * 注册
     * @param registerXcUserDto
     * @return
     */
    public String register(RegisterXcUserDto registerXcUserDto);
}
