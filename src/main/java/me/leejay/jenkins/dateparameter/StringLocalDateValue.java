package me.leejay.jenkins.dateparameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Created by JuHyunLee on 2017. 6. 2..
 */
public class StringLocalDateValue implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(StringLocalDateValue.class);

    private static final long serialVersionUID = 8295455815421939737L;

    //private final String JAVA_PATTERN = "^LocalDate(Time)?\\.now\\(\\)(\\.(plus|minus)(Seconds|Minutes|Hours|Days|Months|Years)\\([0-9]+\\))*;?$";
    private final String JAVA_PATTERN = "^LocalDate(Time)?\\.now\\(\\)(\\.atStartOfDay\\(\\))?(\\.(plus|minus)(Seconds|Minutes|Hours|Days|Months|Years)\\([0-9]+\\))*;?$";

    private final String stringLocalDate;

    private final String stringDateFormat;

    public StringLocalDateValue(String stringLocalDate, String stringDateFormat) {
        this.stringLocalDate = stringLocalDate;
        this.stringDateFormat = stringDateFormat;
    }

    public String getStringLocalDate() {
        return stringLocalDate;
    }

    public String getStringValue() {
        return stringLocalDate;
    }

    public boolean isCompletionFormat() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringDateFormat);
        try {
            LocalDateTime.parse(stringLocalDate, formatter);
            return true;
        }
        catch (DateTimeParseException e)
        {
            try
            {
                LocalDate.parse(stringLocalDate, formatter);
                return true;
            }
            catch (Exception ex)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean isJavaFormat() {
        return stringLocalDate.matches(JAVA_PATTERN);
    }

    public String getStringDateFormat() {
        return stringDateFormat;
    }

    LocalDateTime parseJava() {
        List<String> codes = Arrays.asList(stringLocalDate.split("\\."));
        if (codes.size() == 2) { // LocalDate.now();
            if (stringLocalDate.matches("^LocalDate(Time)?\\.now\\(\\);?$")) {
                return LocalDateTime.now();
            }
            return null;
        }


        LocalDateTime localDateTime = LocalDateTime.now();
        int i = 2;
        if (codes.get(i).equals("atStartOfDay()"))
        {
            localDateTime = localDateTime.toLocalDate().atStartOfDay();
            ++i;
        }
        for (String code : codes.subList(i, codes.size())) {
            IntegerParamMethod paramMethod = new IntegerParamMethod(code);
            if (paramMethod.getName() == null || paramMethod.getParameter() == null) {
                return null;
            }

            try {
                Method method = localDateTime.getClass().getMethod(paramMethod.getName(), long.class);
                localDateTime = (LocalDateTime) method.invoke(localDateTime, paramMethod.getParameter());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return null;
            }
        }

        return localDateTime;
    }

    String getValue() {
        if (isCompletionFormat()) {
            return stringLocalDate;
        }

        if (isJavaFormat()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringDateFormat);
            return parseJava().format(formatter);
        }

        return "";
    }

}
