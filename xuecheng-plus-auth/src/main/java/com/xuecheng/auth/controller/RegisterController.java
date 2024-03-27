package com.xuecheng.auth.controller;

import com.xuecheng.auth.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.model.dto.RegisterXcUserDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description 注册
 */
@Slf4j
@RestController
public class RegisterController {

    @Resource
    RegisterService registerService;

    @PostMapping("/register")
    public String register(@RequestBody @Validated RegisterXcUserDto registerXcUserDto) {
        return registerService.register(registerXcUserDto);
    }


    @PostMapping("/findpassword")
    public String findPassword(@RequestBody @Validated FindPasswordParamsDto findPasswordParamsDto) {
        return registerService.findPassword(findPasswordParamsDto);
    }

}
