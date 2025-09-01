package park.bumsiku.utils.monitoring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodLoggingAspect extends AbstractLoggingAspect {

    private static final long SLOW_METHOD_THRESHOLD_MS = 500;

    @Around("@annotation(park.bumsiku.utils.monitoring.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logExecutionTime(joinPoint, "method execution", SLOW_METHOD_THRESHOLD_MS);
    }
}