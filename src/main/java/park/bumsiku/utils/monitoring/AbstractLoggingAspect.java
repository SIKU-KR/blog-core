package park.bumsiku.utils.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractLoggingAspect {

    protected Object logExecutionTime(ProceedingJoinPoint joinPoint, String operation, long thresholdMs) throws Throwable {
        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        if (durationMs > thresholdMs) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = signature.getMethod().getName();
            String requestId = MDC.get(LoggingFilter.MdcUtils.KEY_REQUEST_ID);

            log.warn("Slow {} detected - RequestId: {}, {}.{} finished in {} ms",
                    operation, requestId, className, methodName, durationMs);
        }

        return result;
    }
}