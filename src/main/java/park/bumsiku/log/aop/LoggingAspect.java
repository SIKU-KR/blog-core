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
public class LoggingAspect {

    private static final long WARN_EXECUTION_TIME_MS = 500;

    @Around("@annotation(park.bumsiku.log.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();
        long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        if (duration > WARN_EXECUTION_TIME_MS) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = signature.getMethod().getName();
            String requestId = MDC.get(MdcUtils.KEY_REQUEST_ID);

            log.warn("Slow method execution detected - RequestId: {}, {}.{} finished in {} ms",
                    requestId, className, methodName, duration);
        }

        return result;
    }
}