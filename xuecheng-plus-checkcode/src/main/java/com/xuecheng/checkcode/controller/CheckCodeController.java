package com.xuecheng.checkcode.controller;

import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.CheckCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description 验证码服务接口
 */
@Api(value = "验证码服务接口")
@RestController
public class CheckCodeController {

    @Resource(name = "CheckCodeService")
    private CheckCodeService checkCodeService;


    @ApiOperation(value = "生成验证信息", notes = "生成验证信息")
    @PostMapping(value = "/phone")
    public CheckCodeResultDto generatePhoneCheckCode(CheckCodeParamsDto checkCodeParamsDto) {
        String param1 = checkCodeParamsDto.getParam1();
        CheckCodeResultDto checkCodeResultDto;
//        if (StringUtils.isEmpty(param1)) {
//            checkCodeResultDto = new CheckCodeResultDto();
//            checkCodeResultDto.setAliasing("手机或者邮箱不能为空");
//            return checkCodeResultDto;
//        }
        if (param1.contains("@")) {
            checkCodeResultDto = checkCodeService.generateEmailCode(checkCodeParamsDto);
        } else {
            checkCodeResultDto = checkCodeService.generatePhoneCode(checkCodeParamsDto);
        }
        return checkCodeResultDto;
    }

    @ApiOperation(value = "发送验证码", notes = "发送验证码")
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto) {
        return checkCodeService.generateCheckCode(checkCodeParamsDto);
    }

    @ApiOperation(value = "校验", notes = "校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "业务名称", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "key", value = "验证key", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType = "query")
    })
    @PostMapping(value = "/verify")
    public Boolean verify(String key, String code) {
        Boolean isSuccess = checkCodeService.verify(key, code);
        return isSuccess;
    }
}
