import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ChoiceDefinition;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
public class TestCamel {

    private static int defaultTimeout = 1;

    public static void main(String[] args) throws Exception {
        // thread(new HelloWorldProducer(), false);
//        thread(new HelloWorldConsumer(), false);
//        thread(new HelloWorldConsumer(), false);
        try {
            CamelContext context = new DefaultCamelContext();
            ConsumeProcessor consumeProcessor = new ConsumeProcessor();
            ErrorProcessor errorProcessor = new ErrorProcessor();
            context.addComponent("activemq", ActiveMQComponent.activeMQComponent("tcp://127.0.0.1:61616"));
            context.addComponent("activemq2", ActiveMQComponent.activeMQComponent("tcp://127.0.0.1:61616"));
            context.addRoutes(createErrorRoute(errorProcessor));
            context.addRoutes(createRoute(errorProcessor));
            //context.getShutdownStrategy().setTimeout(60);
            context.addRoutes(createConsumerRoute(consumeProcessor));
            context.start();
            ProducerTemplate template = context.createProducerTemplate();
            try {
               camelSendMsg(template,"AT");
//               camelSendMsg(template,"BT");
//                camelSendMsg(template,"CT");
                TimeUnit.SECONDS.sleep(1);
            } finally {
                context.stop();
            }
        } catch ( Exception e)
        {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }

    }

    private static RouteBuilder createErrorRoute(final ErrorProcessor errorProcessor){
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:errors").log("NO SE ENVIO EL MENSAJE:").process(errorProcessor).to("activemq2:dead_msgs");
            }
        };
        return  routeBuilder;
    }


    private static RouteBuilder createRoute( final ErrorProcessor errorProcessor) {
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {


                errorHandler(deadLetterChannel("direct:errors")
                        .maximumRedeliveries(2).redeliveryDelay(0));
                ChoiceDefinition choiceDefinition =from("seda:msgToAtMsg").process(errorProcessor).choice();

                choiceDefinition.when(header("DESTINATION_BOAT").isEqualTo("AT")).log("MENSAJE ENVIADO").to("activemq:queue:QUEUE.AT", "activemq:queue:QUEUE.MSG");
                choiceDefinition.when(header("DESTINATION_BOAT").isEqualTo("BT")).to("activemq2:queue:TEST.AT", "activemq2:queue:TEST.MSG");
                choiceDefinition.otherwise().log("NO SE FUE").end();
            }
        };
        return routeBuilder;
    }


    private static RouteBuilder createConsumerRoute(final ConsumeProcessor consumeProcessor){
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:queue:QUEUE.AT").process(consumeProcessor).end();
            }
        };
        return routeBuilder;
}


    public static void camelSendMsg(ProducerTemplate template,String destinyBoat) {
        try {
            String msg = "CUT 2.1.1//CFT 2 - CGT 2.1//CFT 2//Confidencial//OPER/OPAER-0402//MSGID/PIM/CUT 2.1.1/1/JULIO/2016//A6/UNIDAD/1//A7/POSICION/23/-/-//A9/ADMIN/4//K15/PIM/2/ 01� 43' 04\" S/081� 10' 34\" W/031320RFEB16/5//K16/FORMACION/-/5//K18/SOA/16.82//Z1/ACR/5//";
            HashMap<String,Object> headers =new HashMap<String,Object>();
            headers.put( "DESTINATION_BOAT", destinyBoat);
            headers.put( "LOCALE"  , Locale.getDefault().toString());
            template.sendBodyAndHeaders("seda:msgToAtMsg", msg, headers);


        } catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();


        }
    }
}

