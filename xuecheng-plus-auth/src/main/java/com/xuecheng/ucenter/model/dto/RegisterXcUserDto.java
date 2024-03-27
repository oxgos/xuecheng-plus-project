package com.xuecheng.ucenter.model.dto;

import com.xuecheng.auth.utils.CheckCellphoneOrEmailphone;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class RegisterXcUserDto {

    @NotEmpty(message = "手机号码不能为空")
    private String cellphone;

    @NotEmpty(message = "账号不能为空")
    private String username;

    private String email;

    @NotEmpty(message = "昵称不能为空")
    private String nickname;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "确认密码不能为空")
    private String confirmpwd;

    @NotEmpty(message = "请获取验证码")
    private String checkcodekey;

    @NotEmpty(message = "验证码不能为空")
    private String checkcode;
}
