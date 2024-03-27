package com.xuecheng.auth.utils;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CheckCellphoneOrEmailphoneValidator implements ConstraintValidator<CheckCellphoneOrEmailphone, FindPasswordParamsDto> {

    @Override
    public boolean isValid(FindPasswordParamsDto dto, ConstraintValidatorContext context) {
        return StringUtils.isNotBlank(dto.getCellphone()) || StringUtils.isNotBlank(dto.getEmailphone());
    }
}
