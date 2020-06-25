package io.renren.common.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
public class ControllerAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    @Pointcut("execution(* io.renren.modules.*.controller.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {

    }

    @AfterReturning(value = "pointcut()", returning = "ret")
    public void doAfterReturning(Object ret) throws Throwable {

    }

    @Around("pointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String remoteAddr = request.getRemoteAddr();//ip
        String methodName = this.getMethodName(joinPoint);//方法名
        StringBuffer params = new StringBuffer();
        for (int i = 0; i < joinPoint.getArgs().length; i++) {
            Object argument = joinPoint.getArgs()[i];
            if (joinPoint.getArgs()[i] instanceof ServletRequest || joinPoint.getArgs()[i] instanceof ServletResponse || joinPoint.getArgs()[i] instanceof MultipartFile) {
                continue;
            }
            params.append(argument);
        }
        MethodSignature msig = (MethodSignature) joinPoint.getSignature();
        Method pointMethod = joinPoint.getTarget().getClass().getMethod(msig.getName(), msig.getParameterTypes());
        String methodPath = String.format("%s.%s", joinPoint.getSignature().getDeclaringTypeName(), pointMethod.getName());
        logger.info("接口开始请求方法:[{}] 服务:[{}] 参数:[{}] IP:[{}] userAgent [{}]", methodName,methodPath,params.toString(), remoteAddr);
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        logger.info("接口结束请求方法:[{}] 参数:[{}] 返回结果[{}] 耗时:[{}]毫秒 ",
                methodName,params.toString(), JSON.toJSONString(result), end - start);
        return result;
    }

    private String getMethodName(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        String shortMethodNameSuffix = "(..)";
        if (methodName.endsWith(shortMethodNameSuffix)) {
            methodName = methodName.substring(0, methodName.length() - shortMethodNameSuffix.length());
        }
        return methodName;
    }

}
