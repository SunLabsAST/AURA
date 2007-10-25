
import com.sun.labs.util.props.Configurable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import com.sun.labs.util.props.Registry;

/**
 *
 * @author plamere
 */
public class FeedCrawler implements Configurable {
    private String name;

    public FeedCrawler() {

    }
    public void register(String name, Registry registry) throws PropertyException {
        this.name = name;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName() {
        return name;
    }

    public void start() {
    }

    public void stop() {
    }
    
    public void run() {
    }
}
