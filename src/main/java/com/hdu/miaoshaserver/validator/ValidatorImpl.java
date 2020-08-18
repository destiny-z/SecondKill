package com.hdu.miaoshaserver.validator;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class ValidatorImpl implements InitializingBean {

    private Validator validator;

    //实现校验方法并返回校验结果
    public ValidationResult validate(Object bean) {
        final ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constrainViolationSet = validator.validate(bean);
        if (constrainViolationSet.size() > 0) {
            //有错误
            result.setHasErrors(true);
            constrainViolationSet.forEach(ConstraintViolation -> {
                String errMsg = ConstraintViolation.getMessage();
                String propertyName = ConstraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName, errMsg);
            });
        }
        return result;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //通过hibernate validator通过工厂的初始化方式使其实例化
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
}
