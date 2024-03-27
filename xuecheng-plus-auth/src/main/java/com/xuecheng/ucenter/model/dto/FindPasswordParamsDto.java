package com.xuecheng.ucenter.model.dto;

import com.xuecheng.auth.utils.CheckCellphoneOrEmailphone;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description 认证用户请求参数
 */
@Data
@CheckCellphoneOrEmailphone(message = "手机号和邮箱至少填写一个")
public class FindPasswordParamsDto {

    // 手机号
    private String cellphone;

    // 邮箱
    private String emailphone;

    @NotEmpty(message = "验证码不能为空")
    private String checkcode;

    @NotEmpty(message = "验证码KEY不能为空")
    private String checkcodekey;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmpwd;
}
