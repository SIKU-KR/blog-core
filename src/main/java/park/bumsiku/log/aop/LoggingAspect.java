package park.bumsiku.log.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import park.bumsiku.log.LoggingConstants;
import park.bumsiku.log.performance.PerformanceConfig;

@Aspect
@Component
public class LoggingAspect extends AbstractLoggingAspect {

    public LoggingAspect(PerformanceConfig config) {
        super(config);
    }

    @Around("@annotation(park.bumsiku.log.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logExecutionTime(joinPoint, LoggingConstants.Operations.METHOD_EXECUTION);
    }
}