import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class ConsumeProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {

      String routeId =exchange.getFromRouteId();
        System.out.println("nombre de la ruta es:" +routeId);



    }
}