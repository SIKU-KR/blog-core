package park.bumsiku.log.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import park.bumsiku.log.performance.PerformanceConfig;
import park.bumsiku.log.performance.PerformanceMonitor;

public abstract class AbstractLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLoggingAspect.class);
    protected final PerformanceMonitor performanceMonitor;

    protected AbstractLoggingAspect(PerformanceConfig config) {
        this.performanceMonitor = new PerformanceMonitor(logger, config);
    }

    protected Object logExecutionTime(ProceedingJoinPoint joinPoint, String operation) throws Throwable {
        long startTime = System.nanoTime();
        Object result = joinPoint.proceed();
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        performanceMonitor.logIfSlow(duration, className, methodName, operation);

        return result;
    }
}