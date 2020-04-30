import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WIN10 .
 * @create 2020-04-30-11:57 .
 * @description .
 */
@RestController
@RefreshScope
public class configController {

    @Value("${server.port}")
    String port;

    @Value("${myconfig}")
    String myconfig;


    @GetMapping
    public String getConfig() {
        return myconfig;
    }
}
