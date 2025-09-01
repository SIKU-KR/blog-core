package park.bumsiku.log.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect extends AbstractLoggingAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 500;

    @Around("@annotation(park.bumsiku.log.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logExecutionTime(joinPoint, "method execution", SLOW_METHOD_THRESHOLD_MS);
    }
}