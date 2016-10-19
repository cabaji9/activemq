import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by HW on 8/17/16.
 */
public class ErrorProcessor implements Processor {

    private static Logger logger = Logger.getLogger(ErrorProcessor.class.getName());
    Charset utf8 = StandardCharsets.UTF_8;

    public void process(Exchange exchange) throws Exception {

        String trama =getTrama(exchange);
        String destinationBoat = (String) exchange.getIn().getHeader("DESTINATION_BOAT");
        String content =getContent(destinationBoat,trama);
        File file = createFileOrReturnExistent("/Users/User/"+createFileName());
        writeToFileError(content,file);
        logger.info("INGRESO" + trama);


    }

    private String getContent(String destinationBoat, String trama){
       StringBuilder result = new StringBuilder();
        result.append("NO SE ENVIO EL MENSAJE AL DESTINO: ");
        result.append(destinationBoat);
        result.append("\n A LAS : "+ new Date());
        result.append("\n");
        result.append("CON LA SIGUIENTE TRAMA:");
        result.append("\n");
        result.append(trama);
        result.append("\n");
        result.append("\n");
        return result.toString();
    }


    private String getTrama(Exchange exchange){
        String trama = null;
        if(exchange != null && exchange.getIn() != null && exchange.getIn().getBody() != null){
           trama = exchange.getIn().getBody().toString();
        }
        return trama;
    }



    private String createFileName(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month=cal.get(Calendar.MONTH);
        int day=cal.get(Calendar.DAY_OF_MONTH);
        int year =cal.get(Calendar.YEAR);
        return day+"_"+month+"_"+year+"_errores.txt";
    }


    private void writeToFileError(String content,File file) {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            lines.add(content);
            Files.write(Paths.get(file.getAbsolutePath()), lines, utf8, StandardOpenOption.APPEND);


        } catch (Exception e) {
            logger.log(Level.SEVERE, "No se puede guardar en el archivo de errores", e);
        }

    }

    private File createFileOrReturnExistent(String fileName) {
        File file = null;
        try {

            file = new File(fileName);


            if (!file.exists()) {
                file.createNewFile();
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return file;
    }
}
