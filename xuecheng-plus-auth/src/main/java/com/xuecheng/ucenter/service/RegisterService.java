package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;

public interface RegisterService {

    /**
     * 找回密码
     * @param findPasswordParamsDto
     * @return
     */
    public String findPassword(FindPasswordParamsDto findPasswordParamsDto);

}
