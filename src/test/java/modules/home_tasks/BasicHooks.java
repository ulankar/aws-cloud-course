package modules.home_tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.text.SimpleDateFormat;
import java.util.Date;

import static modules.home_tasks.PropertyHandler.setPropertiesForEnvironment;


public abstract class BasicHooks {

    private static final Logger logger = LogManager.getLogger(BasicHooks.class);
    public static String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    @BeforeAll
    public static void setUp(){
        setPropertiesForEnvironment();
        logger.info("\n============TEST CLASS EXECUTION STARTED============");
    }

    @AfterAll
    public static void tearDown(){
        logger.info("\n============TEST CLASS EXECUTION FINISHED============");
    }
}
