package retryTest;


import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.security.PrivateKey;

public class Retry implements IRetryAnalyzer {

    private int retryCount =0 ;
    private int maxRetry   =3 ;


    @Override
    public boolean retry(ITestResult iTestResult) {
        if (retryCount < maxRetry)
        {
            retryCount ++ ;
            return  true ;
        }
        return false;
    }
}
