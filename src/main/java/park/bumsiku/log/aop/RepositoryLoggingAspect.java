package park.bumsiku.log.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RepositoryLoggingAspect extends AbstractLoggingAspect {

    private static final long SLOW_DB_QUERY_THRESHOLD_MS = 100;

    @Around("execution(* park.bumsiku.repository..*.*(..))")
    public Object logRepositoryExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logExecutionTime(joinPoint, "DB query", SLOW_DB_QUERY_THRESHOLD_MS);
    }
} 