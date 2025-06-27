package park.bumsiku.log.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import park.bumsiku.log.MdcUtils;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RepositoryLoggingAspect {

    @Around("execution(* park.bumsiku.repository..*.*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();
        String requestId = MDC.get(MdcUtils.KEY_REQUEST_ID);

        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();

        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        // 결과가 너무 길 경우를 대비하여 간단히 타입만 로깅
        String resultType = (result != null) ? result.getClass().getSimpleName() : "null";
        log.info("AOP DB - RequestId: {}, {}.{} finished in {} ms. Result type: {}", requestId, className, methodName, duration, resultType);

        return result;
    }
} 