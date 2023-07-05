package kafkaeventdriven.kafkaapp.exceptionHandler;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandlerUnchecked {

    private Logger logger = LoggerFactory.getLogger(ExceptionHandlerUnchecked.class);


    public static RuntimeException handlerException(Exception exception) {
        ExceptionHandlerUnchecked.<RuntimeException>throwTheException(exception);

        throw new AssertionError("error");
    }

    private static <T extends Exception> void throwTheException(Exception toThrow) throws T{
        throw (T) toThrow;
    }

}
