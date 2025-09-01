package park.bumsiku.log.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import park.bumsiku.log.LoggingConstants;
import park.bumsiku.log.performance.PerformanceConfig;

@Aspect
@Component
public class RepositoryLoggingAspect extends AbstractLoggingAspect {

    public RepositoryLoggingAspect(PerformanceConfig config) {
        super(config);
    }

    @Around("execution(* park.bumsiku.repository..*.*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logExecutionTime(joinPoint, LoggingConstants.Operations.DB_QUERY);
    }
} 