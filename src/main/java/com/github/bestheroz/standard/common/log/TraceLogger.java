package com.github.bestheroz.standard.common.log;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TraceLogger {
  private static final String STR_START_EXECUTE_TIME = "{} START .......";
  private static final String STR_END_EXECUTE_TIME = "{} E N D [{}ms] - return: {}";
  private static final String STR_END_EXECUTE_TIME_FOR_REPOSITORY = "{} E N D [{}ms]";
  private static final String STR_END_EXECUTE_TIME_FOR_EXCEPTION = "{} THROW [{}ms]";
  private final ObjectMapper objectMapper;

  @Around(
      """
      execution(!private * com.github.bestheroz..*Controller.*(..)) ||
      execution(!private * com.github.bestheroz..*Service.*(..)) ||
      execution(!private * com.github.bestheroz..*Repository.*(..))
      """)
  public Object writeLog(final ProceedingJoinPoint pjp) throws Throwable {
    final Object retVal;

    final String signature =
        pjp.getStaticPart()
            .getSignature()
            .toString()
            .replace(
                pjp.getStaticPart().getSignature().getDeclaringType().getPackageName().concat("."),
                "");
    if (signature.contains("HealthController") || signature.contains("HealthRepository")) {
      return pjp.proceed();
    }

    final StopWatch stopWatch = new StopWatch(signature);
    stopWatch.start();
    try {
      log.info(STR_START_EXECUTE_TIME, signature);

      retVal = pjp.proceed();

      stopWatch.stop();
      if (signature.contains("Repository.")
          || signature.contains("RepositoryCustom.")
          || signature.contains(".domain.")) {
        if (!signature.contains("HealthRepository")) {
          log.info(STR_END_EXECUTE_TIME_FOR_REPOSITORY, signature, stopWatch.getTotalTimeMillis());
        }
      } else {
        if (!signature.contains("HealthController")) {
          final String str = objectMapper.writeValueAsString(retVal);
          final String displayStr = Objects.toString(str, "null");
          final int strLen = str != null ? str.length() : 0;
          log.info(
              STR_END_EXECUTE_TIME,
              signature,
              stopWatch.getTotalTimeMillis(),
              displayStr.length() <= 1000
                  ? displayStr
                  : displayStr.substring(0, 1000)
                      + "--skip massive text-- total length : "
                      + strLen);
        }
      }
    } catch (final Throwable e) {
      if (stopWatch.isRunning()) {
        stopWatch.stop();
      }
      log.info(STR_END_EXECUTE_TIME_FOR_EXCEPTION, signature, stopWatch.getTotalTimeMillis());
      throw e;
    }
    return retVal;
  }
}
