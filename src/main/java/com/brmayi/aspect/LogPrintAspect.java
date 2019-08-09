package com.brmayi.aspect;

import com.brmayi.annotation.LogNotPrintAnnotation;
import com.brmayi.annotation.LogPrintAnnotation;
import com.brmayi.pojo.BaseDTO;
import com.google.common.collect.Sets;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.LocationAwareLogger;
import org.springframework.core.annotation.Order;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Order(1)
public class LogPrintAspect {
    private static Logger LOGGER = LoggerFactory.getLogger(LogPrintAspect.class);
    private static final Pattern pattern = Pattern.compile("execution\\([\\w\\$]+\\s+([\\w\\$]+\\.)+([\\w\\$]+)\\([\\w\\$\\,]*\\)\\)");

    private static int LOGGER_ANNOTATION_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_annotation_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_MAPPER_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_mapper_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_DAO_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_dao_level", LocationAwareLogger.DEBUG_INT);
    private static int LOGGER_MANAGER_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_manager_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_API_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_api_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_CONTROLLER_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_controller_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_TASK_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_task_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_SERVICE_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_service_level", LocationAwareLogger.INFO_INT);
    private static int LOGGER_QUEUE_LEVEL = ConfigurationManager.getConfigInstance().getInt("logger_queue_level", LocationAwareLogger.INFO_INT);


    public LogPrintAspect() {
        LOGGER.info("------------------LogPrintAspect---start------------------------");
    }
    /**
     * 执行dao的日志的切面
     * 只会匹配com.meituan.hulk.*.dao下的所有类中的所有方法
     *
     * @param pjp 连接点
     * @return 方法返回对象
     * @throws Throwable
     */
    @Around(value = "execution(* *.dao..*.*(..))", argNames = "pjp")
    public Object printDaoLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_DAO_LEVEL);
    }

    @Around(value = "execution(* *.service..*.*(..))", argNames = "pjp")
    public Object printServiceLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_SERVICE_LEVEL);
    }

    @Around(value = "execution(* *..controller..*.*(..))", argNames = "pjp")
    public Object printControllerLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_CONTROLLER_LEVEL);
    }

    @Around(value = "execution(* *..api..*.*(..))", argNames = "pjp")
    public Object printApiLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_API_LEVEL);
    }

    @Around(value = "execution(* *..mapper..*.*(..))", argNames = "pjp")
    public Object printMapperLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_MAPPER_LEVEL);
    }

    @Around(value = "execution(* *..task..*.*(..))", argNames = "pjp")
    public Object printTaskLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_TASK_LEVEL);
    }

    @Around(value = "execution(* *..queue..*.*(..))", argNames = "pjp")
    public Object printQueueLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_QUEUE_LEVEL);
    }

    @Around(value = "execution(* *..manager..*.*(..))", argNames = "pjp")
    public Object printManagerLog(final ProceedingJoinPoint pjp) throws Throwable {
        return printLog(pjp, null, LOGGER_MANAGER_LEVEL);
    }

    /**
     * 执行所有配置logPrintAnnotation标签的日志的切面
     *
     * @param pjp                连接点
     * @param logPrintAnnotation 注解对象
     * @return 方法返回对象
     * @throws Throwable
     */
    @Around(value = "(execution(* *(..)) && @annotation(logPrintAnnotation))", argNames = "pjp,logPrintAnnotation")
    public Object printLogForAnnotation(final ProceedingJoinPoint pjp, LogPrintAnnotation logPrintAnnotation) throws Throwable {
        return printLog(pjp, logPrintAnnotation, LOGGER_ANNOTATION_LEVEL);
    }

    private Object printLog(final ProceedingJoinPoint pjp, LogPrintAnnotation logPrintAnnotation, int loggerLevel) throws Throwable {
        MethodSignature joinPointObject = (MethodSignature) pjp.getSignature();
        Method method = joinPointObject.getMethod();
        boolean isPrint = !method.isAnnotationPresent(LogNotPrintAnnotation.class);

        Object business = null;
        String logPrefix = getPrintPrefixLog(pjp);
        long start = System.currentTimeMillis();
        try {
            //创建配置的排除参数序列
            Set<Integer> set = Sets.newHashSet();
            if (logPrintAnnotation != null && logPrintAnnotation.exceptParamIndex() != null && logPrintAnnotation.exceptParamIndex().length > 0) {
                for (Integer index : logPrintAnnotation.exceptParamIndex()) {
                    set.add(index);
                }
            }

            if(isPrint) printStartLog(pjp, logPrefix, set, loggerLevel);
            //执行方法
            business = pjp.proceed();
        } finally {
            if(isPrint) printEndLog(pjp, null, business, logPrefix, loggerLevel, start);
        }
        return business;
    }

    /**
     * 链接点pjp.toString() 简化日志打印前缀 类名.方法名
     *
     * @param pjp 链接点，默认是这种格式
     *            execution(List com.meituan.pay.payroute.dao.BankTypeMapper$Iface.getMobileBankType(BankTypePo))
     * @return 简化后的日志打印前缀
     */
    private String getPrintPrefixLog(ProceedingJoinPoint pjp) {
        String resStr = pjp.toString();
        StringBuilder patternStr = new StringBuilder();
        //正则匹配相关的字符串
        if (StringUtils.isNotBlank(resStr)) {
            Matcher matcher = pattern.matcher(resStr);
            if (matcher.find()) {
                int count = matcher.groupCount();
                //拼装组装好的字符串
                for (int i = 1; i <= count; i++) {
                    patternStr.append(matcher.group(i));
                }
            }
            if (StringUtils.isNotBlank(patternStr.toString())) {
                resStr = patternStr.toString();
            }
        }
        return resStr;
    }


    /**
     * 打印方法开始时的输入参数序列
     *
     * @param pjp       连接点对象
     * @param logPrefix 日志方法前缀（默认是类名.方法名）
     * @param set       排除的对象的索引set
     */
    private static void printStartLog(ProceedingJoinPoint pjp, String logPrefix, Set<Integer> set, Integer levelint) {
        Object[] params = pjp.getArgs();
        String msg = null;
        if (params != null && params.length > 0) {
            StringBuilder str = new StringBuilder();
            String remoteAddr = "";
            for (int i = 0; i < params.length; i++) {
                //如果是排除的参数，跳过打印
                if (set != null && !set.isEmpty() && set.contains(i)) {
                    continue;
                }
                Object param = params[i];
                if (param != null && param instanceof BaseDTO) {
                    MDC.put("reqId", ((BaseDTO) param).getReqId());
                }
                if (param != null && !(param instanceof ServletRequest || param instanceof ServletResponse)) {
                    str.append(param.toString());
                }
                if (param != null && (param instanceof ServletRequest)) {
                    remoteAddr = ((ServletRequest) param).getRemoteAddr();
                }
            }
            msg = logPrefix + "-" + remoteAddr + "-[@@开始@@],入参:[" + str.toString() + "]";
        } else {
            msg = logPrefix + "-[@@开始@@]";
        }
        printLogEnum(LOGGER, levelint, msg);
    }

    /**
     * 打印方法结束时的输出参数序列
     *
     * @param business  方法输出参数对象
     * @param logPrefix 日志方法前缀（默认是类名.方法名）
     */
    private static void printEndLog(final ProceedingJoinPoint pjp, Set<Integer> set, Object business, String logPrefix, Integer levelint, long start) {
        //打印日志结果的内容
        String data = null;
        Object[] params = pjp.getArgs();
        if (params != null && params.length > 0) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                //如果是排除的参数，跳过打印
                if (set != null && !set.isEmpty() && set.contains(i)) {
                    continue;
                }
                Object param = params[i];
                if (param != null && !(param instanceof ServletRequest || param instanceof ServletResponse)) {
                    str.append(param.toString());
                }
            }
            data = str.toString();
        }
        String msg;
        long cost = System.currentTimeMillis() - start;
        if (business != null) {
            msg = logPrefix + "-[@@结束@@],入参:[" + data + "],结果:[" + business + "],总耗时:[" + cost + "]";
        } else {
            msg = logPrefix + "-[@@结束@@],入参:[" + data + "],总耗时:[" + cost + "]";
        }
        printLogEnum(LOGGER, levelint, msg);
    }

    private static void printLogEnum(Logger logger, Integer levelInt, String info) {
        switch (levelInt.intValue()) {
            case LocationAwareLogger.TRACE_INT:
                logger.trace(info);
                break;
            case LocationAwareLogger.DEBUG_INT:
                logger.debug(info);
                break;
            case LocationAwareLogger.WARN_INT:
                logger.warn(info);
                break;
            case LocationAwareLogger.ERROR_INT:
                logger.error(info);
                break;
            default:
                logger.info(info);
        }
    }
}
